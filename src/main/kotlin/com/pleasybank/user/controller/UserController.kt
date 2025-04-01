package com.pleasybank.user.controller

import com.pleasybank.user.dto.*
import com.pleasybank.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "사용자 관리 API")
class UserController(
    private val userService: UserService
) {
    
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    fun getCurrentUser(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<UserProfileResponse> {
        val userId = userDetails.username.toLong()
        val response = userService.getUserProfile(userId)
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/me")
    fun updateMyProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: UserProfileUpdateRequest
    ): ResponseEntity<UserProfileUpdateResponse> {
        // 실제 구현에서는 토큰에서 사용자 ID를 추출하는 로직이 필요하지만, 여기서는 임시로 1L을 사용
        val userId = 1L // 토큰에서 추출한 사용자 ID
        val response = userService.updateUserProfile(userId, request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/profile")
    @Operation(summary = "프로필 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    fun getUserProfile(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<UserProfileResponse> {
        // 실제 구현에서는 토큰에서 사용자 ID를 추출하는 로직이 필요하지만, 여기서는 임시로 1L을 사용
        val userId = 1L // 토큰에서 추출한 사용자 ID
        val response = userService.getUserProfile(userId)
        return ResponseEntity.ok(response)
    }
    
    @PutMapping("/profile")
    @Operation(summary = "프로필 수정", description = "로그인한 사용자의 프로필 정보를 수정합니다.")
    fun updateUserProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: UserProfileUpdateRequest
    ): ResponseEntity<UserProfileUpdateResponse> {
        // 실제 구현에서는 토큰에서 사용자 ID를 추출하는 로직이 필요하지만, 여기서는 임시로 1L을 사용
        val userId = 1L // 토큰에서 추출한 사용자 ID
        val response = userService.updateUserProfile(userId, request)
        return ResponseEntity.ok(response)
    }
} 