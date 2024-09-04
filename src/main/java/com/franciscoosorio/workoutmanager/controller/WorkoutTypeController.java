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

import com.franciscoosorio.workoutmanager.domain.workouttype.CreateWorkoutTypeDto;
import com.franciscoosorio.workoutmanager.domain.workouttype.UpdateWorkoutTypeDto;
import com.franciscoosorio.workoutmanager.domain.workouttype.WorkoutType;
import com.franciscoosorio.workoutmanager.service.WorkoutTypeService;

@RestController
@RequestMapping("/workout-type")
public class WorkoutTypeController {
    
    @Autowired
    private WorkoutTypeService workoutTypeService;

    @PostMapping
    public ResponseEntity<WorkoutType> createWorkoutType(@RequestBody CreateWorkoutTypeDto createWorkoutTypeDto){
        
        return ResponseEntity.status(HttpStatus.CREATED).body(workoutTypeService.createWorkoutType(createWorkoutTypeDto));
    }

    @GetMapping("/{workoutTypeId}")
    public ResponseEntity<WorkoutType> getWorkoutTypeById(@PathVariable("workoutTypeId") String id){
        
        return ResponseEntity.ok(workoutTypeService.getWorkoutTypeById(id));
    }

    @GetMapping
    public ResponseEntity<List<WorkoutType>> getAllWorkoutsTypes(){
        
        return ResponseEntity.ok(workoutTypeService.getAllWorkoutsTypes());
    }

    @DeleteMapping("/{workoutTypeId}")
    public ResponseEntity<WorkoutType> deleteWorkoutTypeById(@PathVariable("workoutTypeId") String id){
        
        workoutTypeService.deleteWorkoutTypeById(id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{workoutTypeId}")
    public ResponseEntity<WorkoutType> updateWorkoutTypeById(@PathVariable("workoutTypeId") String id, @RequestBody UpdateWorkoutTypeDto updateWorkoutTypeDto){
        
        workoutTypeService.updateWorkoutTypeById(id,updateWorkoutTypeDto);

        return ResponseEntity.noContent().build();
    }
}
