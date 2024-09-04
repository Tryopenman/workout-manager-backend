package com.franciscoosorio.workoutmanager.domain.workout;

import java.util.Set;
import java.util.UUID;

public record UpdateWorkoutDto(String name, String description,UUID workoutTypeId, Set<UUID> exerciseIds) {
    
}
