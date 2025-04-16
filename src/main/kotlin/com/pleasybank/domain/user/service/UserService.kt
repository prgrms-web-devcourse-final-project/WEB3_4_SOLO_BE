package com.pleasybank.domain.user.service

import com.pleasybank.domain.user.dto.UserDto
import com.pleasybank.domain.user.entity.User
import org.springframework.web.multipart.MultipartFile

interface UserService {
    /**
     * 사용자 ID로 사용자 정보 조회
     */
    fun getUserById(id: Long): UserDto.Response
    
    /**
     * 사용자 정보 업데이트
     */
    fun updateUser(id: Long, request: UserDto.UpdateRequest): UserDto.Response
    
    /**
     * 비밀번호 변경
     */
    fun updatePassword(id: Long, currentPassword: String, newPassword: String): Boolean
    
    /**
     * 사용자 삭제
     */
    fun deleteUser(id: Long)
    
    /**
     * 프로필 이미지 업데이트
     */
    fun updateProfileImage(id: Long, file: MultipartFile): UserDto.Response
    
    /**
     * 이메일로 사용자 조회
     */
    fun getUserByEmail(email: String): User?
} 