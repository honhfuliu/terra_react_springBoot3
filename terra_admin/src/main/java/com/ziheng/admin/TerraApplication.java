package com.ziheng.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(scanBasePackages = "com.ziheng")
@MapperScan("com.ziheng.system.**.mapper")
public class TerraApplication {
    public static void main(String[] args) {
        SpringApplication.run(TerraApplication.class, args);
    }
}