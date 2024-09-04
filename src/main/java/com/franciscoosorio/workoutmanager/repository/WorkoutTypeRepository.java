package com.franciscoosorio.workoutmanager.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.franciscoosorio.workoutmanager.domain.workouttype.WorkoutType;

public interface WorkoutTypeRepository extends JpaRepository<WorkoutType,UUID>{
    
}
