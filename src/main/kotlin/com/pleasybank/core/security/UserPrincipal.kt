package com.pleasybank.core.security

import com.pleasybank.domain.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * 스프링 시큐리티가 인증에 사용하는 UserDetails 구현체
 * 인증된 사용자의 정보와 권한을 담고 있습니다.
 */
class UserPrincipal(
    val id: Long,
    private val email: String,
    private val password: String,
    val name: String,
    val profileImage: String?,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {
    override fun getUsername(): String = email
    override fun getPassword(): String = password
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
    
    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
            
            return UserPrincipal(
                id = user.id!!,
                email = user.email,
                password = user.password,
                name = user.name,
                profileImage = user.profileImageUrl,
                authorities = authorities
            )
        }
    }
} 