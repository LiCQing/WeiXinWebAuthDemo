package com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@MapperScan("com.dao")
@ServletComponentScan("com.filter")
public class App_8520 {

    public static void main(String[] args) {
        SpringApplication.run(App_8520.class);
    }

}
