package edu.mu25.number_guess.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGuessRequestDto {
    private Integer gameId;
    private Integer guessedNumber;
}

