package com.example.backend.dto;

import com.example.backend.entity.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecommendedUserDto {
    private Long userId;
    private String name;
    private String age;
    private String hobbies;
    private String location;
    private String videoUrl;

    public RecommendedUserDto(User user) {
        this.userId = user.getId();
        this.name = user.getName();
        this.age = user.getAge();
        this.hobbies = user.getHobbies();
        this.location = user.getLocation();
        this.videoUrl = user.getVideoUrl();
    }
}