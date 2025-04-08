package com.pleasybank.core.service

import io.minio.*
import io.minio.errors.MinioException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.*

/**
 * MinIO를 사용한 파일 스토리지 서비스
 * MinIO가 활성화된 경우에만 사용됨
 */
@Service
@ConditionalOnProperty(name = ["minio.enabled"], havingValue = "true")
class FileStorageService(
    private val minioClient: MinioClient,
    @Value("\${minio.bucketName}") private val bucketName: String
) {

    /**
     * 파일을 업로드하고 접근 URL을 반환
     */
    fun uploadFile(file: MultipartFile, customBucket: String? = null): String {
        try {
            val bucket = customBucket ?: bucketName
            val filename = generateUniqueFilename(file.originalFilename ?: "unknown")
            val contentType = file.contentType ?: "application/octet-stream"
            
            // 버킷이 없다면 생성
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
            }
            
            // 파일 업로드
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(filename)
                    .stream(file.inputStream, file.size, -1)
                    .contentType(contentType)
                    .build()
            )
            
            return filename
        } catch (e: MinioException) {
            throw RuntimeException("파일 업로드 중 오류 발생: ${e.message}", e)
        }
    }
    
    /**
     * 파일을 다운로드
     */
    fun downloadFile(filename: String, customBucket: String? = null): InputStream {
        try {
            val bucket = customBucket ?: bucketName
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(filename)
                    .build()
            )
        } catch (e: MinioException) {
            throw RuntimeException("파일 다운로드 중 오류 발생: ${e.message}", e)
        }
    }
    
    /**
     * 파일 삭제
     */
    fun deleteFile(filename: String, customBucket: String? = null) {
        try {
            val bucket = customBucket ?: bucketName
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(filename)
                    .build()
            )
        } catch (e: MinioException) {
            throw RuntimeException("파일 삭제 중 오류 발생: ${e.message}", e)
        }
    }
    
    /**
     * 고유한 파일 이름 생성
     */
    private fun generateUniqueFilename(originalFilename: String): String {
        val extension = originalFilename.substringAfterLast('.', "")
        val randomUUID = UUID.randomUUID().toString()
        return if (extension.isNotEmpty()) "$randomUUID.$extension" else randomUUID
    }
} 