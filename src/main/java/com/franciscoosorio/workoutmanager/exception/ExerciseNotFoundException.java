package com.franciscoosorio.workoutmanager.exception;

public class ExerciseNotFoundException extends RuntimeException {
    
    public ExerciseNotFoundException(String message){
        super(message);
    }
}
