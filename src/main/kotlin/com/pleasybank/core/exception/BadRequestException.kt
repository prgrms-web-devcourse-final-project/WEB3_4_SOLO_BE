package com.pleasybank.core.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * 잘못된 요청 예외
 * 클라이언트의 요청이 잘못되었을 때 발생하는 예외
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException(message: String) : RuntimeException(message) 