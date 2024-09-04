package com.franciscoosorio.workoutmanager.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.franciscoosorio.workoutmanager.domain.user.User;

public interface UserRepository extends JpaRepository<User,UUID>{
    
}
