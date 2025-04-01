package com.pleasybank.system.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.HashMap

@RestController
@RequestMapping("/system")
@Tag(name = "System", description = "시스템 관리 API")
class SystemController {
    
    @GetMapping("/health")
    @Operation(summary = "시스템 상태 확인", description = "시스템의 현재 상태를 확인합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "시스템 정상 작동 중",
            content = [Content(mediaType = "application/json")])
    ])
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        val response = HashMap<String, Any>()
        response["status"] = "UP"
        response["timestamp"] = System.currentTimeMillis()
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/version")
    @Operation(summary = "시스템 버전 확인", description = "현재 애플리케이션의 버전 정보를 제공합니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "버전 정보 제공 성공",
            content = [Content(mediaType = "application/json")])
    ])
    fun version(): ResponseEntity<Map<String, Any>> {
        val response = HashMap<String, Any>()
        response["version"] = "1.0.0"
        response["buildDate"] = "2023-12-31"
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/test")
    @Operation(summary = "시스템 테스트 엔드포인트", description = "시스템의 동작 테스트를 위한 엔드포인트입니다.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "테스트 성공",
            content = [Content(mediaType = "application/json")])
    ])
    fun test(): ResponseEntity<Map<String, Any>> {
        val response = HashMap<String, Any>()
        response["message"] = "Test endpoint working correctly"
        response["timestamp"] = System.currentTimeMillis()
        return ResponseEntity.ok(response)
    }
} 