package com.pleasybank.user.service

import com.pleasybank.authentication.entity.UserAuthentication
import com.pleasybank.authentication.repository.UserAuthenticationRepository
import com.pleasybank.user.dto.UserProfileResponse
import com.pleasybank.user.dto.UserProfileUpdateRequest
import com.pleasybank.user.dto.UserProfileUpdateResponse
import com.pleasybank.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userAuthenticationRepository: UserAuthenticationRepository
) {
    
    @Transactional(readOnly = true)
    fun getUserProfile(userId: Long): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        val pinEnabled = userAuthenticationRepository.existsByUserIdAndAuthType(userId, "PIN")
        val bioAuthEnabled = userAuthenticationRepository.existsByUserIdAndAuthType(userId, "FINGERPRINT") || 
                userAuthenticationRepository.existsByUserIdAndAuthType(userId, "FACE")
        
        return UserProfileResponse(
            id = user.id!!,
            username = user.email, // 사용자명으로 이메일 사용
            email = user.email,
            name = user.name,
            phone = user.phoneNumber,
            address = user.address,
            pinEnabled = pinEnabled,
            bioAuthEnabled = bioAuthEnabled,
            createdAt = user.createdAt,
            lastLoginAt = user.lastLoginAt
        )
    }
    
    @Transactional
    fun updateUserProfile(userId: Long, request: UserProfileUpdateRequest): UserProfileUpdateResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        // 변경할 필드 업데이트
        user.name = request.name
        user.phoneNumber = request.phone
        user.address = request.address
        user.updatedAt = LocalDateTime.now()
        
        val updatedUser = userRepository.save(user)
        
        return UserProfileUpdateResponse(
            id = updatedUser.id!!,
            name = updatedUser.name,
            phone = updatedUser.phoneNumber,
            address = updatedUser.address,
            updatedAt = updatedUser.updatedAt
        )
    }
} 