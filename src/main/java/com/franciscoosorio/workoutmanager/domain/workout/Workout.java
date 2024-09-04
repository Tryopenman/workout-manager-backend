package com.franciscoosorio.workoutmanager.domain.workout;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.franciscoosorio.workoutmanager.domain.exercise.Exercise;
import com.franciscoosorio.workoutmanager.domain.user.User;
import com.franciscoosorio.workoutmanager.domain.workouttype.WorkoutType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "workouts")
public class Workout {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true)
    private String name;    

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "workoutType_id",nullable = false)
    private WorkoutType workoutType;

    @ManyToMany
    @JoinTable(name = "workouts_exercises", joinColumns = @JoinColumn(name = "workout_id",nullable = false), inverseJoinColumns = @JoinColumn(name = "exercise_id",nullable = false))
    private Set<Exercise> exercises = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonIgnore 
    private User user;

    public Workout(UUID id, String name, String description, WorkoutType workoutType, Set<Exercise> exercises, User user) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.workoutType = workoutType;
        this.exercises = exercises;
        this.user = user;   
    }

    public Workout(String name, String description, WorkoutType workoutType, Set<Exercise> exercises, User user) {
        this.name = name;
        this.description = description;
        this.workoutType = workoutType;
        this.exercises = exercises;
        this.user = user;   
    }

    public Workout() {   
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public WorkoutType getWorkoutType() {
        return workoutType;
    }

    public Set<Exercise> getExercises() {
        return exercises;
    }

    public User getUser() {
        return user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWorkoutType(WorkoutType workoutType) {
        this.workoutType = workoutType;
    }

    public void setExercises(Set<Exercise> exercises) {
        this.exercises = exercises;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Workout workout = (Workout) obj;
        return Objects.equals(id, workout.id);
    }
}
