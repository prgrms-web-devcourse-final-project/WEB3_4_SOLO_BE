package com.pleasybank.core.config

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
@ConditionalOnProperty(name = ["minio.enabled"], havingValue = "true", matchIfMissing = false)
class MinioConfig {

    @Value("\${minio.endpoint}")
    private lateinit var endpoint: String

    @Value("\${minio.accessKey}")
    private lateinit var accessKey: String

    @Value("\${minio.secretKey}")
    private lateinit var secretKey: String

    // 하드코딩된 기본 버킷 목록 사용
    private val defaultBuckets = listOf("profiles", "documents", "products")

    /**
     * MinIO 클라이언트 빈 생성
     * Lazy 어노테이션을 사용하여 실제 필요할 때만 초기화
     */
    @Bean
    @Lazy
    fun minioClient(): MinioClient {
        // MinIO 클라이언트 생성만 하고 실제 연결 시도는 하지 않음
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
        
        // 실제 연결 시도 부분 주석 처리
        // defaultBuckets.forEach { bucketName ->
        //     if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
        //         client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
        //         println("Created MinIO bucket: $bucketName")
        //     }
        // }
    }
} 