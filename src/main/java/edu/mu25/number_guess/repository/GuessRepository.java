package edu.mu25.number_guess.repository;

import edu.mu25.number_guess.entity.Guess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuessRepository extends JpaRepository<Guess, Integer> {
    List<Guess> findByGame_Id(Integer gameId);
}