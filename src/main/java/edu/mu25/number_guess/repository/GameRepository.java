package edu.mu25.number_guess.repository;

import edu.mu25.number_guess.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Integer> {
}
