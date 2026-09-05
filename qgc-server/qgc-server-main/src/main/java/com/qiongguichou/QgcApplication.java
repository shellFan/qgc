package com.qiongguichou;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 穷鬼筹 - 主启动类
 */
@SpringBootApplication
@MapperScan("com.qiongguichou.**.mapper")
@EnableScheduling
public class QgcApplication {

    public static void main(String[] args) {
        SpringApplication.run(QgcApplication.class, args);
    }
}