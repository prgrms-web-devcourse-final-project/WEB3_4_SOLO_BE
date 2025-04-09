package com.pleasybank.core.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@ConditionalOnProperty(name = ["spring.data.redis.enabled"], havingValue = "true", matchIfMissing = false)
class RedisConfig {

    @Value("\${spring.data.redis.host:localhost}")
    private lateinit var redisHost: String

    @Value("\${spring.data.redis.port:6379}")
    private var redisPort: Int = 0

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val configuration = RedisStandaloneConfiguration()
        configuration.hostName = redisHost
        configuration.port = redisPort
        // configuration.setPassword() // 필요한 경우 Redis 비밀번호 설정
        return LettuceConnectionFactory(configuration)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(redisConnectionFactory())
        
        // Redis 직렬화에 사용할 ObjectMapper 설정 (LocalDateTime 지원)
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        
        // 직렬화 설정
        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = jsonSerializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = jsonSerializer
        
        return template
    }
} 