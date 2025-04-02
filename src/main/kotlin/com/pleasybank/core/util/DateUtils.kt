package com.pleasybank.core.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 날짜와 시간 관련 유틸리티 클래스
 */
object DateUtils {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
    private val ISO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE
    private val ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME
    
    /**
     * 현재 날짜를 yyyyMMdd 형식으로 반환
     */
    fun getCurrentDate(): String {
        return LocalDate.now().format(DATE_FORMATTER)
    }
    
    /**
     * 현재 날짜와 시간을 yyyyMMddHHmmss 형식으로 반환
     */
    fun getCurrentDateTime(): String {
        return LocalDateTime.now().format(DATETIME_FORMATTER)
    }
    
    /**
     * 현재 날짜를 ISO 형식(yyyy-MM-dd)으로 반환
     */
    fun getCurrentDateIso(): String {
        return LocalDate.now().format(ISO_DATE_FORMATTER)
    }
    
    /**
     * 현재 날짜와 시간을 ISO 형식(yyyy-MM-ddTHH:mm:ss)으로 반환
     */
    fun getCurrentDateTimeIso(): String {
        return LocalDateTime.now().format(ISO_DATETIME_FORMATTER)
    }
    
    /**
     * 문자열 날짜를 LocalDate 객체로 변환 (yyyyMMdd 형식)
     */
    fun parseDate(date: String): LocalDate {
        return LocalDate.parse(date, DATE_FORMATTER)
    }
    
    /**
     * 문자열 날짜와 시간을 LocalDateTime 객체로 변환 (yyyyMMddHHmmss 형식)
     */
    fun parseDateTime(dateTime: String): LocalDateTime {
        return LocalDateTime.parse(dateTime, DATETIME_FORMATTER)
    }
    
    /**
     * 날짜를 특정 형식으로 포맷팅
     */
    fun formatDate(date: LocalDate, pattern: String): String {
        return date.format(DateTimeFormatter.ofPattern(pattern))
    }
    
    /**
     * 날짜와 시간을 특정 형식으로 포맷팅
     */
    fun formatDateTime(dateTime: LocalDateTime, pattern: String): String {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern))
    }
    
    /**
     * 특정 일수를 뺀 날짜 반환
     */
    fun minusDays(days: Long): String {
        return LocalDate.now().minusDays(days).format(DATE_FORMATTER)
    }
    
    /**
     * 특정 일수를 더한 날짜 반환
     */
    fun plusDays(days: Long): String {
        return LocalDate.now().plusDays(days).format(DATE_FORMATTER)
    }
} 