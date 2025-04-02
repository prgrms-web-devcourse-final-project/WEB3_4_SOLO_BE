package com.pleasybank.authentication.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.HashMap

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test", description = "테스트용 API")
class TestAuthController {

    @GetMapping("/all")
    @Operation(summary = "모든 사용자용 테스트 API", description = "인증 없이 모든 사용자가 접근 가능한 API입니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "정상 작동 중",
            content = [Content(mediaType = "application/json")])
    ])
    fun allAccess(): ResponseEntity<Map<String, Any>> {
        val response = HashMap<String, Any>()
        response["message"] = "누구나 접근 가능한 Public 콘텐츠입니다."
        response["timestamp"] = System.currentTimeMillis()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/user")
    @Operation(summary = "인증된 사용자용 테스트 API", description = "로그인한 사용자만 접근 가능한 API입니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "정상 작동 중",
            content = [Content(mediaType = "application/json")])
    ])
    fun userAccess(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<String, Any>> {
        val response = HashMap<String, Any>()
        response["message"] = "인증된 사용자만 접근 가능한 User 콘텐츠입니다."
        response["userId"] = userDetails.username
        response["timestamp"] = System.currentTimeMillis()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/admin")
    @Operation(summary = "관리자용 테스트 API", description = "관리자 권한이 있는 사용자만 접근 가능한 API입니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "정상 작동 중",
            content = [Content(mediaType = "application/json")])
    ])
    fun adminAccess(
        @Parameter(hidden = true) @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<String, Any>> {
        val response = HashMap<String, Any>()
        response["message"] = "관리자만 접근 가능한 Admin 콘텐츠입니다."
        response["userId"] = userDetails.username
        response["timestamp"] = System.currentTimeMillis()
        return ResponseEntity.ok(response)
    }

    @PostMapping("/login")
    @Operation(summary = "테스트 로그인", description = "테스트용 토큰을 발급합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "테스트 토큰 발급 성공",
            content = [Content(mediaType = "application/json")])
    ])
    fun testLogin(@RequestBody credentials: Map<String, String>): ResponseEntity<Map<String, Any>> {
        val response = HashMap<String, Any>()
        response["accessToken"] = "test-access-token-123456789"
        response["refreshToken"] = "test-refresh-token-987654321"
        response["tokenType"] = "Bearer"
        response["expiresIn"] = 3600
        
        return ResponseEntity.ok(response)
    }
} 