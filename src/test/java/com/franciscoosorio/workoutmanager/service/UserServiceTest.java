package com.franciscoosorio.workoutmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.franciscoosorio.workoutmanager.domain.user.CreateUserDto;
import com.franciscoosorio.workoutmanager.domain.user.UpdateUserDto;
import com.franciscoosorio.workoutmanager.domain.user.User;
import com.franciscoosorio.workoutmanager.exception.UserNotFoundException;
import com.franciscoosorio.workoutmanager.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Nested
    class CreateUser {
    
        @Test
        @DisplayName("Should create a user with success")
        void shouldCreateUserWithSuccess(){

            String password = "Password1";
            String encryptedPassword = "encryptedPassword1";

            doReturn(encryptedPassword).when(passwordEncoder).encode(stringArgumentCaptor.capture());

            var user = new User(UUID.randomUUID(), "username", "email@email.com", encryptedPassword, Instant.now(), null);

            doReturn(user).when(userRepository).save(userArgumentCaptor.capture());

            var input = new CreateUserDto("username", "email@email.com", password);

            var output = userService.createUser(input);

            var userCaptured = userArgumentCaptor.getValue();

            assertNotNull(output);
            assertEquals(input.email(), userCaptured.getEmail());
            assertEquals(input.username(), userCaptured.getUsername());
            assertEquals(encryptedPassword, userCaptured.getPassword());
            assertEquals(password, stringArgumentCaptor.getValue());
        }

        @Test
        @DisplayName("Should not create user when error occurs")
        void shouldNotCreateUserWhenErrorOccurs(){

            doThrow(new RuntimeException()).when(userRepository).save(any());

            var input = new CreateUserDto("username", "email@email.com", "Password1");

            assertThrows(RuntimeException.class, () -> userService.createUser(input));
        }

        @Test
        @DisplayName("Should not create user when username is null")
        void shouldNotCreateUserWhenUsernameIsNull(){

            doThrow(DataIntegrityViolationException.class).when(userRepository).save(any());

            var input = new CreateUserDto(null, "email@email.com", "Password1");

            assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(input));
        }

        @Test
        @DisplayName("Should not create user when description is null")
        void shouldNotCreateUserWhenDescriptionIsNull(){

            doThrow(DataIntegrityViolationException.class).when(userRepository).save(any());

            var input = new CreateUserDto("username", null, "Password1");

            assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(input));
        }

        @Test
        @DisplayName("Should not create user when password is null")
        void shouldNotCreateUserWhenPasswordIsNull(){

            doThrow(DataIntegrityViolationException.class).when(userRepository).save(any());

            var input = new CreateUserDto("username", "email@email.com", null);

            assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(input));
        }

        @Test
        @DisplayName("Should not create user when username already exist")
        void shouldNotCreateUserWhenUsernameAlreadyExist(){

            doThrow(DataIntegrityViolationException.class).when(userRepository).save(any());

            var input = new CreateUserDto("existingUsername", "email@email.com", "Password1");

            assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(input));
        }

        @Test
        @DisplayName("Should not create user when email already exists")
        void shouldNotCreateUserWhenEmailAlreadyExists(){

            doThrow(DataIntegrityViolationException.class).when(userRepository).save(any());

            var input = new CreateUserDto("username", "existingemail@email.com", "Password1");

            assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(input));
        }
    }

    @Nested
    class GetUserById{

        @Test
        @DisplayName("Should get user by id with success when user exists")
        void shouldGetUserByIdWithSuccessWhenUserExists(){
            
            var user = new User(UUID.randomUUID(), "username", "email@email.com", "encryptedPassword1", Instant.now(), null);

            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());

            var output = userService.getUserById(user.getUserId().toString());
            
            assertEquals(user, output);
            assertEquals(user.getUserId(),uuidArgumentCaptor.getValue());
        }

        @Test
        @DisplayName("Should not get user by id with success when user not exists")
        void shouldNotGetUserByIdWithSuccessWhenUserNotExists(){

            var userId = UUID.randomUUID();
            doReturn(Optional.empty()).when(userRepository).findById(uuidArgumentCaptor.capture());
            
            assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId.toString()));
            assertEquals(userId, uuidArgumentCaptor.getValue());
        }
    }

    @Nested
    class ListUsers{

        @Test
        @DisplayName("Should list users with success")
        void shouldListUsersWithSuccess(){

            var user = new User(UUID.randomUUID(),"username","email@email.com","encryptedPassword1",Instant.now(),null);

            var userList = List.of(user);
            doReturn(userList).when(userRepository).findAll();

            var result = userService.listUsers();
            
            assertNotNull(result);
            assertEquals(result, userList);
            assertEquals(result.size(), userList.size());
        }
    }

    @Nested
    class UpdateUserById {
        
        @Test
        @DisplayName("Should update user by id when user exists and username, password and email are filled")
        void shouldUpdateUserByIdWhenUserExistsAndUsernamePassworAndEmailAreFilled(){

            var updateUserDto = new UpdateUserDto("newUsername", "newPassword", "newemail@email.com");

            var user = new User(UUID.randomUUID(),"username","email@email.com","encryptedPassword1",Instant.now(),null);

            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());

            String encryptedPassword = "encryptedNewPassword";
            doReturn(encryptedPassword).when(passwordEncoder).encode(stringArgumentCaptor.capture());
            
            doReturn(user).when(userRepository).save(userArgumentCaptor.capture());

            userService.updateUserById(user.getUserId().toString(), updateUserDto);

            assertEquals(user.getUserId(), uuidArgumentCaptor.getValue());
            var userCaptured = userArgumentCaptor.getValue();

            assertEquals(updateUserDto.username(), userCaptured.getUsername());
            assertEquals("newPassword", stringArgumentCaptor.getValue());
            assertEquals(encryptedPassword, userCaptured.getPassword());

            verify(userRepository,times(1)).findById(uuidArgumentCaptor.getValue());
            verify(passwordEncoder,times(1)).encode(stringArgumentCaptor.getValue());
            verify(userRepository,times(1)).save(user);
        }

        @Test
        @DisplayName("Should not update user when user not exists")
        void shouldNotUpdateUserWhenUserNotExists(){

            var updateUserDto = new UpdateUserDto("newUsername", "newPassword", "newemail@email.com");

            var userId = UUID.randomUUID();

            doReturn(Optional.empty()).when(userRepository).findById(uuidArgumentCaptor.capture());

            assertThrows(UserNotFoundException.class, () -> userService.updateUserById(userId.toString(), updateUserDto));
            assertEquals(userId, uuidArgumentCaptor.getValue());

            verify(userRepository,times(1)).findById(uuidArgumentCaptor.getValue());
            verify(userRepository,times(0)).save(any());
        }

        @Test
        @DisplayName("Should update user when user exists and only username is filled")
        void shouldUpdateUserWhenUserExistsAndOnlyUsernameIsFilled(){

            var updateUserDto = new UpdateUserDto("newUsername", null, null);

            var user = new User(UUID.randomUUID(),"username","email@email.com","encryptedPassword1",Instant.now(),null);

            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());
            doReturn(user).when(userRepository).save(userArgumentCaptor.capture());

            userService.updateUserById(user.getUserId().toString(), updateUserDto);

            assertEquals(user.getUserId(), uuidArgumentCaptor.getValue());

            var userCaptured = userArgumentCaptor.getValue();

            assertEquals(updateUserDto.username(), userCaptured.getUsername());
            assertEquals(user.getEmail(), userCaptured.getEmail());
            assertEquals(user.getPassword(), userCaptured.getPassword());

            verify(userRepository,times(1)).findById(uuidArgumentCaptor.getValue());
            verify(userRepository,times(1)).save(userArgumentCaptor.getValue());
        }
        
        @Test
        @DisplayName("Should update user when user exists and only password is filled")
        void shouldUpdateUserWhenUserExistsAndOnlyPasswordIsFilled(){

            var updateUserDto = new UpdateUserDto(null, "newPassword", null);

            var user = new User(UUID.randomUUID(),"username","email@email.com", "encryptedPassword1",Instant.now(),null);

            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());

            String encryptedPassword = "encryptedNewPassword";
            doReturn(encryptedPassword).when(passwordEncoder).encode(stringArgumentCaptor.capture());

            doReturn(user).when(userRepository).save(userArgumentCaptor.capture());

            userService.updateUserById(user.getUserId().toString(), updateUserDto);

            assertEquals(user.getUserId(), uuidArgumentCaptor.getValue());

            var userCaptured = userArgumentCaptor.getValue();

            assertEquals(user.getUsername(), userCaptured.getUsername());
            assertEquals("newPassword", stringArgumentCaptor.getValue());
            assertEquals(encryptedPassword, userCaptured.getPassword());
            assertEquals(user.getEmail(), userCaptured.getEmail());

            verify(userRepository,times(1)).findById(uuidArgumentCaptor.getValue());
            verify(passwordEncoder,times(1)).encode(stringArgumentCaptor.getValue());
            verify(userRepository,times(1)).save(userArgumentCaptor.getValue());
        }

        @Test
        @DisplayName("Should update user when user exists and only email is filled")
        void shouldUpdateUserWhenUserExistsAndOnlyEmailIsFilled(){

            var updateUserDto = new UpdateUserDto(null, null, "newemail@email.com");

            var user = new User(UUID.randomUUID(),"username","email@email.com","password",Instant.now(),null);

            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());
            doReturn(user).when(userRepository).save(userArgumentCaptor.capture());

            userService.updateUserById(user.getUserId().toString(), updateUserDto);

            assertEquals(user.getUserId(), uuidArgumentCaptor.getValue());

            var userCaptured = userArgumentCaptor.getValue();

            assertEquals(user.getUsername(), userCaptured.getUsername());
            assertEquals(user.getPassword(), userCaptured.getPassword());
            assertEquals(updateUserDto.email(), userCaptured.getEmail());

            verify(userRepository,times(1)).findById(uuidArgumentCaptor.getValue());
            verify(userRepository,times(1)).save(userArgumentCaptor.getValue());
        }

        @Test
        @DisplayName("Should not update user when username already exists")
        void shouldNotUpdateUserWhenUsernameAlreadyExists() {
            var updateUserDto = new UpdateUserDto("existingUsername", "newPassword", "newemail@email.com");

            var user = new User(UUID.randomUUID(), "username", "email@email.com", "Password1", Instant.now(), null);

            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());

            doThrow(DataIntegrityViolationException.class).when(userRepository).save(any());

            assertThrows(DataIntegrityViolationException.class, () -> userService.updateUserById(user.getUserId().toString(), updateUserDto));

            assertEquals(user.getUserId(), uuidArgumentCaptor.getValue());

            verify(userRepository, times(1)).findById(uuidArgumentCaptor.getValue());
            verify(userRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Should not update user when email already exists")
        void shouldNotUpdateUserWhenEmailAlreadyExists() {
            var updateUserDto = new UpdateUserDto("username", "newPassword", "existingemail@email.com");

            var user = new User(UUID.randomUUID(), "username", "email@email.com", "Password1", Instant.now(), null);

            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());

            doThrow(DataIntegrityViolationException.class).when(userRepository).save(any());

            assertThrows(DataIntegrityViolationException.class, () -> userService.updateUserById(user.getUserId().toString(), updateUserDto));

            assertEquals(user.getUserId(), uuidArgumentCaptor.getValue());

            verify(userRepository, times(1)).findById(uuidArgumentCaptor.getValue());
            verify(userRepository, times(1)).save(any());
        }
    }

    @Nested
    class DeleteById{

        @Test
        @DisplayName("Should delete user by id with success when user exists")
        void shouldDeleteUserByIdWithSuccessWhenUserExists(){
            
            doReturn(true).when(userRepository).existsById(uuidArgumentCaptor.capture());
            doNothing().when(userRepository).deleteById(uuidArgumentCaptor.capture());

            var userId = UUID.randomUUID();

            userService.deleteById(userId.toString());

            var ids = uuidArgumentCaptor.getAllValues();

            assertEquals(userId, ids.get(0));
            assertEquals(userId, ids.get(1));

            verify(userRepository,times(1)).existsById(ids.get(0));
            verify(userRepository,times(1)).deleteById(ids.get(1));
        }

        @Test
        @DisplayName("Should not delete user by id with success when user not exists")
        void shouldNotDeleteUserByIdWithSuccessWhenUserNotExists(){
            
            doReturn(false).when(userRepository).existsById(uuidArgumentCaptor.capture());

            var userId = UUID.randomUUID();

            assertThrows(UserNotFoundException.class, () -> userService.deleteById(userId.toString()));

            var id = uuidArgumentCaptor.getValue();

            assertEquals(userId, id);

            verify(userRepository,times(1)).existsById(uuidArgumentCaptor.getValue());
            verify(userRepository,times(0)).deleteById(any());
        }
    }
}
