package org.smartscholars.projectmanager.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateTimeConverter {
    public static String parseDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate customParsedDate = LocalDate.parse(date, formatter);
        return customParsedDate.toString();
    }

    public static String parseTime(String time) {
        try {
            LocalTime parsedTime = LocalTime.parse(time);
            return parsedTime.toString();
        }
        catch (Exception e) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            try {
                LocalTime parsedTime = LocalTime.parse(time, formatter);
                return parsedTime.toString();
            }
            catch (Exception ex) {
                throw new IllegalArgumentException("Invalid time format. Please use HH:mm or HH:mm:ss");
            }
        }
    }

    public static long convertToMillis(String inputDateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime dateTime = LocalDateTime.parse(inputDateTime, formatter);
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli();
    }

}
