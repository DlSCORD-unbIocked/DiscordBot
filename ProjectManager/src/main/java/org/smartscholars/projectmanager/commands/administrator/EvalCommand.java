package org.smartscholars.projectmanager.commands.administrator;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.Permission;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.security.SecureClassLoader;
import java.util.*;

@CommandInfo(
    name = "eval",
    description = "The bot will evaluate the code",
    permissions = {Permission.ADMINISTRATOR},
    options = {
        @CommandOption(
            name = "code",
            description = "The code you want to evaluate",
            type = OptionType.STRING,
            required = true
        )
    }
)
public class EvalCommand implements ICommand {

    private final Logger logger = LoggerFactory.getLogger(EvalCommand.class);

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String codeToEval = Objects.requireNonNull(event.getOption("code")).getAsString();

        try {
            String className = "DynamicClass";
            String sourceCode = "public class " + className + " { public static String execute() { " + codeToEval + " } }";

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            CustomClassLoader classLoader = new CustomClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            JavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
            JavaFileObject file = new JavaSourceFromString(className, sourceCode);
            Iterable<? extends JavaFileObject> compilationUnits = List.of(file);
            MemoryByteCode memoryByteCode = new MemoryByteCode(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.CLASS.extension));
            fileManager = new ForwardingJavaFileManager<JavaFileManager>(fileManager) {
                @Override
                public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
                    return memoryByteCode;
                }
            };
            boolean compilationResult = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits).call();

            if (!compilationResult) {
                StringBuilder errors = new StringBuilder();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    errors.append(String.format("Error on line %d in %s%n", diagnostic.getLineNumber(), diagnostic.getSource().toUri()));
                    errors.append(diagnostic.getMessage(null)).append("\n");
                }
                throw new Exception("Compilation failed.\n" + errors.toString());
            }

            classLoader.addClass(className, memoryByteCode.getByteCode());
            Class<?> compiledClass = Class.forName(className, true, classLoader);
            Method method = compiledClass.getDeclaredMethod("execute");
            String result = (String) method.invoke(null);

            event.reply("Result: " + result).queue();
        } catch (Exception e) {
            event.reply("Error evaluating code: " + e.getMessage()).queue();
        }
    }


    static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;
        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    static class CustomClassLoader extends SecureClassLoader {
        private final Map<String, byte[]> classes = new HashMap<>();

        public void addClass(String name, byte[] b) {
            classes.put(name, b);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] b = classes.get(name);
            if (b == null) {
                throw new ClassNotFoundException(name);
            }
            return defineClass(name, b, 0, b.length);
        }
    }

    static class MemoryByteCode extends SimpleJavaFileObject {
        private ByteArrayOutputStream outputStream;

        protected MemoryByteCode(URI uri) {
            super(uri, Kind.CLASS);
            outputStream = new ByteArrayOutputStream();
        }

        @Override
        public ByteArrayOutputStream openOutputStream() throws IOException {
            return outputStream;
        }

        public byte[] getByteCode() {
            return outputStream.toByteArray();
        }
    }
}
