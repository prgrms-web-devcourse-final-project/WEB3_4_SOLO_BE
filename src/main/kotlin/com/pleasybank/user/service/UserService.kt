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
        
        // 계좌 수와 총 잔액 계산
        val accountCount = user.accounts.size
        val totalBalance = user.accounts.sumOf { it.balance }
        
        return UserProfileResponse(
            id = user.id!!,
            email = user.email,
            name = user.name,
            phoneNumber = user.phoneNumber ?: "",
            accountCount = accountCount,
            totalBalance = totalBalance,
            pinAuthEnabled = pinEnabled,
            bioAuthEnabled = bioAuthEnabled,
            lastLogin = user.lastLoginAt,
            status = user.status
        )
    }
    
    @Transactional
    fun updateUserProfile(userId: Long, request: UserProfileUpdateRequest): UserProfileUpdateResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        
        // 불변 객체 패턴 사용 - copy 메서드 사용
        val updatedUser = user.copy(
            name = request.name,
            phoneNumber = request.phoneNumber,
            updatedAt = LocalDateTime.now()
        )
        
        userRepository.save(updatedUser)
        
        return UserProfileUpdateResponse(
            id = updatedUser.id!!,
            name = updatedUser.name,
            phoneNumber = updatedUser.phoneNumber ?: "",
            updatedAt = updatedUser.updatedAt
        )
    }
} 