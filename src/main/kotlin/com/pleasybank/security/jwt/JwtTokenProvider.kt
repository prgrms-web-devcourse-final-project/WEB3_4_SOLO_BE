package com.pleasybank.security.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${app.auth.tokenSecret}") private val jwtSecret: String,
    @Value("\${app.auth.tokenExpirationMsec}") private val jwtExpirationMs: Long,
    @Value("\${app.auth.refreshTokenExpirationMsec}") private val refreshTokenExpirationMs: Long
) {

    fun createToken(userId: String, role: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .setSubject(userId)
            .claim("role", role)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS384, jwtSecret)
            .compact()
    }

    fun createRefreshToken(userId: String): String {
        val now = Date()
        val expiryDate = Date(now.time + refreshTokenExpirationMs)

        return Jwts.builder()
            .setSubject(userId)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS384, jwtSecret)
            .compact()
    }

    fun getUserIdFromToken(token: String): String {
        val claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .body

        return claims.subject
    }

    fun validateToken(authToken: String): Boolean {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken)
            return true
        } catch (ex: SignatureException) {
            // 잘못된 JWT 서명
        } catch (ex: MalformedJwtException) {
            // 잘못된 JWT 토큰
        } catch (ex: ExpiredJwtException) {
            // 만료된 JWT 토큰
        } catch (ex: UnsupportedJwtException) {
            // 지원되지 않는 JWT 토큰
        } catch (ex: IllegalArgumentException) {
            // JWT 토큰이 비어있음
        }
        return false
    }
} 