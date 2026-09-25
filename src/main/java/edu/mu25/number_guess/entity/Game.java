package edu.mu25.number_guess.entity;

import edu.mu25.number_guess.enums.GameStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer secretNumber;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn (name = "player_id", nullable = false)
    private Player player;

    @OneToMany (mappedBy = "game", cascade = CascadeType.ALL)
    private List<Guess> guesses = new ArrayList<>();

    public Game(Player player, int secretNumber) {
        this.player = player;
        this.secretNumber = secretNumber;
        this.status = GameStatus.ACTIVE;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

}
