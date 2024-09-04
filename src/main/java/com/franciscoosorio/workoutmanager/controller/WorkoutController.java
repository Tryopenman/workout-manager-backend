package com.franciscoosorio.workoutmanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.franciscoosorio.workoutmanager.domain.workout.CreateWorkoutDto;
import com.franciscoosorio.workoutmanager.domain.workout.UpdateWorkoutDto;
import com.franciscoosorio.workoutmanager.domain.workout.Workout;
import com.franciscoosorio.workoutmanager.service.WorkoutService;

@RestController
@RequestMapping("/users/{userId}/workouts")
public class WorkoutController {
    
    @Autowired
    private WorkoutService workoutService;

    @PostMapping
    public ResponseEntity<Workout> createWorkout(@PathVariable String userId, @RequestBody CreateWorkoutDto createWorkoutDto){

        var workout = workoutService.createWorkout(userId, createWorkoutDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(workout);
    }

    @GetMapping("/{workoutId}")
    public ResponseEntity<Workout> getWorkoutById(@PathVariable String userId, @PathVariable String workoutId){

        return ResponseEntity.ok(workoutService.getWorkoutByIdAndUserId(workoutId, userId));
    }

    @GetMapping
    public ResponseEntity<List<Workout>> getAllWorkouts(@PathVariable("userId") String userId){
        
        return ResponseEntity.ok(workoutService.getWorkoutsByUserId(userId));
    }

    @PutMapping("/{workoutId}")
    public ResponseEntity<Void> updateWorkout(@PathVariable("userId") String userId, @PathVariable("workoutId") String workoutId, @RequestBody UpdateWorkoutDto updateWorkoutDto){

        workoutService.updateWorkout(workoutId, userId, updateWorkoutDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{workoutId}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable("userId") String userId, @PathVariable("workoutId") String workoutId){

        workoutService.deleteWorkoutById(workoutId, userId);
        return ResponseEntity.ok().build();
    }
}
