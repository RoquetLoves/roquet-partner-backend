/*
package com.lk.partner.manager;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

*/
/**
 * Cos 操作测试
 *//*

@SpringBootTest
class CosManagerTest {

    @Test
    void test() throws FileNotFoundException {


        String endpoint = "oss-cn-shenzhen.aliyuncs.com";
        String accessKeyId = "LTAI5tAjx9JLPjZo4fJQzGfz";
        String accessKeySecret = "1v7teiZKu3QovYWg4S3C4t9TSLAgI8";

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        InputStream inputStream = new FileInputStream("D:\\资料\\ea4c87e4627469eb41c7743d8430d90.jpg");
        ossClient.putObject("roquet","ea4c87e4627469eb41c7743d8430d90.jpg",inputStream);

        ossClient.shutdown();
    }

    */
/*@Resource
    private CosManager cosManager;

    @Test
    void putObject() {
        String fileName = "D:\\Tencent\\Roquet-partner-backend\\doc\\加入加密状态的队伍.png";
        cosManager.putObject("test.png", fileName);
    }*//*

}*/
