package com.pleasybank.user.controller

import com.pleasybank.user.dto.UserProfileResponse
import com.pleasybank.user.dto.UserProfileUpdateRequest
import com.pleasybank.user.dto.UserProfileUpdateResponse
import com.pleasybank.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    
    @GetMapping("/me")
    fun getMyProfile(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<UserProfileResponse> {
        // 실제 구현에서는 토큰에서 사용자 ID를 추출하는 로직이 필요하지만, 여기서는 임시로 1L을 사용
        val userId = 1L // 토큰에서 추출한 사용자 ID
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
} 