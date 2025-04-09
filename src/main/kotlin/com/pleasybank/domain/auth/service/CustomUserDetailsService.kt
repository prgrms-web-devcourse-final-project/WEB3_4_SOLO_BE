package com.pleasybank.domain.auth.service

import com.pleasybank.domain.auth.model.CustomUserDetails
import com.pleasybank.domain.user.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Spring Security를 위한 사용자 정보 로드 서비스
 */
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    /**
     * 사용자 이메일을 통해 사용자 정보를 로드하여 CustomUserDetails 객체로 반환
     */
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: $username")
        
        return CustomUserDetails(user)
    }
} 