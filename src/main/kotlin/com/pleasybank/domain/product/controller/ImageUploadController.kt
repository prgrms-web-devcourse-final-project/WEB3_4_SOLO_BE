package com.pleasybank.domain.product.controller

import com.pleasybank.core.service.FileStorageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/api/images")
@Tag(name = "이미지 업로드 API", description = "이미지 업로드 관련 API")
class ImageUploadController(
    private val fileStorageService: FileStorageService
) {

    @Operation(summary = "상품 이미지 업로드", description = "금융 상품 이미지를 업로드합니다.")
    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    fun uploadProductImage(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        validateImage(file)
        
        val filename = fileStorageService.uploadFile(file, "products")
        
        return ResponseEntity.ok(mapOf(
            "filename" to filename,
            "message" to "이미지가 성공적으로 업로드되었습니다."
        ))
    }
    
    @Operation(summary = "프로필 이미지 업로드", description = "사용자 프로필 이미지를 업로드합니다.")
    @PostMapping("/profiles")
    @PreAuthorize("isAuthenticated()")
    fun uploadProfileImage(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, String>> {
        validateImage(file)
        
        val filename = fileStorageService.uploadFile(file, "profiles")
        
        return ResponseEntity.ok(mapOf(
            "filename" to filename,
            "message" to "프로필 이미지가 성공적으로 업로드되었습니다."
        ))
    }
    
    @Operation(summary = "이미지 삭제", description = "업로드된 이미지를 삭제합니다.")
    @DeleteMapping("/{bucket}/{filename}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteImage(
        @PathVariable bucket: String,
        @PathVariable filename: String
    ): ResponseEntity<Map<String, String>> {
        // 유효한 버킷 이름인지 확인
        if (bucket !in listOf("products", "profiles", "documents")) {
            return ResponseEntity.badRequest().body(mapOf(
                "error" to "유효하지 않은 버킷입니다."
            ))
        }
        
        fileStorageService.deleteFile(filename, bucket)
        
        return ResponseEntity.ok(mapOf(
            "message" to "이미지가 성공적으로 삭제되었습니다."
        ))
    }
    
    /**
     * 이미지 파일인지 검증하는 메소드
     */
    private fun validateImage(file: MultipartFile) {
        // 파일이 비어있는지 확인
        if (file.isEmpty) {
            throw IllegalArgumentException("빈 파일입니다.")
        }
        
        // 파일 크기 제한 (10MB)
        if (file.size > 10 * 1024 * 1024) {
            throw IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.")
        }
        
        // 이미지 파일 타입 검증
        val contentType = file.contentType?.lowercase(Locale.getDefault())
        if (contentType == null || !contentType.startsWith("image/")) {
            throw IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.")
        }
        
        // 허용된 이미지 형식만 받기
        val allowedTypes = listOf("image/jpeg", "image/png", "image/gif", "image/webp")
        if (contentType !in allowedTypes) {
            throw IllegalArgumentException("지원되는 이미지 형식은 JPEG, PNG, GIF, WEBP입니다.")
        }
    }
} 