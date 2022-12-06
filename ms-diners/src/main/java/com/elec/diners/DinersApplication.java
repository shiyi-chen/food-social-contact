package com.elec.diners;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = {"com.elec.diners.mapper"})
public class DinersApplication {

    public static void main(String[] args) {
        SpringApplication.run(DinersApplication.class, args);
    }

}
