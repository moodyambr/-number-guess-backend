package edu.mu25.number_guess.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class PlayerResponseDto {
    private Integer id;
    private String username;
    private Timestamp createdAt;
}

