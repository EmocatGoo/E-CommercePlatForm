package com.yyblcc.ecommerceplatforms.controller;

import com.yyblcc.ecommerceplatforms.domain.po.Result;
import com.yyblcc.ecommerceplatforms.service.MinIOService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;

@RestController
@RequestMapping("/oss")
@RequiredArgsConstructor
public class MinIOController {
    private final MinIOService minIOService;
    
    @Value("${rustfs.endpoint}")
    private String endpoint;
    
    @Value("${rustfs.bucket-name}")
    private String bucketName;

    /**
     * 通用文件上传接口（已废弃，请使用角色专用接口）
     */
    @Deprecated
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result upload(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = minIOService.uploadFile(file);
            return Result.success(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }


    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) throws Exception {
        InputStream inputStream = minIOService.downloadFile(fileName);
        response.setContentType("application/octet-stream");
        String encodingFileName = URLEncoder.encode(fileName, "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + encodingFileName + "\"");
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            response.getOutputStream().write(buffer, 0, bytesRead);
        }
        inputStream.close();
    }

    @DeleteMapping("/delete/{fileName}")
    public String delete(@PathVariable String fileName) throws Exception {
        minIOService.deleteFile(fileName);
        return "File deleted: " + fileName;
    }


}
