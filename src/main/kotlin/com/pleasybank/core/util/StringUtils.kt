package com.pleasybank.core.util

/**
 * 문자열 처리 관련 유틸리티 클래스
 */
object StringUtils {
    /**
     * 문자열이 null이거나 비어있는지 확인
     */
    fun isEmpty(str: String?): Boolean {
        return str == null || str.isEmpty()
    }
    
    /**
     * 문자열이 null이거나 비어있거나 공백만 있는지 확인
     */
    fun isBlank(str: String?): Boolean {
        return str == null || str.isBlank()
    }
    
    /**
     * 문자열이 null이면 빈 문자열 반환, 그렇지 않으면 원래 문자열 반환
     */
    fun nullToEmpty(str: String?): String {
        return str ?: ""
    }
    
    /**
     * 문자열이 비어있으면 기본값 반환, 그렇지 않으면 원래 문자열 반환
     */
    fun emptyToDefault(str: String?, defaultValue: String): String {
        return if (isEmpty(str)) defaultValue else str!!
    }
    
    /**
     * 문자열 마스킹 처리 (PII 정보 보호)
     */
    fun maskPII(input: String?, maskChar: Char = '*', preserveStart: Int = 3, preserveEnd: Int = 0): String {
        if (input.isNullOrEmpty()) {
            return ""
        }
        
        if (input.length <= preserveStart + preserveEnd) {
            return input
        }
        
        val prefix = input.substring(0, preserveStart)
        val suffix = if (preserveEnd > 0) input.substring(input.length - preserveEnd) else ""
        val maskedLength = input.length - preserveStart - preserveEnd
        val masked = maskChar.toString().repeat(maskedLength)
        
        return prefix + masked + suffix
    }
    
    /**
     * 계좌번호 마스킹
     */
    fun maskAccountNumber(accountNumber: String?): String {
        if (accountNumber.isNullOrEmpty()) {
            return ""
        }
        
        return maskPII(accountNumber, '*', 4, 2)
    }
    
    /**
     * 이메일 마스킹
     */
    fun maskEmail(email: String?): String {
        if (email.isNullOrEmpty()) {
            return ""
        }
        
        val parts = email.split("@")
        if (parts.size != 2) {
            return email
        }
        
        val username = parts[0]
        val domain = parts[1]
        
        val maskedUsername = maskPII(username, '*', 3, 0)
        
        return "$maskedUsername@$domain"
    }
    
    /**
     * 문자열이 정수인지 확인
     */
    fun isInteger(str: String?): Boolean {
        if (str.isNullOrEmpty()) {
            return false
        }
        
        return try {
            str.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * 문자열이 실수인지 확인
     */
    fun isNumber(str: String?): Boolean {
        if (str.isNullOrEmpty()) {
            return false
        }
        
        return try {
            str.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * 에러 메시지 포맷팅
     */
    fun formatErrorMessage(error: String): String {
        return when {
            error.contains("authorization_request_not_found") -> "인증 요청 정보를 찾을 수 없습니다 (세션 만료)"
            error.contains("login_failure") -> "로그인 실패 (잘못된 사용자 정보)"
            error.startsWith("[") && error.endsWith("]") -> error.substring(1, error.length - 1)
            else -> error
        }
    }
} 