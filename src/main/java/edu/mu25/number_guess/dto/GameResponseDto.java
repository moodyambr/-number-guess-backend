package edu.mu25.number_guess.dto;

import edu.mu25.number_guess.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameResponseDto {
    private Integer id;
    private Integer playerId;
    private GameStatus status;
}

