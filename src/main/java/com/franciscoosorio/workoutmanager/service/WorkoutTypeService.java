package com.franciscoosorio.workoutmanager.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.franciscoosorio.workoutmanager.domain.workouttype.CreateWorkoutTypeDto;
import com.franciscoosorio.workoutmanager.domain.workouttype.UpdateWorkoutTypeDto;
import com.franciscoosorio.workoutmanager.domain.workouttype.WorkoutType;
import com.franciscoosorio.workoutmanager.exception.WorkoutTypeNotFoundException;
import com.franciscoosorio.workoutmanager.repository.WorkoutTypeRepository;

@Service
public class WorkoutTypeService {
    
    @Autowired
    private WorkoutTypeRepository workoutTypeRepository;

    public WorkoutType createWorkoutType(CreateWorkoutTypeDto requestWorkoutTypeDto) {
        
        var entity = new WorkoutType(requestWorkoutTypeDto.name(),requestWorkoutTypeDto.description());

        return workoutTypeRepository.save(entity);
    }

    public WorkoutType getWorkoutTypeById(String id){
        
        var workoutType = workoutTypeRepository.findById(UUID.fromString(id));

        if (workoutType.isEmpty()) {
            throw new WorkoutTypeNotFoundException("WorkoutType not found");
        }
        return workoutType.get();
    }

    public List<WorkoutType> getAllWorkoutsTypes(){
        
        return workoutTypeRepository.findAll();
    }

    public void deleteWorkoutTypeById(String id) {

        var workoutTypeId = UUID.fromString(id);

        var existsEntity = workoutTypeRepository.existsById(workoutTypeId);

        if (existsEntity) {
            workoutTypeRepository.deleteById(workoutTypeId);
        }else{
            throw new WorkoutTypeNotFoundException("WorkoutType not found");
        }
    }

    public void updateWorkoutTypeById(String id, UpdateWorkoutTypeDto updateWorkoutTypeDto) {

        var workoutTypeId = UUID.fromString(id);

        var workoutTypeEntity = workoutTypeRepository.findById(workoutTypeId);

        if (workoutTypeEntity.isPresent()) {
            
            var entity = workoutTypeEntity.get();

            if (updateWorkoutTypeDto.name() != null) {
                entity.setName(updateWorkoutTypeDto.name());
            }

            if (updateWorkoutTypeDto.description() != null) {
                entity.setDescription(updateWorkoutTypeDto.description());
            }
            workoutTypeRepository.save(entity);
            
        }else{
            throw new WorkoutTypeNotFoundException("WorkoutType not found");
        }
    }
}
