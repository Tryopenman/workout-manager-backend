package com.franciscoosorio.workoutmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.franciscoosorio.workoutmanager.domain.workout.Workout;

public interface WorkoutRepository extends JpaRepository<Workout,UUID>{

    Optional<Workout> findByIdAndUser_UserId(UUID workoutId, UUID userId);

    List<Workout> findByUser_UserId(UUID userId);
    
    Boolean existsByIdAndUser_UserId(UUID workoutId, UUID userId);
}
