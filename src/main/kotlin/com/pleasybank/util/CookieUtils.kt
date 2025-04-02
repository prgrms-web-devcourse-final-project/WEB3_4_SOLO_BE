package com.pleasybank.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory

object CookieUtils {
    private val logger = LoggerFactory.getLogger(CookieUtils::class.java)
    
    fun getCookie(request: HttpServletRequest, name: String): Cookie? {
        val cookies = request.cookies ?: return null
        
        for (cookie in cookies) {
            if (cookie.name == name) {
                logger.debug("쿠키 찾음: ${cookie.name}, 값 길이=${cookie.value.length}, 경로=${cookie.path}, maxAge=${cookie.maxAge}")
                return cookie
            }
        }
        
        logger.debug("쿠키를 찾을 수 없음: $name")
        return null
    }
    
    fun addCookie(
        response: HttpServletResponse, 
        name: String, 
        value: String, 
        maxAge: Int,
        path: String = "/",
        domain: String? = null,
        secure: Boolean = false,
        httpOnly: Boolean = true,
        sameSite: String = "Lax"
    ) {
        // 1. 일반 쿠키 설정
        val cookie = Cookie(name, value)
        cookie.path = path
        cookie.isHttpOnly = httpOnly
        cookie.maxAge = maxAge
        
        if (domain != null) {
            cookie.domain = domain
        }
        
        cookie.secure = secure
        
        // 2. 헤더를 통한 Set-Cookie 설정 (SameSite 속성을 위해)
        val cookieHeader = StringBuilder()
            .append(name).append("=").append(value)
            .append("; Max-Age=").append(maxAge)
            .append("; Path=").append(path)
            
        if (domain != null) {
            cookieHeader.append("; Domain=").append(domain)
        }
        
        if (secure) {
            cookieHeader.append("; Secure")
        }
        
        if (httpOnly) {
            cookieHeader.append("; HttpOnly")
        }
        
        cookieHeader.append("; SameSite=").append(sameSite)
        
        response.addHeader("Set-Cookie", cookieHeader.toString())
        response.addCookie(cookie)
        
        logger.info("쿠키 추가: $name, 값 길이=${value.length}, maxAge=$maxAge, path=$path, sameSite=$sameSite")
    }
    
    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        val cookies = request.cookies
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == name) {
                    // 1. 일반 쿠키 삭제
                    cookie.value = ""
                    cookie.path = "/"
                    cookie.maxAge = 0
                    response.addCookie(cookie)
                    
                    // 2. 헤더를 통한 Set-Cookie 설정 (SameSite=Lax)
                    response.addHeader("Set-Cookie", 
                        "${cookie.name}=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax")
                    
                    logger.info("쿠키 삭제: ${cookie.name}")
                    return
                }
            }
        }
        
        // 쿠키가 없어도 헤더를 통해 삭제 요청
        response.addHeader("Set-Cookie", "$name=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax")
        logger.debug("삭제할 쿠키를 찾을 수 없음: $name")
    }
} 