package com.pleasybank.core.security.oauth2.util

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
                logger.debug("Found cookie: {}={}", name, cookie.value.take(20))
                return cookie
            }
        }
        logger.debug("Cookie not found: {}", name)
        return null
    }

    fun addCookie(
        response: HttpServletResponse,
        name: String,
        value: String,
        maxAge: Int,
        path: String = "/",
        secure: Boolean = false,
        httpOnly: Boolean = true,
        sameSite: String? = null
    ) {
        val cookie = Cookie(name, value)
        cookie.path = path
        cookie.isHttpOnly = httpOnly
        cookie.maxAge = maxAge
        cookie.secure = secure
        
        // sameSite 속성은 Cookie 클래스에서 직접 지원하지 않으므로 헤더를 추가
        if (sameSite != null) {
            val headerValue = String.format("%s=%s; Max-Age=%d; Path=%s; HttpOnly=%s; Secure=%s; SameSite=%s", 
                name, value, maxAge, path, httpOnly, secure, sameSite)
            response.addHeader("Set-Cookie", headerValue)
        } else {
            response.addCookie(cookie)
        }
        
        logger.debug("Added cookie: {}={} (maxAge={}s)", name, value.take(20), maxAge)
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        val cookies = request.cookies
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == name) {
                    cookie.value = ""
                    cookie.path = "/"
                    cookie.maxAge = 0
                    response.addCookie(cookie)
                    logger.debug("Deleted cookie: {}", name)
                    return
                }
            }
        }
        logger.debug("No cookie found to delete: {}", name)
    }
} 