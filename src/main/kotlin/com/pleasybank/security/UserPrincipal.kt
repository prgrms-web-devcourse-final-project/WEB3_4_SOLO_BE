package com.pleasybank.security

import com.pleasybank.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.*

/**
 * Spring Security의 UserDetails와 OAuth2User를 구현한 사용자 프린시펄 클래스
 * 인증된 사용자 정보를 담고 있음
 */
class UserPrincipal(
    val id: Long,
    private val email: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>,
    private val attributes: MutableMap<String, Any> = mutableMapOf()
) : OAuth2User, UserDetails {

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getPassword(): String = password

    override fun getUsername(): String = id.toString()

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    override fun getName(): String = id.toString()

    companion object {
        fun create(user: User): UserPrincipal {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))

            return UserPrincipal(
                id = user.id!!,
                email = user.email,
                password = user.password ?: "",
                authorities = authorities
            )
        }

        fun create(user: User, attributes: Map<String, Any>): UserPrincipal {
            val userPrincipal = create(user)
            userPrincipal.attributes.putAll(attributes)
            return userPrincipal
        }
    }
} 