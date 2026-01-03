package com.yyblcc.ecommerceplatforms.service.impl;

import com.yyblcc.ecommerceplatforms.service.MinIOService;
import io.minio.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinIOServiceImplement implements MinIOService {

    @Resource
    private MinioClient minioClient;

    @Value("${rustfs.bucket-name}")
    private String bucketName;

    /**
     * 上传文件
     * @param file
     * @return
     * @throws Exception
     */
    @Override
    public String uploadFile(MultipartFile file) throws Exception{
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = System.currentTimeMillis() + "_" + originalFilename + fileExtension;
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        // 返回文件在 rustFS 中的名称
        return fileName;
    }

    /**
     * 下载文件
     * @param fileName
     * @return
     */
    @Override
    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * 删除文件
     * @param fileName
     */
    @Override
    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * 检查桶是否存在，不存在则创建
     */
    @Override
    public void initBucket() throws Exception{
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }
}
