package edu.mu25.number_guess.repository;

import edu.mu25.number_guess.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Integer> {
}
