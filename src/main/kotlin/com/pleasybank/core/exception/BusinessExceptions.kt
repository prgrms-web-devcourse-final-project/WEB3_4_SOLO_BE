package com.pleasybank.core.exception

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/**
 * 중복된 리소스를 생성하려 할 때 발생하는 예외
 */
class DuplicateResourceException(message: String) : RuntimeException(message)

/**
 * 인증 정보가 유효하지 않을 때 발생하는 예외
 */
class InvalidCredentialsException(message: String) : RuntimeException(message)

/**
 * 권한이 없는 작업을 시도할 때 발생하는 예외
 */
class AccessDeniedException(message: String) : RuntimeException(message)

/**
 * 잔액이 부족할 때 발생하는 예외
 */
class InsufficientBalanceException(message: String) : RuntimeException(message)

/**
 * 비즈니스 규칙 위반 시 발생하는 예외
 */
class BusinessRuleViolationException(message: String) : RuntimeException(message)

/**
 * 외부 서비스 연동 오류 시 발생하는 예외
 */
class ExternalServiceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause) 