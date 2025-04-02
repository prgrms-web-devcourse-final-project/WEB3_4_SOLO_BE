package com.pleasybank.domain.user.service

import com.pleasybank.core.exception.ResourceNotFoundException
import com.pleasybank.domain.auth.repository.UserAuthenticationRepository
import com.pleasybank.domain.user.dto.UserProfileResponse
import com.pleasybank.domain.user.dto.UserProfileUpdateRequest
import com.pleasybank.domain.user.dto.UserProfileUpdateResponse
import com.pleasybank.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * 사용자 서비스 구현체
 * 사용자 프로필 조회 및 수정 관련 기능을 구현합니다.
 */
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userAuthenticationRepository: UserAuthenticationRepository
) : UserService {
    
    /**
     * 사용자 프로필 조회
     */
    @Transactional(readOnly = true)
    override fun getUserProfile(userId: Long): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다.") }
        
        // PIN 설정 여부 확인
        val pinEnabled = userAuthenticationRepository.existsByUserIdAndType(userId, "PIN")
        
        // 생체인증 설정 여부 확인
        val bioAuthEnabled = userAuthenticationRepository.existsByUserIdAndType(userId, "BIOMETRIC")
        
        // 계좌 정보는 오픈뱅킹 API에서 조회하므로 여기서는 기본값 사용
        val accountCount = 0
        val totalBalance = BigDecimal.ZERO
        
        return UserProfileResponse(
            id = user.id!!,
            email = user.email,
            name = user.name,
            phoneNumber = user.phoneNumber ?: "",
            profileImageUrl = user.profileImageUrl,
            lastLoginAt = user.lastLoginAt,
            createdAt = user.createdAt,
            hasPIN = pinEnabled,
            hasBiometric = bioAuthEnabled,
            accountCount = accountCount,
            totalBalance = totalBalance,
            pinAuthEnabled = pinEnabled,
            bioAuthEnabled = bioAuthEnabled,
            lastLogin = user.lastLoginAt,
            status = user.status
        )
    }
    
    /**
     * 사용자 프로필 수정
     */
    @Transactional
    override fun updateUserProfile(userId: Long, request: UserProfileUpdateRequest): UserProfileUpdateResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("사용자를 찾을 수 없습니다.") }
        
        // 업데이트할 필드만 변경
        val updatedUser = user.copy(
            name = request.name ?: user.name,
            phoneNumber = request.phoneNumber,
            profileImageUrl = request.profileImageUrl,
            updatedAt = LocalDateTime.now()
        )
        
        val savedUser = userRepository.save(updatedUser)
        
        return UserProfileUpdateResponse(
            id = savedUser.id!!,
            email = savedUser.email,
            name = savedUser.name,
            phoneNumber = savedUser.phoneNumber ?: "",
            profileImageUrl = savedUser.profileImageUrl,
            updatedAt = savedUser.updatedAt
        )
    }
} 