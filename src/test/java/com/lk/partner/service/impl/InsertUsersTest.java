/*
package com.lk.partner.service.impl;


import com.lk.partner.model.entity.User;
import com.lk.partner.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(60, 1000, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));


    @Resource
    private StringRedisTemplate stringRedisTemplate;
    */
/**
     * 批量插入用户
     *//*

//    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    */
/*@Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        System.out.println("aaaaaaaaa");
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假罗凯");
            user.setUserAccount("fakeluokai");
            user.setAvatarUrl("https://nimg.ws.126.net/?url=http%3A%2F%2Fdingyue.ws.126.net%2F2023%2F1116%2Fbb8f1d6dj00s47y6s00l1c000ne00grm.jpg&thumbnail=660x2147483647&quality=80&type=jpg");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            user.setPlanetCode("111111111");
            userList.add(user);
        }
        // 30 秒 10万条
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }*//*


    */
/**
     * 并发批量插入用户
     *//*

    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 分10组
        int j = 0;
        int batchSize = 5000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("假罗凯");
                user.setUserAccount("fakeluokai");
                user.setUserAvatarUrl("https://nimg.ws.126.net/?url=http%3A%2F%2Fdingyue.ws.126.net%2F2023%2F1116%2Fbb8f1d6dj00s47y6s00l1c000ne00grm.jpg&thumbnail=660x2147483647&quality=80&type=jpg");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setEmail("123@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }

            }
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
                System.out.println("ThreadName" + Thread.currentThread().getName());
               userService.saveBatch(userList, batchSize);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

}
*/
