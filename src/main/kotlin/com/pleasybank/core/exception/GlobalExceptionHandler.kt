package com.pleasybank.core.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    data class ErrorResponse(
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val status: Int,
        val error: String,
        val message: String,
        val path: String? = null,
        val details: Map<String, String> = emptyMap(),
        val errorCode: String? = null
    )

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error("리소스를 찾을 수 없음: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = ex.message ?: "요청한 리소스를 찾을 수 없습니다.",
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "RESOURCE_NOT_FOUND"
        )
        return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentialsException(ex: InvalidCredentialsException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error("인증 정보 유효하지 않음: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            error = HttpStatus.UNAUTHORIZED.reasonPhrase,
            message = ex.message ?: "인증 정보가 유효하지 않습니다.",
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "INVALID_CREDENTIALS"
        )
        return ResponseEntity(errorResponse, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResourceException(ex: DuplicateResourceException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error("중복된 리소스: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = ex.message ?: "이미 존재하는 리소스입니다.",
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "DUPLICATE_RESOURCE"
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }
    
    @ExceptionHandler(DuplicateAccountNumberException::class)
    fun handleDuplicateAccountNumberException(ex: DuplicateAccountNumberException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error("중복된 계좌번호: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = ex.message ?: "이미 등록된 계좌번호입니다.",
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "DUPLICATE_ACCOUNT_NUMBER"
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error("입력값 검증 실패: ${ex.bindingResult.allErrors}")
        val errors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as? FieldError)?.field ?: error.objectName
            val message = error.defaultMessage ?: "유효하지 않은 값입니다."
            fieldName to message
        }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "입력값 검증에 실패했습니다.",
            details = errors,
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "VALIDATION_FAILED"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error("접근 권한 없음: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.FORBIDDEN.value(),
            error = HttpStatus.FORBIDDEN.reasonPhrase,
            message = ex.message ?: "해당 리소스에 접근할 권한이 없습니다.",
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "ACCESS_DENIED"
        )
        return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(InsufficientBalanceException::class)
    fun handleInsufficientBalanceException(ex: InsufficientBalanceException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error("잔액 부족: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message ?: "잔액이 부족합니다.",
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "INSUFFICIENT_BALANCE"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolationException(ex: BusinessRuleViolationException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error("비즈니스 규칙 위반: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message ?: "비즈니스 규칙 위반이 발생했습니다.",
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "BUSINESS_RULE_VIOLATION"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ExternalServiceException::class)
    fun handleExternalServiceException(ex: ExternalServiceException, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error("외부 서비스 연동 오류: ${ex.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.SERVICE_UNAVAILABLE.value(),
            error = HttpStatus.SERVICE_UNAVAILABLE.reasonPhrase,
            message = ex.message ?: "외부 서비스 연동 중 오류가 발생했습니다.",
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "EXTERNAL_SERVICE_ERROR"
        )
        return ResponseEntity(errorResponse, HttpStatus.SERVICE_UNAVAILABLE)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val fullMessage = ex.message ?: "잘못된 요청 파라미터입니다."
        log.error("잘못된 요청 인자: $fullMessage", ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = fullMessage,
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "INVALID_ARGUMENT"
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val fullMessage = ex.message ?: "현재 상태에서 요청한 작업을 수행할 수 없습니다."
        log.error("잘못된 상태: $fullMessage", ex)
        
        val errorResponse = ErrorResponse(
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = fullMessage,
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "INVALID_STATE"
        )
        return ResponseEntity(errorResponse, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        log.error("서버 내부 오류: ${ex.message}", ex)
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            path = request.getDescription(false).replace("uri=", ""),
            errorCode = "INTERNAL_SERVER_ERROR"
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
} 