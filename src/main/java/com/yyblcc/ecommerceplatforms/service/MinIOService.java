package com.yyblcc.ecommerceplatforms.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MinIOService {
    String uploadFile(MultipartFile file) throws Exception;
    InputStream downloadFile(String fileName) throws Exception;
    void deleteFile(String fileName) throws Exception;
    void initBucket() throws Exception;
}
