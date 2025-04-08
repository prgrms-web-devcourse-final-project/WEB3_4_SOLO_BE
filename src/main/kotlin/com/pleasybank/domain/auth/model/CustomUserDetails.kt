package com.pleasybank.domain.auth.model

import com.pleasybank.domain.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Spring Security를 위한 사용자 상세 정보 구현체
 */
class CustomUserDetails(private val user: User) : UserDetails {

    // 식별자 필드 추가
    val id: Long = user.id ?: throw IllegalArgumentException("사용자 ID가 없습니다")
    
    // 추가 정보 필드들
    val name: String = user.name
    val email: String = user.email
    val profileImageUrl: String? = user.profileImageUrl
    
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return user.userRoles.map { SimpleGrantedAuthority(it.role.name) }
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getUsername(): String {
        return user.email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return user.status != "BLOCKED"
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return user.status == "ACTIVE"
    }
} 