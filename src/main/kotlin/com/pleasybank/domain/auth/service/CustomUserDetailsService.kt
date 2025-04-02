package com.pleasybank.domain.auth.service

import com.pleasybank.domain.user.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * 스프링 시큐리티의 UserDetailsService 구현체
 * 사용자 이메일로 사용자 정보를 로드하고 UserDetails 객체를 생성합니다.
 */
@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    /**
     * 사용자 이메일로 UserDetails 로드
     */
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("사용자를 찾을 수 없습니다: $username") }
        
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        
        return User.builder()
            .username(user.email)
            .password(user.password)
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(user.status != "ACTIVE")
            .build()
    }
} 