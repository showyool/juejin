package com.showyool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class ShowyoolJuejinApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShowyoolJuejinApplication.class, args);
    }

}
