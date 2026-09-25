package edu.mu25.number_guess.dto;

import edu.mu25.number_guess.enums.GuessResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GuessResponseDto {
    private Integer id;
    private Integer gameId;
    private Integer guessedNumber;
    private GuessResult result;
}

