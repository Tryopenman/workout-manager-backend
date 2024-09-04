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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.franciscoosorio.workoutmanager.domain.exercise.CreateExerciseDto;
import com.franciscoosorio.workoutmanager.domain.exercise.Exercise;
import com.franciscoosorio.workoutmanager.domain.exercise.UpdateExerciseDto;
import com.franciscoosorio.workoutmanager.exception.ExerciseNotFoundException;
import com.franciscoosorio.workoutmanager.repository.ExerciseRepository;

@ExtendWith(MockitoExtension.class)
public class ExerciseServiceTest {
    
    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    @Captor
    private ArgumentCaptor<Exercise> exerciseArgumentCaptor;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Nested
    class CreateExercise{

        @Test
        @DisplayName("Should create a exercise with success")
        void shouldCreateExerciseWithSuccess(){

            var exercise = new Exercise(UUID.randomUUID(),"exercise", "Description for exercise");

            doReturn(exercise).when(exerciseRepository).save(exerciseArgumentCaptor.capture());

            var input = new CreateExerciseDto("exercise", "Description for exercise");

            var output = exerciseService.createExercise(input);

            var exerciseCaptured = exerciseArgumentCaptor.getValue();

            assertNotNull(output);
            assertEquals(output, exercise);
            assertEquals(input.name(), exerciseCaptured.getName());
            assertEquals(input.description(), exerciseCaptured.getDescription());
        }

        @Test
        @DisplayName("Should not create exercise when error occurs")
        void shouldNotCreateExerciseWhenErrorOccurs(){

            doThrow(new RuntimeException()).when(exerciseRepository).save(any());

            var input = new CreateExerciseDto("exercise", "Description for exercise");

            assertThrows(RuntimeException.class, () -> exerciseService.createExercise(input));
        }

        @Test
        @DisplayName("Should not create exercise when name is null")
        void shouldNotCreateExerciseWhenNameIsNull(){

            doThrow(DataIntegrityViolationException.class).when(exerciseRepository).save(any());

            var input = new CreateExerciseDto(null, "Description for exercise");

            assertThrows(DataIntegrityViolationException.class, () -> exerciseService.createExercise(input));
        }

        @Test
        @DisplayName("Should not create exercise when description is null")
        void shouldNotCreateExerciseWhenDescriptionIsNull(){
            doThrow(DataIntegrityViolationException.class).when(exerciseRepository).save(any());

            var input = new CreateExerciseDto("exercise", null);

            assertThrows(DataIntegrityViolationException.class, () -> exerciseService.createExercise(input)); 
        }
        
        @Test
        @DisplayName("Should not create exercise when name already exists")
        void shouldNotCreateExerciseWhenNameAlreadyExists(){

            doThrow(DataIntegrityViolationException.class).when(exerciseRepository).save(any());

            var input = new CreateExerciseDto("existingexercise", "Description for exercise");

            assertThrows(DataIntegrityViolationException.class, () -> exerciseService.createExercise(input));
        }
    }

    @Nested
    class GetExerciseById{

        @Test
        @DisplayName("Should get exercise by id with success when exercise exists")
        void shouldGetExerciseByIdWithSuccessWhenExerciseExists(){

            var exercise = new Exercise(UUID.randomUUID(),"exercise","Description for exercise");
            
            doReturn(Optional.of(exercise)).when(exerciseRepository).findById(uuidArgumentCaptor.capture());
            
            var output = exerciseService.getExerciseById(exercise.getId().toString());

            assertEquals(exercise, output);
            assertEquals(exercise.getId(),uuidArgumentCaptor.getValue());
        }

        @Test
        @DisplayName("Should not get exercise by id with success when exercise not exists")
        void shouldNotGetExerciseByIdWithSuccessWhenExerciseNotExists(){

            var exerciseId = UUID.randomUUID();
            doReturn(Optional.empty()).when(exerciseRepository).findById(uuidArgumentCaptor.capture());

            assertThrows(ExerciseNotFoundException.class, () -> exerciseService.getExerciseById(exerciseId.toString()));
            assertEquals(exerciseId, uuidArgumentCaptor.getValue());
        }
    }

    @Nested
    class GetAllExercises{

        @Test
        @DisplayName("Should get all exercises with success")
        void shouldGetAllExercisesWithSuccess(){

            var exercise = new Exercise(UUID.randomUUID(),"exercise","Description for exercise");

            var exerciseList = List.of(exercise);
            doReturn(exerciseList).when(exerciseRepository).findAll();

            var result = exerciseService.getAllExercises();

            assertNotNull(result);
            assertEquals(result, exerciseList);
            assertEquals(result.size(), exerciseList.size());
        }
    }

    @Nested 
    class UpdateExerciseById{

        @Test
        @DisplayName("Should update exercise by id when exercise exists and name and description are filled")
        void shouldUpdateExerciseByIdWhenExerciseExistsAndNameAndDescriptionAreFilled(){

            var updateExerciseDto = new UpdateExerciseDto("newExercise", "Description for the new exercise");
            
            var exercise = new Exercise(UUID.randomUUID(),"exercise","Description for exercise");

            doReturn(Optional.of(exercise)).when(exerciseRepository).findById(uuidArgumentCaptor.capture());
            doReturn(exercise).when(exerciseRepository).save(exerciseArgumentCaptor.capture());

            exerciseService.updateExerciseById(exercise.getId().toString(), updateExerciseDto);

            assertEquals(exercise.getId(), uuidArgumentCaptor.getValue());

            var exerciseCaptured = exerciseArgumentCaptor.getValue();

            assertEquals(updateExerciseDto.name(), exerciseCaptured.getName());
            assertEquals(updateExerciseDto.description(), exerciseCaptured.getDescription());

            verify(exerciseRepository, times(1)).findById(uuidArgumentCaptor.getValue());
            verify(exerciseRepository,times(1)).save(exerciseCaptured);
        }

