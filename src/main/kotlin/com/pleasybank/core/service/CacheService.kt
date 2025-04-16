package com.pleasybank.core.service

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ConcurrentMap

/**
 * 캐시 서비스 인터페이스
 */
interface CacheService {
    /**
     * 캐시에 값을 저장
     */
    fun setValue(key: String, value: Any, ttl: Long = 3600)
    
    /**
     * 해시에 값 저장
     */
    fun setHashValue(key: String, hashKey: String, value: Any)
    
    /**
     * 캐시에서 값을 조회
     */
    fun getValue(key: String): Any?
    
    /**
     * 해시에서 값 조회
     */
    fun getHashValue(key: String, hashKey: String): Any?
    
    /**
     * 해시의 모든 값 조회
     */
    fun getAllHashValues(key: String): Map<String, Any>
    
    /**
     * 키 만료 시간 설정
     */
    fun setExpire(key: String, ttl: Long)
    
    /**
     * 값 삭제
     */
    fun delete(key: String)
    
    /**
     * 해시에서 특정 필드 삭제
     */
    fun deleteHashKey(key: String, hashKey: String)
    
    /**
     * 값 존재 여부 확인
     */
    fun hasKey(key: String): Boolean
}

/**
 * Redis 기반 캐시 서비스 구현
 */
@Service
@ConditionalOnProperty(name = ["spring.data.redis.enabled"], havingValue = "true", matchIfMissing = false)
class RedisCacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) : CacheService {

    override fun setValue(key: String, value: Any, ttl: Long) {
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS)
    }
    
    override fun setHashValue(key: String, hashKey: String, value: Any) {
        redisTemplate.opsForHash<String, Any>().put(key, hashKey, value)
    }
    
    override fun getValue(key: String): Any? {
        return redisTemplate.opsForValue().get(key)
    }
    
    override fun getHashValue(key: String, hashKey: String): Any? {
        return redisTemplate.opsForHash<String, Any>().get(key, hashKey)
    }
    
    override fun getAllHashValues(key: String): Map<String, Any> {
        val entries = redisTemplate.opsForHash<String, Any>().entries(key)
        return entries.mapKeys { it.key.toString() }
    }
    
    override fun setExpire(key: String, ttl: Long) {
        redisTemplate.expire(key, ttl, TimeUnit.SECONDS)
    }
    
    override fun delete(key: String) {
        redisTemplate.delete(key)
    }
    
    override fun deleteHashKey(key: String, hashKey: String) {
        redisTemplate.opsForHash<String, Any>().delete(key, hashKey)
    }
    
    override fun hasKey(key: String): Boolean {
        return redisTemplate.hasKey(key)
    }
}

/**
 * 메모리 기반 캐시 서비스 구현 (Redis가 비활성화되었을 때 사용)
 */
@Service
@ConditionalOnProperty(name = ["spring.data.redis.enabled"], havingValue = "false", matchIfMissing = true)
class InMemoryCacheService : CacheService {
    private val cache: ConcurrentMap<String, Any> = ConcurrentHashMap()
    private val hashCache: ConcurrentMap<String, ConcurrentMap<String, Any>> = ConcurrentHashMap()
    private val expirations: ConcurrentMap<String, Long> = ConcurrentHashMap()
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    
    init {
        // 주기적으로 만료된 항목 정리
        scheduler.scheduleAtFixedRate({ cleanupExpiredEntries() }, 10, 10, TimeUnit.SECONDS)
    }
    
    private fun cleanupExpiredEntries() {
        val now = System.currentTimeMillis()
        expirations.entries.removeIf { (key, expiry) ->
            if (expiry < now) {
                cache.remove(key)
                hashCache.remove(key)
                true
            } else {
                false
            }
        }
    }
    
    override fun setValue(key: String, value: Any, ttl: Long) {
        cache[key] = value
        if (ttl > 0) {
            val expiryTime = System.currentTimeMillis() + (ttl * 1000)
            expirations[key] = expiryTime
        }
    }
    
    override fun setHashValue(key: String, hashKey: String, value: Any) {
        val hashMap = hashCache.computeIfAbsent(key) { ConcurrentHashMap() }
        hashMap[hashKey] = value
    }
    
    override fun getValue(key: String): Any? {
        return if (isExpired(key)) null else cache[key]
    }
    
    override fun getHashValue(key: String, hashKey: String): Any? {
        return if (isExpired(key)) null else hashCache[key]?.get(hashKey)
    }
    
    override fun getAllHashValues(key: String): Map<String, Any> {
        return if (isExpired(key)) emptyMap() else hashCache[key]?.toMap() ?: emptyMap()
    }
    
    override fun setExpire(key: String, ttl: Long) {
        if (ttl > 0) {
            val expiryTime = System.currentTimeMillis() + (ttl * 1000)
            expirations[key] = expiryTime
        } else {
            expirations.remove(key)
        }
    }
    
    override fun delete(key: String) {
        cache.remove(key)
        hashCache.remove(key)
        expirations.remove(key)
    }
    
    override fun deleteHashKey(key: String, hashKey: String) {
        hashCache[key]?.remove(hashKey)
    }
    
    override fun hasKey(key: String): Boolean {
        return !isExpired(key) && (cache.containsKey(key) || hashCache.containsKey(key))
    }
    
    private fun isExpired(key: String): Boolean {
        val expiry = expirations[key] ?: return false
        return expiry < System.currentTimeMillis()
    }
} 