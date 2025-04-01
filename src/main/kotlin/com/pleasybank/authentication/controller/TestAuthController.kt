package com.pleasybank.authentication.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.HashMap

@RestController
@RequestMapping("/test")
@Tag(name = "Test", description = "테스트용 API")
class TestAuthController {

    @GetMapping("/health")
    @Operation(summary = "테스트 API 상태 확인", description = "테스트 API의 상태를 확인합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "정상 작동 중",
            content = [Content(mediaType = "application/json")])
    ])
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        val response = HashMap<String, Any>()
        response["status"] = "UP"
        response["message"] = "Test API is working"
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