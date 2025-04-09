package com.pleasybank.domain.user.controller

import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.core.security.CurrentUser
import com.pleasybank.domain.user.dto.UserDto
import com.pleasybank.domain.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/users")
@Tag(name = "사용자 API", description = "사용자 정보 관련 API")
class UserController(private val userService: UserService) {

    @Operation(summary = "현재 사용자 정보 조회", description = "인증된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUser(@CurrentUser userId: Long): ResponseEntity<UserDto.Response> {
        val user = userService.getUserById(userId)
        return ResponseEntity.ok(user)
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.hasUserId(authentication, #id)")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserDto.Response> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }

    @Operation(summary = "사용자 정보 업데이트", description = "사용자 정보를 업데이트합니다.")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    fun updateUser(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: UserDto.UpdateRequest
    ): ResponseEntity<UserDto.Response> {
        val updatedUser = userService.updateUser(userId, request)
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.hasUserId(authentication, #id)")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
    @PostMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    fun changePassword(
        @CurrentUser userId: Long,
        @Valid @RequestBody request: UserDto.PasswordUpdateRequest
    ): ResponseEntity<Map<String, String>> {
        val success = userService.updatePassword(userId, request.currentPassword, request.newPassword)
        
        return if (success) {
            ResponseEntity.ok(mapOf("message" to "비밀번호가 성공적으로 변경되었습니다."))
        } else {
            ResponseEntity.badRequest().body(mapOf("error" to "현재 비밀번호가 일치하지 않습니다."))
        }
    }

    @Operation(summary = "프로필 이미지 업로드", description = "사용자의 프로필 이미지를 업로드합니다.")
    @PostMapping("/me/profile-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun uploadProfileImage(
        @CurrentUser userId: Long,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<UserDto.Response> {
        val updatedUser = userService.updateProfileImage(userId, file)
        return ResponseEntity.ok(updatedUser)
    }
} 