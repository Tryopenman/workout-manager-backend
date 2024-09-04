package com.franciscoosorio.workoutmanager.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.franciscoosorio.workoutmanager.domain.exercise.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise,UUID>{
    
}
