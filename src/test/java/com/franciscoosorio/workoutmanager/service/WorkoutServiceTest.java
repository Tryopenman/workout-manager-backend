package com.franciscoosorio.workoutmanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

import com.franciscoosorio.workoutmanager.domain.exercise.Exercise;
import com.franciscoosorio.workoutmanager.domain.user.User;
import com.franciscoosorio.workoutmanager.domain.workout.CreateWorkoutDto;
import com.franciscoosorio.workoutmanager.domain.workout.UpdateWorkoutDto;
import com.franciscoosorio.workoutmanager.domain.workout.Workout;
import com.franciscoosorio.workoutmanager.domain.workouttype.WorkoutType;
import com.franciscoosorio.workoutmanager.exception.ExerciseNotFoundException;
import com.franciscoosorio.workoutmanager.exception.UserNotFoundException;
import com.franciscoosorio.workoutmanager.exception.WorkoutNotFoundException;
import com.franciscoosorio.workoutmanager.exception.WorkoutTypeNotFoundException;
import com.franciscoosorio.workoutmanager.repository.ExerciseRepository;
import com.franciscoosorio.workoutmanager.repository.UserRepository;
import com.franciscoosorio.workoutmanager.repository.WorkoutRepository;
import com.franciscoosorio.workoutmanager.repository.WorkoutTypeRepository;

@ExtendWith(MockitoExtension.class)
public class WorkoutServiceTest {
    
    @Mock
    private WorkoutRepository workoutRepository;
    
    @Mock
    private WorkoutTypeRepository workoutTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private WorkoutService workoutService;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    private ArgumentCaptor<Iterable<UUID>> listUuidArgumentCaptor;

    @Captor
    private ArgumentCaptor<Workout> workoutArgumentCaptor;

    @Nested
    class CreateWorkout{

