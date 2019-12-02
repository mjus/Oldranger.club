package ru.java.mentor.oldranger.club.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UserStatisticDto {

    private String nickName;
    private String email;
    private LocalDateTime registered;
    private String role;
    private LocalDateTime lastComment;
    private LocalDateTime lastVizit;
}