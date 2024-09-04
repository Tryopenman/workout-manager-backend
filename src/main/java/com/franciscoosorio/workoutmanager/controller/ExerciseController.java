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

import com.franciscoosorio.workoutmanager.domain.exercise.CreateExerciseDto;
import com.franciscoosorio.workoutmanager.domain.exercise.Exercise;
import com.franciscoosorio.workoutmanager.domain.exercise.UpdateExerciseDto;
import com.franciscoosorio.workoutmanager.service.ExerciseService;

@RestController
@RequestMapping("/exercises")
public class ExerciseController {
    
    @Autowired
    private ExerciseService exerciseService;

    @PostMapping
    public ResponseEntity<Exercise> createExercise(@RequestBody CreateExerciseDto createExerciseDto){

        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.createExercise(createExerciseDto));
    }

    @GetMapping("/{exerciseId}")
    public ResponseEntity<Exercise> getExerciseById(@PathVariable("exerciseId") String id){

        return ResponseEntity.ok(exerciseService.getExerciseById(id));
    }

    @GetMapping
    public ResponseEntity<List<Exercise>> listAllExercises(){
        
        return ResponseEntity.ok(exerciseService.getAllExercises());
    }

    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Exercise> deleteExerciseById(@PathVariable("exerciseId") String id){

        exerciseService.deleteExerciseById(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{exerciseId}")
    public ResponseEntity<Exercise> updateExerciseById(@PathVariable("exerciseId") String id, @RequestBody UpdateExerciseDto updateExerciseDto){

        exerciseService.updateExerciseById(id, updateExerciseDto);

        return ResponseEntity.noContent().build();
    }
}
