package com.example.backend.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        //          * 013BE/
        //         *   backend/   <-- Spring Boot 프로젝트 루트
        //         *     media/   <-- 여기 저장됨
        //         *     src/
        //         *     build...

        // FileStorageService에서 쓰는 media 폴더 경로
        Path mediaDir = Paths.get("media").toAbsolutePath().normalize();

        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + mediaDir + "/");
    }
}
