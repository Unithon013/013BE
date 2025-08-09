package com.example.backend.service;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Getter
@Service
public class FileStorageService {

    // 파일을 저장할 경로 (프로젝트 루트에 'media' 폴더 생성)
    private final Path fileStorageLocation = Paths.get("media").toAbsolutePath().normalize();

    public FileStorageService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // 파일 이름의 고유성을 위해 UUID 사용
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        try {
            // 파일 저장 위치를 확인
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            // 파일을 해당 위치로 이동
            file.transferTo(targetLocation);

//            // 저장된 파일의 전체 경로를 문자열로 반환
//            return targetLocation.toString();
            // 저장된 파일의 실제 절대 경로 대신, 프론트에서 접근 가능한 URL 반환
            return "/media/" + fileName;

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public File getFile(String filePath) {
        return new File(filePath);
    }
}