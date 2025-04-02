package com.pleasybank.domain.user.service

import com.pleasybank.domain.user.dto.UserProfileResponse
import com.pleasybank.domain.user.dto.UserProfileUpdateRequest
import com.pleasybank.domain.user.dto.UserProfileUpdateResponse

/**
 * 사용자 서비스 인터페이스
 * 사용자 프로필 조회 및 수정 관련 기능을 제공합니다.
 */
interface UserService {
    /**
     * 사용자 프로필 조회
     */
    fun getUserProfile(userId: Long): UserProfileResponse
    
    /**
     * 사용자 프로필 수정
     */
    fun updateUserProfile(userId: Long, request: UserProfileUpdateRequest): UserProfileUpdateResponse
} 