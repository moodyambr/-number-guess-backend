package edu.mu25.number_guess.entity;

import edu.mu25.number_guess.enums.GuessResult;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Guess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer guessedNumber;

    @Enumerated(EnumType.STRING)
    private GuessResult result;

    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    public Guess(Game game, int guessedNumber) {
        this.game = game;
        this.guessedNumber = guessedNumber;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
