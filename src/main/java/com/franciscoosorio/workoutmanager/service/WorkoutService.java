package com.franciscoosorio.workoutmanager.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.franciscoosorio.workoutmanager.domain.workout.CreateWorkoutDto;
import com.franciscoosorio.workoutmanager.domain.workout.UpdateWorkoutDto;
import com.franciscoosorio.workoutmanager.domain.workout.Workout;
import com.franciscoosorio.workoutmanager.exception.ExerciseNotFoundException;
import com.franciscoosorio.workoutmanager.exception.UserNotFoundException;
import com.franciscoosorio.workoutmanager.exception.WorkoutNotFoundException;
import com.franciscoosorio.workoutmanager.exception.WorkoutTypeNotFoundException;
import com.franciscoosorio.workoutmanager.repository.ExerciseRepository;
import com.franciscoosorio.workoutmanager.repository.UserRepository;
import com.franciscoosorio.workoutmanager.repository.WorkoutRepository;
import com.franciscoosorio.workoutmanager.repository.WorkoutTypeRepository;

@Service
public class WorkoutService {
    
    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutTypeRepository workoutTypeRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    public Workout createWorkout(String userId, CreateWorkoutDto createWorkoutDto) {

        var workoutType = workoutTypeRepository.findById(createWorkoutDto.workoutTypeId())
            .orElseThrow(() -> new WorkoutTypeNotFoundException("WorkoutType not found"));

        if (createWorkoutDto.exerciseIds() == null) {
            throw new IllegalArgumentException("Exercise set cannot be null");
        }

        var exercises = exerciseRepository.findAllById(createWorkoutDto.exerciseIds()).stream().collect(Collectors.toSet());

        if (exercises.size() != createWorkoutDto.exerciseIds().size()) {
            throw new ExerciseNotFoundException("One or more exercises were not found");
        }

        var user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        var workout = new Workout(
            createWorkoutDto.name(),
            createWorkoutDto.description(),
            workoutType,
            exercises,
            user
        );

        return workoutRepository.save(workout);
    }

    public Workout getWorkoutByIdAndUserId(String workoutId,String userId){

        var id = UUID.fromString(userId);
        var userExists = userRepository.existsById(id);

        if (!userExists) {
            throw new UserNotFoundException("User not found");
        }
        
        var workout = workoutRepository.findByIdAndUser_UserId(UUID.fromString(workoutId),UUID.fromString(userId)).orElseThrow(() -> new WorkoutNotFoundException("Workout not found"));

        return workout;
    }

    public List<Workout> getWorkoutsByUserId(String userId){

        var id = UUID.fromString(userId);
        var userExists = userRepository.existsById(id);

        if (!userExists) {
            throw new UserNotFoundException("User not found");
        }

        return workoutRepository.findByUser_UserId(id);
    }

    public void updateWorkout(String workoutId, String userId, UpdateWorkoutDto updateWorkout){
        
        var entity = workoutRepository.findByIdAndUser_UserId(UUID.fromString(workoutId),UUID.fromString(userId));

        if (entity.isPresent()) {
            var workout = entity.get();

            if (updateWorkout.name()!= null) {
                workout.setName(updateWorkout.name());
            }

            if (updateWorkout.description()!= null) {
                workout.setDescription(updateWorkout.description());
            }

            if (updateWorkout.workoutTypeId() != null) {
                var workoutType = workoutTypeRepository.findById(updateWorkout.workoutTypeId());

                if (workoutType.isEmpty()) {
                    throw new WorkoutTypeNotFoundException("Workout Type not found");
                }
                workout.setWorkoutType(workoutType.get());
            }

            if (updateWorkout.exerciseIds() != null) {
                
                var exercises = exerciseRepository.findAllById(updateWorkout.exerciseIds()).stream().collect(Collectors.toSet());

                if (exercises.size() != updateWorkout.exerciseIds().size()) {
                    throw new ExerciseNotFoundException("One or more exercises were not found");
                }

                workout.setExercises(exercises);
            }

            workoutRepository.save(workout);
        }else{
            throw new WorkoutNotFoundException("Workout not found");
        }
    }

    public void deleteWorkoutById(String workoutId, String userId){
        
        var workoutExists = workoutRepository.existsByIdAndUser_UserId(UUID.fromString(workoutId), UUID.fromString(userId));

        if (!workoutExists) {
            throw new WorkoutNotFoundException("Workout not found");
        }
        workoutRepository.deleteById(UUID.fromString(workoutId));
    }
}

