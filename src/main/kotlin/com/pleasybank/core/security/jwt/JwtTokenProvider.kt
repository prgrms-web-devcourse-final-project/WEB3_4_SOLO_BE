package com.pleasybank.core.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 */
@Component
class JwtTokenProvider {
    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    
    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String
    
    @Value("\${app.jwt.expiration}")
    private var jwtExpirationMs: Long = 86400000 // 24시간
    
    @Value("\${app.jwt.expiration}")
    private var jwtRefreshExpirationMs: Long = 604800000 // 7일
    
    @Autowired(required = false)
    private var userDetailsService: UserDetailsService? = null
    
    /**
     * 사용자 정보로 액세스 토큰 생성
     */
    fun generateToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal
        
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)
        
        logger.debug("Generating JWT token for user: {}, expires at: {}", 
            authentication.name, expiryDate)
        
        return Jwts.builder()
            .setSubject(authentication.name)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()
    }
    
    /**
     * 사용자명으로 토큰 생성 (이전 API와의 호환성을 위해 추가)
     */
    fun createToken(username: String, role: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)
        
        logger.debug("Creating JWT token for user: {}, expires at: {}", 
            username, expiryDate)
        
        return Jwts.builder()
            .setSubject(username)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()
    }
    
    /**
     * 리프레시 토큰 생성
     */
    fun createRefreshToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtRefreshExpirationMs)
        
        val signingKey = getSigningKey()
        
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact()
    }
    
    /**
     * 토큰에서 사용자명 추출
     */
    fun getUsernameFromToken(token: String): String? {
        val claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
        
        return claims.subject
    }
    
    /**
     * 토큰으로부터 인증 정보 생성
     */
    fun getAuthentication(token: String): Authentication {
        val username = getUsernameFromToken(token)
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        
        // UserDetailsService가 있으면 사용하고, 없으면 임시 객체 생성
        if (userDetailsService != null) {
            val userDetails = userDetailsService!!.loadUserByUsername(username)
            return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
        } else {
            // UserDetailsService가 없는 경우 간단한 인증 객체 생성
            return UsernamePasswordAuthenticationToken(username, "", authorities)
        }
    }
    
    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
            return true
        } catch (e: Exception) {
            logger.error("JWT token validation error", e)
        }
        return false
    }
    
    private fun getSigningKey(): Key {
        val keyBytes = jwtSecret.toByteArray()
        return Keys.hmacShaKeyFor(keyBytes)
    }
} 