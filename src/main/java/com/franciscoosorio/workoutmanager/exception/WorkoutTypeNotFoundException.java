package com.franciscoosorio.workoutmanager.exception;

public class WorkoutTypeNotFoundException extends RuntimeException {
    
    public WorkoutTypeNotFoundException(String message){
        super(message);
    }
}
