package com.solubank.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;


public class DateUtil {
    
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");
    
    
    public static Optional<LocalDate> parseDate(String dateStr) {
        try {
            return Optional.of(LocalDate.parse(dateStr, DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
    
    
    public static Optional<LocalDateTime> parseDateTime(String dateTimeStr) {
        try {
            return Optional.of(LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
    
    
    public static Optional<YearMonth> parseMonth(String monthStr) {
        try {
            return Optional.of(YearMonth.parse(monthStr, MONTH_FORMATTER));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
    
    
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
    
    
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_FORMATTER);
    }
    
    
    public static String formatMonth(YearMonth month) {
        return month.format(MONTH_FORMATTER);
    }
}