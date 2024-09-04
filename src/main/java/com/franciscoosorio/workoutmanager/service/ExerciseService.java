package com.franciscoosorio.workoutmanager.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.franciscoosorio.workoutmanager.domain.exercise.CreateExerciseDto;
import com.franciscoosorio.workoutmanager.domain.exercise.Exercise;
import com.franciscoosorio.workoutmanager.domain.exercise.UpdateExerciseDto;
import com.franciscoosorio.workoutmanager.exception.ExerciseNotFoundException;
import com.franciscoosorio.workoutmanager.repository.ExerciseRepository;

@Service
public class ExerciseService {
    
    @Autowired
    private ExerciseRepository exerciseRepository;

    public Exercise createExercise(CreateExerciseDto createExerciseDto){
        var entity = new Exercise(createExerciseDto.name(),createExerciseDto.description());

        return exerciseRepository.save(entity);
    }

    public Exercise getExerciseById(String id){

        var exercise = exerciseRepository.findById(UUID.fromString(id));

        if (exercise.isEmpty()) {
            throw new ExerciseNotFoundException("Exercise not found");
        }
        return exercise.get();
    }

    public List<Exercise> getAllExercises(){
        
        return exerciseRepository.findAll();
    }

    public void deleteExerciseById(String id){

        var exerciseId = UUID.fromString(id);

        var existsEntity = exerciseRepository.existsById(exerciseId);

        if (existsEntity) {
            exerciseRepository.deleteById(exerciseId);
        }else{
            throw new ExerciseNotFoundException("Exercise not found");
        }
    }

    public void updateExerciseById(String id, UpdateExerciseDto updateExerciseDto){

        var exerciseId = UUID.fromString(id);

        var exerciseEntity = exerciseRepository.findById(exerciseId);

        if (exerciseEntity.isPresent()) {
            
            var entity = exerciseEntity.get();

            if (updateExerciseDto.name() != null) {
                entity.setName(updateExerciseDto.name());
            }

            if (updateExerciseDto.description() != null) {
                entity.setDescription(updateExerciseDto.description());
            }
            exerciseRepository.save(entity);
        }else{
            throw new ExerciseNotFoundException("Exercise not found");
        }
    }
}
