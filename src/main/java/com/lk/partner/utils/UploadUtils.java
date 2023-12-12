package com.lk.partner.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jodd.io.FileNameUtil;
//import org.apache.commons.beanutils.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Configuration
@ConfigurationProperties(prefix = "oss")
public class UploadUtils {
    public static String ALI_DOMAIN = "https://roquet.oss-cn-shenzhen.aliyuncs.com/";

    public static String uploadImage(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension ="." + FileNameUtil.getExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + extension;

        String endpoint = "oss-cn-shenzhen.aliyuncs.com";
        String accessKeyId = "LTAI5tAjx9JLPjZo4fJQzGfz";
        String accessKeySecret = "1v7teiZKu3QovYWg4S3C4t9TSLAgI8";

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject("roquet",fileName,file.getInputStream());

        ossClient.shutdown();
        return ALI_DOMAIN + fileName;
    }
}