        @Test
        @DisplayName("Should create workout with success when all data is valid")
        void shouldCreateWorkoutWithSuccessWhenAllDataIsValid(){
            
            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var createWorkoutDto = new CreateWorkoutDto("Workout 1","Description for workout 1", workoutType.getId(), Set.of(exercise1.getId(), exercise2.getId()));

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1, exercise2), user);

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(List.of(exercise1, exercise2)).when(exerciseRepository).findAllById(listUuidArgumentCaptor.capture());
            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());
            doReturn(workout).when(workoutRepository).save(workoutArgumentCaptor.capture());

            var result = workoutService.createWorkout(user.getUserId().toString(), createWorkoutDto);

            assertNotNull(result);
            assertEquals(result, workout);

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workoutType.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());

            Set<UUID> capturedlistUUIDs = (Set<UUID>) listUuidArgumentCaptor.getValue();
            assertEquals(2, capturedlistUUIDs.size());
            assertTrue(capturedlistUUIDs.contains(exercise1.getId()));
            assertTrue(capturedlistUUIDs.contains(exercise2.getId()));

            var capturedWorkout = workoutArgumentCaptor.getValue();

            assertEquals(workout.getName(), capturedWorkout.getName());
            assertEquals(workout.getDescription(), capturedWorkout.getDescription());
            assertEquals(workout.getWorkoutType(), capturedWorkout.getWorkoutType());
            assertEquals(workout.getExercises(), capturedWorkout.getExercises());
            assertEquals(workout.getUser(), capturedWorkout.getUser());

            verify(workoutRepository, times(1)).save(capturedWorkout);
        }

        @Test
        @DisplayName("Should not create workout with success when workout type not exist")
        void shouldNotCreateWorkoutWithSuccessWhenWorkoutTypeNotExist(){

            var workoutTypeId = UUID.randomUUID();

            var createWorkoutDto = new CreateWorkoutDto("Workout 1","Description for workout 1", workoutTypeId, Set.of(UUID.randomUUID()));

            doReturn(Optional.empty()).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());

            assertThrows(WorkoutTypeNotFoundException.class, () -> workoutService.createWorkout(UUID.randomUUID().toString(), createWorkoutDto));

            assertEquals(uuidArgumentCaptor.getValue(), workoutTypeId);

            verify(workoutRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("Should not create workout with success when one or more exercises not exists")
        void shouldNotCreateWorkoutWithSuccessWhenOneOrMoreExercisesNotExists(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");

            var exercise2Id = UUID.randomUUID();

            var createWorkoutDto = new CreateWorkoutDto("Workout 1","Description for workout 1", workoutType.getId(), Set.of(exercise1.getId(),exercise2Id));

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(List.of(exercise1)).when(exerciseRepository).findAllById(listUuidArgumentCaptor.capture());

            assertThrows(ExerciseNotFoundException.class, () -> workoutService.createWorkout(UUID.randomUUID().toString(), createWorkoutDto));

            assertEquals(uuidArgumentCaptor.getValue(), workoutType.getId());

            Set<UUID> capturedUUIDs = (Set<UUID>) listUuidArgumentCaptor.getValue();
            assertEquals(2, capturedUUIDs.size());
            assertTrue(capturedUUIDs.contains(exercise1.getId()));
            assertTrue(capturedUUIDs.contains(exercise2Id));
            
            verify(workoutRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("Should not create workout with success when user not exists")
        void shouldNotCreateWorkoutWithSuccessWhenUserNotExists(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var userId = UUID.randomUUID();

            var createWorkoutDto = new CreateWorkoutDto("Workout 1","Description for workout 1", workoutType.getId(), Set.of(exercise1.getId(), exercise2.getId()));

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(List.of(exercise1, exercise2)).when(exerciseRepository).findAllById(listUuidArgumentCaptor.capture());
            doReturn(Optional.empty()).when(userRepository).findById(uuidArgumentCaptor.capture());

            assertThrows(UserNotFoundException.class, () -> workoutService.createWorkout(userId.toString(), createWorkoutDto));

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workoutType.getId());
            assertEquals(capturedUUIDs.get(1), userId);

            Set<UUID> capturedlistUUIDs = (Set<UUID>) listUuidArgumentCaptor.getValue();
            assertEquals(2, capturedlistUUIDs.size());
            assertTrue(capturedlistUUIDs.contains(exercise1.getId()));
            assertTrue(capturedlistUUIDs.contains(exercise2.getId()));
            
            verify(workoutRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("Should not create workout when error occurs")
        void shouldNotCreateWorkoutWhenErrorOccurs(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var createWorkoutDto = new CreateWorkoutDto("Workout 1","Description for workout 1", workoutType.getId(), Set.of(exercise1.getId(), exercise2.getId()));

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(List.of(exercise1, exercise2)).when(exerciseRepository).findAllById(listUuidArgumentCaptor.capture());
            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());            
            doThrow(new RuntimeException()).when(workoutRepository).save(any());

            assertThrows(RuntimeException.class, () -> workoutService.createWorkout(user.getUserId().toString(),createWorkoutDto));

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workoutType.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());

            Set<UUID> capturedlistUUIDs = (Set<UUID>) listUuidArgumentCaptor.getValue();
            assertEquals(2, capturedlistUUIDs.size());
            assertTrue(capturedlistUUIDs.contains(exercise1.getId()));
            assertTrue(capturedlistUUIDs.contains(exercise2.getId()));
        }

        @Test
        @DisplayName("Should not create workout when name is null")
        void shouldNotCreateWorkoutWhenNameIsNull(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var createWorkoutDto = new CreateWorkoutDto(null,"Description for workout 1", workoutType.getId(), Set.of(exercise1.getId(), exercise2.getId()));

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(List.of(exercise1, exercise2)).when(exerciseRepository).findAllById(listUuidArgumentCaptor.capture());
            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());            
            doThrow(DataIntegrityViolationException.class).when(workoutRepository).save(any());

            assertThrows(DataIntegrityViolationException.class, () -> workoutService.createWorkout(user.getUserId().toString(),createWorkoutDto));

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workoutType.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());

            Set<UUID> capturedlistUUIDs = (Set<UUID>) listUuidArgumentCaptor.getValue();
            assertEquals(2, capturedlistUUIDs.size());
            assertTrue(capturedlistUUIDs.contains(exercise1.getId()));
            assertTrue(capturedlistUUIDs.contains(exercise2.getId()));
        }

        @Test
        @DisplayName("Should not create workout when description is null")
        void shouldNotCreateWorkoutWhenDescriptionIsNull(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var createWorkoutDto = new CreateWorkoutDto("Workout 1",null, workoutType.getId(), Set.of(exercise1.getId(), exercise2.getId()));

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(List.of(exercise1, exercise2)).when(exerciseRepository).findAllById(listUuidArgumentCaptor.capture());
            doReturn(Optional.of(user)).when(userRepository).findById(uuidArgumentCaptor.capture());            
            doThrow(DataIntegrityViolationException.class).when(workoutRepository).save(any());

            assertThrows(DataIntegrityViolationException.class, () -> workoutService.createWorkout(user.getUserId().toString(),createWorkoutDto));

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workoutType.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());

            Set<UUID> capturedlistUUIDs = (Set<UUID>) listUuidArgumentCaptor.getValue();
            assertEquals(2, capturedlistUUIDs.size());
            assertTrue(capturedlistUUIDs.contains(exercise1.getId()));
            assertTrue(capturedlistUUIDs.contains(exercise2.getId()));
        }

        @Test
        @DisplayName("Should not create workout when exercise ids are null")
        void shouldNotCreateWorkoutWhenExerciseIdsAreNull(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var createWorkoutDto = new CreateWorkoutDto("Workout 1",null, workoutType.getId(), null);

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            assertThrows(IllegalArgumentException.class, () -> workoutService.createWorkout(user.getUserId().toString(), createWorkoutDto));

            assertEquals(workoutType.getId(), uuidArgumentCaptor.getValue());
            verify(workoutRepository, times(0)).save(any());
        }
    }

    @Nested
    class GetWorkoutByIdAndUserId{

        @Test
        @DisplayName("Should get workout by id and user id with success when workout exists")
        void shouldGetWorkoutByIdAndUserIdWithSuccessWhenWorkoutExists(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1, exercise2), user);

            doReturn(true).when(userRepository).existsById(uuidArgumentCaptor.capture());
            doReturn(Optional.of(workout)).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(),uuidArgumentCaptor.capture());

            var output = workoutService.getWorkoutByIdAndUserId(workout.getId().toString(), user.getUserId().toString());

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), user.getUserId());
            assertEquals(capturedUUIDs.get(1), workout.getId());
            assertEquals(capturedUUIDs.get(2), user.getUserId());
            assertEquals(workout, output);

            verify(userRepository,times(1)).existsById(user.getUserId());
            verify(workoutRepository,times(1)).findByIdAndUser_UserId(workout.getId(), user.getUserId());
        }

        @Test
        @DisplayName("Should not get workout by id and user id with success when workout not exists")
        void shouldNotGetWorkoutByIdAndUserIdWithSuccessWhenWorkoutNotExists(){
            
            var workoutId = UUID.randomUUID();
            var userId = UUID.randomUUID();

            doReturn(true).when(userRepository).existsById(uuidArgumentCaptor.capture());
            doReturn(Optional.empty()).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(),uuidArgumentCaptor.capture());

            assertThrows(WorkoutNotFoundException.class, () -> workoutService.getWorkoutByIdAndUserId(workoutId.toString(),userId.toString()));
            
            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), userId);
            assertEquals(capturedUUIDs.get(1), workoutId);
            assertEquals(capturedUUIDs.get(2), userId);

            verify(userRepository,times(1)).existsById(userId);
            verify(workoutRepository,times(1)).findByIdAndUser_UserId(workoutId, userId);
        }

        @Test
        @DisplayName("Should not get workout by id and user id with success when user not exists")
        void shouldNotGetWorkoutByIdAndUserIdWithSuccessWhenUserNotExists(){

            var workoutId = UUID.randomUUID();
            var userId = UUID.randomUUID();

            doReturn(false).when(userRepository).existsById(uuidArgumentCaptor.capture());
            
            assertThrows(UserNotFoundException.class, () -> workoutService.getWorkoutByIdAndUserId(workoutId.toString(),userId.toString()));

            assertEquals(uuidArgumentCaptor.getValue(), userId);
            verify(userRepository,times(1)).existsById(userId);
            verify(workoutRepository,times(0)).findByIdAndUser_UserId(workoutId, userId);
        }
    }

    @Nested
    class GetWorkoutsByUserId{

        @Test
        @DisplayName("Should get workouts by user id with success when user exists")
        void shouldGetAllExercisesWithSuccessWhenUserExists(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1, exercise2), user);

            doReturn(true).when(userRepository).existsById(uuidArgumentCaptor.capture());
            var workoutList = List.of(workout);
            doReturn(workoutList).when(workoutRepository).findByUser_UserId(uuidArgumentCaptor.capture());

            var result = workoutService.getWorkoutsByUserId(user.getUserId().toString());

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), user.getUserId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());

            assertNotNull(result);
            assertEquals(result, workoutList);
            assertEquals(result.size(), workoutList.size());

            verify(userRepository,times(1)).existsById(user.getUserId());
            verify(workoutRepository,times(1)).findByUser_UserId(user.getUserId());
        }

        @Test
        @DisplayName("Should not get workouts by user id with success when user not exists")
        void shouldNotGetAllExercisesWithSuccessWhenUserNotExists(){

            var userId = UUID.randomUUID();

            doReturn(false).when(userRepository).existsById(uuidArgumentCaptor.capture());

            assertThrows(UserNotFoundException.class, () -> workoutService.getWorkoutsByUserId(userId.toString()));

            assertEquals(uuidArgumentCaptor.getValue(), userId);

            verify(userRepository,times(1)).existsById(userId);
            verify(workoutRepository,times(0)).findByUser_UserId(userId);
        }
    }

    @Nested
    class UpdateWorkout{
        
        @Test
        @DisplayName("Should update workout with success when all data is valid")
        void shouldUpdateWorkoutWithSuccessWhenAllDataIsValid(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var newWorkoutType = new WorkoutType(UUID.randomUUID(), "Workout type 2", "Description for workout type 2");

            var updateWorkoutDto = new UpdateWorkoutDto("New Workout","Description for new workout", newWorkoutType.getId(), Set.of(exercise2.getId()));

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1), user);

            doReturn(Optional.of(workout)).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            doReturn(Optional.of(newWorkoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(List.of(exercise2)).when(exerciseRepository).findAllById(listUuidArgumentCaptor.capture());
            doReturn(workout).when(workoutRepository).save(workoutArgumentCaptor.capture());
            
            workoutService.updateWorkout(workout.getId().toString(),user.getUserId().toString(),updateWorkoutDto);

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workout.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());
            assertEquals(capturedUUIDs.get(2), newWorkoutType.getId());

            Set<UUID> capturedlistUUIDs = (Set<UUID>) listUuidArgumentCaptor.getValue();
            assertEquals(1, capturedlistUUIDs.size());
            assertTrue(capturedlistUUIDs.contains(exercise2.getId()));

            var capturedWorkout = workoutArgumentCaptor.getValue();

            assertEquals(workout,capturedWorkout);

            assertEquals(updateWorkoutDto.name(), capturedWorkout.getName());
            assertEquals(updateWorkoutDto.description(), capturedWorkout.getDescription());
            assertEquals(newWorkoutType, capturedWorkout.getWorkoutType());
            assertEquals(Set.of(exercise2), capturedWorkout.getExercises());

            verify(workoutRepository, times(1)).findByIdAndUser_UserId(workout.getId(),user.getUserId());
            verify(workoutTypeRepository, times(1)).findById(newWorkoutType.getId());
            verify(exerciseRepository, times(1)).findAllById(updateWorkoutDto.exerciseIds());
            verify(workoutRepository, times(1)).save(workout);
        }

        @Test
        @DisplayName("Should not update workout with success when workout not exists for this user")
        void shouldNotUpdateWorkoutWithSuccessWhenWorkoutNotExists(){

            var workoutId = UUID.randomUUID();
            var userId = UUID.randomUUID();

            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var newWorkoutType = new WorkoutType(UUID.randomUUID(), "Workout type 2", "Description for workout type 2");

            var updateWorkoutDto = new UpdateWorkoutDto("New Workout","Description for new workout", newWorkoutType.getId(), Set.of(exercise2.getId()));

            doReturn(Optional.empty()).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());

            assertThrows(WorkoutNotFoundException.class, () -> workoutService.updateWorkout(workoutId.toString(), userId.toString(), updateWorkoutDto));

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workoutId);
            assertEquals(capturedUUIDs.get(1), userId);

            verify(workoutRepository, times(1)).findByIdAndUser_UserId(workoutId,userId);
            verify(workoutTypeRepository, times(0)).findById(any());
            verify(exerciseRepository, times(0)).findAllById(any());
            verify(workoutRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("Should not update workout with success when workout type not exists")
        void shouldNotUpdateWorkoutWithSuccessWhenWorkoutTypeNotExists(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var newWorkoutType = new WorkoutType(UUID.randomUUID(), "Workout type 2", "Description for workout type 2");

            var updateWorkoutDto = new UpdateWorkoutDto("New Workout","Description for new workout", newWorkoutType.getId(), Set.of(exercise2.getId()));

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1), user);

            doReturn(Optional.of(workout)).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            doReturn(Optional.empty()).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());

            assertThrows(WorkoutTypeNotFoundException.class, () -> workoutService.updateWorkout(workout.getId().toString(), user.getUserId().toString(), updateWorkoutDto));

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workout.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());
            assertEquals(capturedUUIDs.get(2), newWorkoutType.getId());

            verify(workoutRepository, times(1)).findByIdAndUser_UserId(workout.getId(),user.getUserId());
            verify(workoutTypeRepository, times(1)).findById(newWorkoutType.getId());
            verify(exerciseRepository, times(0)).findAllById(any());
            verify(workoutRepository, times(0)).save(any());

        }

        @Test
        @DisplayName("Should not update workout with success when one or more exercises not exists")
        void shouldNotUpdateWithSuccessWhenOneOrMoreExercisesNotExists(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var newWorkoutType = new WorkoutType(UUID.randomUUID(), "Workout type 2", "Description for workout type 2");

            var updateWorkoutDto = new UpdateWorkoutDto("New Workout","Description for new workout", newWorkoutType.getId(), Set.of(exercise2.getId()));

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1), user);

            doReturn(Optional.of(workout)).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            doReturn(Optional.of(newWorkoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(List.of()).when(exerciseRepository).findAllById(listUuidArgumentCaptor.capture());

            assertThrows(ExerciseNotFoundException.class, () -> workoutService.updateWorkout(workout.getId().toString(), user.getUserId().toString(), updateWorkoutDto));

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workout.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());
            assertEquals(capturedUUIDs.get(2), newWorkoutType.getId());

            Set<UUID> capturedlistUUIDs = (Set<UUID>) listUuidArgumentCaptor.getValue();
            assertEquals(1, capturedlistUUIDs.size());
            assertTrue(capturedlistUUIDs.contains(exercise2.getId()));

            verify(workoutRepository, times(1)).findByIdAndUser_UserId(workout.getId(),user.getUserId());
            verify(workoutTypeRepository, times(1)).findById(newWorkoutType.getId());
            verify(exerciseRepository, times(1)).findAllById(updateWorkoutDto.exerciseIds());
            verify(workoutRepository, times(0)).save(any());
        }

        @Test
        @DisplayName("Should update workout with success when only name is filled")
        void shouldUpdateWorkoutWithSuccessWhenOnlyNameIsFilled(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var updateWorkoutDto = new UpdateWorkoutDto("New Workout",null, null, null);

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1), user);

            doReturn(Optional.of(workout)).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            doReturn(workout).when(workoutRepository).save(workoutArgumentCaptor.capture());

            workoutService.updateWorkout(workout.getId().toString(), user.getUserId().toString(), updateWorkoutDto);

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workout.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());

            var capturedWorkout = workoutArgumentCaptor.getValue();

            assertEquals(updateWorkoutDto.name(), capturedWorkout.getName());
            assertEquals(workout.getDescription(), capturedWorkout.getDescription());
            assertEquals(workout.getWorkoutType(), capturedWorkout.getWorkoutType());
            assertEquals(workout.getExercises(), capturedWorkout.getExercises());

            verify(workoutRepository, times(1)).findByIdAndUser_UserId(workout.getId(),user.getUserId());
            verify(workoutTypeRepository, times(0)).findById(any());
            verify(exerciseRepository, times(0)).findAllById(any());
            verify(workoutRepository, times(1)).save(workout);
        }

        @Test
        @DisplayName("Should update workout with success when only descritpion is filled")
        void shouldUpdateWorkoutWithSuccessWhenOnlyDescriptionIsFilled(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var updateWorkoutDto = new UpdateWorkoutDto(null,"Description for new workout", null, null);

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1), user);

            doReturn(Optional.of(workout)).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            doReturn(workout).when(workoutRepository).save(workoutArgumentCaptor.capture());

            workoutService.updateWorkout(workout.getId().toString(), user.getUserId().toString(), updateWorkoutDto);

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workout.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());

            var capturedWorkout = workoutArgumentCaptor.getValue();

            assertEquals(workout.getName(), capturedWorkout.getName());
            assertEquals(updateWorkoutDto.description(), capturedWorkout.getDescription());
            assertEquals(workout.getWorkoutType(), capturedWorkout.getWorkoutType());
            assertEquals(workout.getExercises(), capturedWorkout.getExercises());

            verify(workoutRepository, times(1)).findByIdAndUser_UserId(workout.getId(),user.getUserId());
            verify(workoutTypeRepository, times(0)).findById(any());
            verify(exerciseRepository, times(0)).findAllById(any());
            verify(workoutRepository, times(1)).save(workout);
        }

        @Test
        @DisplayName("Should update workout with success when only workout type is filled")
        void shouldUpdateWorkoutWithSuccessWhenOnlyWorkoutTypeIsFilled(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var newWorkoutType = new WorkoutType(UUID.randomUUID(), "Workout type 2", "Description for workout type 2");

            var updateWorkoutDto = new UpdateWorkoutDto(null,null, newWorkoutType.getId(), null);

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1), user);

            doReturn(Optional.of(workout)).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            doReturn(Optional.of(newWorkoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(workout).when(workoutRepository).save(workoutArgumentCaptor.capture());

            workoutService.updateWorkout(workout.getId().toString(), user.getUserId().toString(), updateWorkoutDto);

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workout.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());
            assertEquals(capturedUUIDs.get(2), newWorkoutType.getId());

            var capturedWorkout = workoutArgumentCaptor.getValue();

            assertEquals(workout.getName(), capturedWorkout.getName());
            assertEquals(workout.getDescription(), capturedWorkout.getDescription());
            assertEquals(updateWorkoutDto.workoutTypeId(), capturedWorkout.getWorkoutType().getId());
            assertEquals(workout.getExercises(), capturedWorkout.getExercises());

            verify(workoutRepository, times(1)).findByIdAndUser_UserId(workout.getId(),user.getUserId());
            verify(workoutTypeRepository, times(1)).findById(newWorkoutType.getId());
            verify(exerciseRepository, times(0)).findAllById(any());
            verify(workoutRepository, times(1)).save(workout);
        }

        @Test
        @DisplayName("Should update workout with success when only exercises are filled")
        void shouldUpdateWorkoutWithSuccessWhenOnlyExercisesAreFilled(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var updateWorkoutDto = new UpdateWorkoutDto(null,null, null, Set.of(exercise2.getId()));

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1), user);

            doReturn(Optional.of(workout)).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            doReturn(List.of(exercise2)).when(exerciseRepository).findAllById(listUuidArgumentCaptor.capture());
            doReturn(workout).when(workoutRepository).save(workoutArgumentCaptor.capture());

            workoutService.updateWorkout(workout.getId().toString(), user.getUserId().toString(), updateWorkoutDto);

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workout.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());

            Set<UUID> capturedlistUUIDs = (Set<UUID>) listUuidArgumentCaptor.getValue();
            assertEquals(1, capturedlistUUIDs.size());
            assertTrue(capturedlistUUIDs.contains(exercise2.getId()));

            var capturedWorkout = workoutArgumentCaptor.getValue();

            assertEquals(workout.getName(), capturedWorkout.getName());
            assertEquals(workout.getDescription(), capturedWorkout.getDescription());
            assertEquals(workout.getWorkoutType(), capturedWorkout.getWorkoutType());
            assertEquals(updateWorkoutDto.exerciseIds().size(), capturedWorkout.getExercises().size());
            assertTrue(updateWorkoutDto.exerciseIds().contains(exercise2.getId()));

            verify(workoutRepository, times(1)).findByIdAndUser_UserId(workout.getId(),user.getUserId());
            verify(workoutTypeRepository, times(0)).findById(any());
            verify(exerciseRepository, times(1)).findAllById(updateWorkoutDto.exerciseIds());
            verify(workoutRepository, times(1)).save(workout);
        }

        @Test
        @DisplayName("Should not update workout with success when name already exists")
        void shouldNotUpdateWorkoutWithSuccessWhenNameAlreadyExists(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "Workout type 1", "Description for workout type 1");

            var exercise1 = new Exercise(UUID.randomUUID(),"Exercise 1", "Description for exercise 1");
            var exercise2 = new Exercise(UUID.randomUUID(),"Exercise 2", "Description for exercise 2");

            var user = new User(UUID.randomUUID(), "username", "email@example.com", "password", Instant.now(), null);

            var updateWorkoutDto = new UpdateWorkoutDto("existingWorkout",null, null, Set.of(exercise2.getId()));

            var workout = new Workout(UUID.randomUUID(),"Workout 1","Description for workout 1",workoutType,Set.of(exercise1), user);

            doReturn(Optional.of(workout)).when(workoutRepository).findByIdAndUser_UserId(uuidArgumentCaptor.capture(), uuidArgumentCaptor.capture());
            doReturn(List.of(exercise2)).when(exerciseRepository).findAllById(listUuidArgumentCaptor.capture());
            doThrow(DataIntegrityViolationException.class).when(workoutRepository).save(any());
            
            assertThrows(DataIntegrityViolationException.class,() -> workoutService.updateWorkout(workout.getId().toString(), user.getUserId().toString(), updateWorkoutDto));

            List<UUID> capturedUUIDs = uuidArgumentCaptor.getAllValues();

            assertEquals(capturedUUIDs.get(0), workout.getId());
            assertEquals(capturedUUIDs.get(1), user.getUserId());

            Set<UUID> capturedlistUUIDs = (Set<UUID>) listUuidArgumentCaptor.getValue();
            assertEquals(1, capturedlistUUIDs.size());
            assertTrue(capturedlistUUIDs.contains(exercise2.getId()));

            verify(workoutRepository, times(1)).findByIdAndUser_UserId(workout.getId(),user.getUserId());
            verify(workoutTypeRepository, times(0)).findById(any());
            verify(exerciseRepository, times(1)).findAllById(updateWorkoutDto.exerciseIds());
            verify(workoutRepository, times(1)).save(workout);
        }
    }

    @Nested
    class DeleteWorkoutById{

        @Test
        @DisplayName("Should delete workout by id with success when workout exists for this user")
        void shouldDeleteWorkoutByIdWithSuccessWhenWorkoutExistsForThisUser(){
            
            var workoutId = UUID.randomUUID();
            var userId = UUID.randomUUID();

            doReturn(true).when(workoutRepository).existsByIdAndUser_UserId(uuidArgumentCaptor.capture(),uuidArgumentCaptor.capture());
            doNothing().when(workoutRepository).deleteById(workoutId);

            workoutService.deleteWorkoutById(workoutId.toString(), userId.toString());

            var ids = uuidArgumentCaptor.getAllValues();

            assertEquals(ids.get(0), workoutId);
            assertEquals(ids.get(1), userId);
            
            verify(workoutRepository,times(1)).existsByIdAndUser_UserId(ids.get(0),ids.get(1));
            verify(workoutRepository,times(1)).deleteById(ids.get(0));
        }

        @Test
        @DisplayName("Should not delete workout by id with success when workout not exists for this user")
        void shouldNotDeleteWorkoutByIdWithSuccessWhenWorkoutNotExistsForThisUser(){
            
            var workoutId = UUID.randomUUID();
            var userId = UUID.randomUUID();

            doReturn(false).when(workoutRepository).existsByIdAndUser_UserId(uuidArgumentCaptor.capture(),uuidArgumentCaptor.capture());

            assertThrows(WorkoutNotFoundException.class, () -> workoutService.deleteWorkoutById(workoutId.toString(), userId.toString()));

            var ids = uuidArgumentCaptor.getAllValues();

            assertEquals(ids.get(0), workoutId);
            assertEquals(ids.get(1), userId);

            verify(workoutRepository,times(1)).existsByIdAndUser_UserId(workoutId,userId);
            verify(workoutRepository,times(0)).deleteById(any());
        }
    }
}
