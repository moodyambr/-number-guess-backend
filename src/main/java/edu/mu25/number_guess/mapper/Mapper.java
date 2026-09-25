package edu.mu25.number_guess.mapper;

import edu.mu25.number_guess.dto.CreatePlayerRequestDto;
import edu.mu25.number_guess.dto.GameResponseDto;
import edu.mu25.number_guess.dto.GuessResponseDto;
import edu.mu25.number_guess.dto.PlayerResponseDto;
import edu.mu25.number_guess.entity.Game;
import edu.mu25.number_guess.entity.Guess;
import edu.mu25.number_guess.entity.Player;

public class Mapper {

    private Mapper() {}


    public static Player toPlayer(CreatePlayerRequestDto dto) {
        return new Player(dto.getUsername());
    }

    public static PlayerResponseDto toPlayerResponseDto(Player player) {
        return new PlayerResponseDto(
                player.getId(),
                player.getUsername(),
                player.getCreatedAt()
        );
    }

    public static GameResponseDto toGameResponseDto(Game game) {
        return new GameResponseDto(
                game.getId(),
                game.getPlayer().getId(),
                game.getStatus()
        );
    }

    public static GuessResponseDto toGuessResponseDto(Guess guess) {
        return new GuessResponseDto(
                guess.getId(),
                guess.getGame().getId(),
                guess.getGuessedNumber(),
                guess.getResult()
        );
    }
}

