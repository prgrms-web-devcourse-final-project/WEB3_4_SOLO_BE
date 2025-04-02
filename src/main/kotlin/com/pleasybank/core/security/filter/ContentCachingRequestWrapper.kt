package com.pleasybank.core.security.filter

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.util.StreamUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * HTTP 요청 본문을 캐싱하여 여러 번 읽을 수 있게 하는 래퍼 클래스
 */
class ContentCachingRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private val cachedBody: ByteArray
    private val charset: Charset = StandardCharsets.UTF_8

    init {
        // 요청 본문을 한 번 읽어서 메모리에 캐시
        val inputStream = super.getInputStream()
        cachedBody = StreamUtils.copyToByteArray(inputStream)
    }

    override fun getInputStream(): ServletInputStream {
        // 캐시된 본문으로부터 새 입력 스트림 생성
        val cachedInputStream = ByteArrayInputStream(cachedBody)
        
        return object : ServletInputStream() {
            override fun isFinished(): Boolean = cachedInputStream.available() == 0
            override fun isReady(): Boolean = true
            override fun setReadListener(readListener: ReadListener?) {
                throw UnsupportedOperationException("ReadListener는 지원되지 않습니다")
            }
            override fun read(): Int = cachedInputStream.read()
        }
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(getInputStream(), charset))
    }

    /**
     * 캐시된 요청 본문을 문자열로 반환
     */
    fun getContentAsString(): String {
        return String(cachedBody, charset)
    }
} 