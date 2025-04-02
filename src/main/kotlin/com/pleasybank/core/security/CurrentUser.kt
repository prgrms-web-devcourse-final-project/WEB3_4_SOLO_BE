package com.pleasybank.core.security

import org.springframework.security.core.annotation.AuthenticationPrincipal

/**
 * 현재 인증된 사용자를 주입받기 위한 어노테이션
 * UserPrincipal 객체를 컨트롤러 메서드에 주입할 때 사용합니다.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@AuthenticationPrincipal
annotation class CurrentUser 