        @Test
        @DisplayName("Should not update exercise when exercise not exists")
        void shouldNotUpdateExerciseWhenExerciseNotExists(){

            var updateExerciseDto = new UpdateExerciseDto("newExercise", "Description for the new exercise");
            
            var exerciseId = UUID.randomUUID();

            doReturn(Optional.empty()).when(exerciseRepository).findById(uuidArgumentCaptor.capture());

            assertThrows(ExerciseNotFoundException.class, () -> exerciseService.updateExerciseById(exerciseId.toString(), updateExerciseDto));

            assertEquals(exerciseId, uuidArgumentCaptor.getValue());

            verify(exerciseRepository,times(1)).findById(uuidArgumentCaptor.getValue());
            verify(exerciseRepository,times(0)).save(any());
        }

        @Test
        @DisplayName("Should update exercise when exercise exists and only name is filled")
        void shouldUpdateExerciseWhenExerciseExistsAndOnlyNameIsFilled(){

            var updateExerciseDto = new UpdateExerciseDto("newExercise", null);

            var exercise = new Exercise(UUID.randomUUID(),"exercise","Description for exercise");

            doReturn(Optional.of(exercise)).when(exerciseRepository).findById(uuidArgumentCaptor.capture());
            doReturn(exercise).when(exerciseRepository).save(exerciseArgumentCaptor.capture());

            exerciseService.updateExerciseById(exercise.getId().toString(), updateExerciseDto);

            assertEquals(exercise.getId(), uuidArgumentCaptor.getValue());

            var exerciseCaptured = exerciseArgumentCaptor.getValue();

            assertEquals(updateExerciseDto.name(), exerciseCaptured.getName());
            assertEquals(exercise.getDescription(), exerciseCaptured.getDescription());

            verify(exerciseRepository, times(1)).findById(uuidArgumentCaptor.getValue());
            verify(exerciseRepository,times(1)).save(exercise);
        }

        @Test
        @DisplayName("Should update exercise when exercise exists and only description is filled")
        void shouldUpdateExerciseWhenExerciseExistsAndOnlyNameAreFilled(){

            var updateExerciseDto = new UpdateExerciseDto(null, "New description for exercise");

            var exercise = new Exercise(UUID.randomUUID(),"exercise","Description for exercise");

            doReturn(Optional.of(exercise)).when(exerciseRepository).findById(uuidArgumentCaptor.capture());
            doReturn(exercise).when(exerciseRepository).save(exerciseArgumentCaptor.capture());

            exerciseService.updateExerciseById(exercise.getId().toString(), updateExerciseDto);

            assertEquals(exercise.getId(), uuidArgumentCaptor.getValue());

            var exerciseCaptured = exerciseArgumentCaptor.getValue();

            assertEquals(exercise.getName(), exerciseCaptured.getName());
            assertEquals(updateExerciseDto.description(), exerciseCaptured.getDescription());

            verify(exerciseRepository, times(1)).findById(uuidArgumentCaptor.getValue());
            verify(exerciseRepository,times(1)).save(exercise);
        }

        @Test
        @DisplayName("Should not update exercise when name already exists")
        void shouldNotUpdateExerciseWhenNameAlreadyExists(){

            var updateExerciseDto = new UpdateExerciseDto("existingExercise", "Description for exercise");

            var exercise = new Exercise(UUID.randomUUID(),"exercise","Description for exercise");

            doReturn(Optional.of(exercise)).when(exerciseRepository).findById(uuidArgumentCaptor.capture());

            doThrow(DataIntegrityViolationException.class).when(exerciseRepository).save(any());

            assertThrows(DataIntegrityViolationException.class,() -> exerciseService.updateExerciseById(exercise.getId().toString(), updateExerciseDto));

            assertEquals(exercise.getId(), uuidArgumentCaptor.getValue());

            verify(exerciseRepository, times(1)).findById(uuidArgumentCaptor.getValue());
            verify(exerciseRepository, times(1)).save(any());
        }
    }

    @Nested
    class DeleteExerciseById{

        @Test
        @DisplayName("Should delete exercise by id with sucess when exercise exists")
        void shouldDeleteExerciseByIdWithSuccessWhenExerciseExists(){

            doReturn(true).when(exerciseRepository).existsById(uuidArgumentCaptor.capture());
            doNothing().when(exerciseRepository).deleteById(uuidArgumentCaptor.capture());

            var exerciseId = UUID.randomUUID();

            exerciseService.deleteExerciseById(exerciseId.toString());

            var ids = uuidArgumentCaptor.getAllValues();

            assertEquals(ids.get(0), exerciseId);
            assertEquals(ids.get(1), exerciseId);

            verify(exerciseRepository,times(1)).existsById(ids.get(0));
            verify(exerciseRepository,times(1)).deleteById(ids.get(1));
        }

        @Test
        @DisplayName("Should not delete exercise by id with success when exercise not exists")
        void shouldNotDeleteExerciseByIdWithSuccessWhenExerciseNotExists(){

            doReturn(false).when(exerciseRepository).existsById(uuidArgumentCaptor.capture());

            var exerciseId = UUID.randomUUID();

            assertThrows(ExerciseNotFoundException.class, () -> exerciseService.deleteExerciseById(exerciseId.toString()));

            assertEquals(exerciseId, uuidArgumentCaptor.getValue());

            verify(exerciseRepository,times(1)).existsById(uuidArgumentCaptor.getValue());
            verify(exerciseRepository,times(0)).deleteById(any());
        }
    }
}
