package com.pleasybank.user.dto

import java.time.LocalDateTime

data class UserProfileResponse(
    val id: Long,
    val username: String,
    val email: String,
    val name: String,
    val phone: String,
    val address: String?,
    val pinEnabled: Boolean,
    val bioAuthEnabled: Boolean,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
)

data class UserProfileUpdateRequest(
    val name: String,
    val phone: String,
    val address: String?
)

data class UserProfileUpdateResponse(
    val id: Long,
    val name: String,
    val phone: String,
    val address: String?,
    val updatedAt: LocalDateTime
) 