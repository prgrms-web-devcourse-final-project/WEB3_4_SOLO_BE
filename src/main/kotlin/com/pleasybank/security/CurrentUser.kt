package com.pleasybank.security

import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 * 현재 인증된 사용자를 컨트롤러 메서드 파라미터에 주입받기 위한 커스텀 어노테이션
 * AuthenticationPrincipal 어노테이션의 래퍼
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@AuthenticationPrincipal
annotation class CurrentUser 