package edu.mu25.number_guess.entity;

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
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private Timestamp createdAt;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<Game> games = new ArrayList<>();

    public Player(String username) {
        this.username = username;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
