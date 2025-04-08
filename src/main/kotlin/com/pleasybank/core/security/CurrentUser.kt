package com.pleasybank.core.security

import org.springframework.security.core.annotation.AuthenticationPrincipal

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@AuthenticationPrincipal(expression = "id")
annotation class CurrentUser 