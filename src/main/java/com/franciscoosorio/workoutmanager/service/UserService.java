package com.franciscoosorio.workoutmanager.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.franciscoosorio.workoutmanager.domain.user.CreateUserDto;
import com.franciscoosorio.workoutmanager.domain.user.UpdateUserDto;
import com.franciscoosorio.workoutmanager.domain.user.User;
import com.franciscoosorio.workoutmanager.exception.UserNotFoundException;
import com.franciscoosorio.workoutmanager.repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User createUser(CreateUserDto createUserDto){
        
        String password = passwordEncoder.encode(createUserDto.password());

        var user = new User(createUserDto.username(),
                            createUserDto.email(),password,
                            Instant.now(),null);

        return userRepository.save(user);
    }

    public User getUserById(String userId){

        var user = userRepository.findById(UUID.fromString(userId));

        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        return user.get();
    }

    public List<User> listUsers(){

        return userRepository.findAll();
    }

    public void updateUserById(String userId, UpdateUserDto updateUserDto){
        var id = UUID.fromString(userId);
        var userEntity = userRepository.findById(id);
        
        if (userEntity.isPresent()) {
            var user = userEntity.get();

            if (updateUserDto.username() != null) {
                user.setUsername(updateUserDto.username());
            }

            if (updateUserDto.email() != null) {
                user.setEmail(updateUserDto.email());
            }

            if (updateUserDto.password() != null) {

                String password = passwordEncoder.encode(updateUserDto.password());
                user.setPassword(password);
            }

            userRepository.save(user);
        }else{
            throw new UserNotFoundException("User not found");
        }
    }

    public void deleteById(String userId){  

        var id = UUID.fromString(userId);
        var userExists = userRepository.existsById(id);

        if (userExists) {
            userRepository.deleteById(id);
        }else{
            throw new UserNotFoundException("User not found");
        }
    }
}
