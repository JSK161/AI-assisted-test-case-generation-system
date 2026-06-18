package com.nun.aitestcase;

import com.nun.aitestcase.config.DeepSeekProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.nun.aitestcase.mapper")
@EnableConfigurationProperties(DeepSeekProperties.class)
public class AiTestCaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTestCaseApplication.class, args);
    }
}
