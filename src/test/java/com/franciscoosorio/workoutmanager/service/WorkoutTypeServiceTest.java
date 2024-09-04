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

import com.franciscoosorio.workoutmanager.domain.workouttype.CreateWorkoutTypeDto;
import com.franciscoosorio.workoutmanager.domain.workouttype.UpdateWorkoutTypeDto;
import com.franciscoosorio.workoutmanager.domain.workouttype.WorkoutType;
import com.franciscoosorio.workoutmanager.exception.WorkoutTypeNotFoundException;
import com.franciscoosorio.workoutmanager.repository.WorkoutTypeRepository;

@ExtendWith(MockitoExtension.class)
public class WorkoutTypeServiceTest {

    @Mock
    private WorkoutTypeRepository workoutTypeRepository;

    @InjectMocks
    private WorkoutTypeService workoutTypeService;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    private ArgumentCaptor<WorkoutType> workoutTypeArgumentCaptor;


    @Nested
    class CreateWorkoutType{

        @Test
        @DisplayName("Should create a workout type with success")
        void shouldCreateWorkoutTypeWithSuccess(){
            
            var workoutType = new WorkoutType(UUID.randomUUID(), "workout type", "Description for workout type");

            doReturn(workoutType).when(workoutTypeRepository).save(workoutTypeArgumentCaptor.capture());

            var createWorkoutTypeDto = new CreateWorkoutTypeDto("workout type","Description for workout type");

            var output = workoutTypeService.createWorkoutType(createWorkoutTypeDto);

            var workoutTypeCaptured = workoutTypeArgumentCaptor.getValue();

            assertNotNull(output);
            assertEquals(output, workoutType);
            assertEquals(createWorkoutTypeDto.name(), workoutTypeCaptured.getName());
            assertEquals(createWorkoutTypeDto.description(), workoutTypeCaptured.getDescription());
        }

        @Test
        @DisplayName("Should not create workout type when error occurs")
        void shouldNotCreateWorkoutTypeWhenErrorOccurs(){

            doThrow(new RuntimeException()).when(workoutTypeRepository).save(any());

            var createWorkoutTypeDto = new CreateWorkoutTypeDto("workout type","Description for workout type");

            assertThrows(RuntimeException.class,() -> workoutTypeService.createWorkoutType(createWorkoutTypeDto));
        }

        @Test
        @DisplayName("Should not create workout type when name is null")
        void shouldNotCreateWorkoutTypeWhenNameIsNull(){
            
            doThrow(DataIntegrityViolationException.class).when(workoutTypeRepository).save(any());

            var input = new CreateWorkoutTypeDto(null, "Description for workout type");

            assertThrows(DataIntegrityViolationException.class, () -> workoutTypeService.createWorkoutType(input));
        }

        @Test
        @DisplayName("Should not create workout type when description is null")
        void shouldNotCreateWorkoutTypeWhenDescriptionIsNull(){
            
            doThrow(DataIntegrityViolationException.class).when(workoutTypeRepository).save(any());

            var input = new CreateWorkoutTypeDto("workout type", null);

            assertThrows(DataIntegrityViolationException.class, () -> workoutTypeService.createWorkoutType(input));
        }

        @Test
        @DisplayName("Should not create workout type when name already exists")
        void shouldNotCreateWorkoutTypeWhenNameAlreadyExists(){
            
            doThrow(DataIntegrityViolationException.class).when(workoutTypeRepository).save(any());

            var input = new CreateWorkoutTypeDto("existingWorkoutType", "Description for workout type");

            assertThrows(DataIntegrityViolationException.class, () -> workoutTypeService.createWorkoutType(input));
        }
    }

    @Nested
    class GetWorkoutTypeById{

        @Test
        @DisplayName("Should not get workout type by id with success when workout type not exists")
        void shouldNotGetExerciseByIdWithSuccessWhenWorkoutTypeNotExists(){

            var workoutTypeId = UUID.randomUUID();

            doReturn(Optional.empty()).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());

            assertThrows(WorkoutTypeNotFoundException.class,() -> workoutTypeService.getWorkoutTypeById(workoutTypeId.toString()));
            assertEquals(workoutTypeId, uuidArgumentCaptor.getValue());
        }

        @Test
        @DisplayName("Should get workout type by id with success when workout type exists")
        void shouldGetWorkoutTypeByIdWithSuccessWhenWorkoutTypeExists(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "workout type", "Description for workout type");

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            
            var output = workoutTypeService.getWorkoutTypeById(workoutType.getId().toString());

            assertEquals(workoutType.getId(), uuidArgumentCaptor.getValue());
            assertEquals(workoutType, output);
        }
    }

    @Nested
    class GetAllWorkoutsTypes{

        @Test
        @DisplayName("Should get all workouts types with success")
        void shouldGetAllWorkoutsTypesWithSuccess(){

            var workoutType = new WorkoutType(UUID.randomUUID(), "workout type", "Description for workout type");

            var workoutTypeList = List.of(workoutType);
            doReturn(workoutTypeList).when(workoutTypeRepository).findAll();

            var result = workoutTypeService.getAllWorkoutsTypes();

            assertNotNull(result);
            assertEquals(result, workoutTypeList);
            assertEquals(result.size(), workoutTypeList.size());

        }
    }
    
    @Nested
    class UpdateWorkoutTypeById{

        @Test
        @DisplayName("Should update workout type by id when workout type exists and name and description are filled")
        void shouldUpdateWorkoutTypeByIdWhenWorkoutTypeExistsAndNameAndDescriptionAreFilled(){

            var updateWorkoutTypeDto = new UpdateWorkoutTypeDto("New workout type", "Description for new workout type");

            var workoutType = new WorkoutType(UUID.randomUUID(),"workout type","Description for workout type");

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(workoutType).when(workoutTypeRepository).save(workoutTypeArgumentCaptor.capture());

            workoutTypeService.updateWorkoutTypeById(workoutType.getId().toString(), updateWorkoutTypeDto);

            assertEquals(workoutType.getId(), uuidArgumentCaptor.getValue());

            var workoutTypeCaptured = workoutTypeArgumentCaptor.getValue();

            assertEquals(updateWorkoutTypeDto.name(), workoutTypeCaptured.getName());
            assertEquals(updateWorkoutTypeDto.description(), workoutTypeCaptured.getDescription());

            verify(workoutTypeRepository,times(1)).findById(uuidArgumentCaptor.getValue());
            verify(workoutTypeRepository,times(1)).save(workoutType);
        }

        @Test
        @DisplayName("Should not update workout type when workout type not exists")
        void shouldNotUpdateWorkoutTypeWhenWorkoutTypeNotExists(){
            
            var updateWorkoutTypeDto = new UpdateWorkoutTypeDto("workout type", "Description for workout type");

            var workoutTypeId = UUID.randomUUID();

            doReturn(Optional.empty()).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());

            assertThrows(WorkoutTypeNotFoundException.class, () -> workoutTypeService.updateWorkoutTypeById(workoutTypeId.toString(), updateWorkoutTypeDto));

            assertEquals(workoutTypeId, uuidArgumentCaptor.getValue());

            verify(workoutTypeRepository,times(1)).findById(uuidArgumentCaptor.getValue());
            verify(workoutTypeRepository,times(0)).save(any());
        }

        @Test
        @DisplayName("Should update workout type by id when workout type exists and only name is filled")
        void shouldUpdateWorkoutTypeByIdWhenWorkoutTypeExistsAndOnlyNameIsFilled(){

            var updateWorkoutTypeDto = new UpdateWorkoutTypeDto("New workout type", null);

            var workoutType = new WorkoutType(UUID.randomUUID(),"workout type","Description for workout type");

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(workoutType).when(workoutTypeRepository).save(workoutTypeArgumentCaptor.capture());

            workoutTypeService.updateWorkoutTypeById(workoutType.getId().toString(), updateWorkoutTypeDto);

            assertEquals(workoutType.getId(), uuidArgumentCaptor.getValue());

            var workoutTypeCaptured = workoutTypeArgumentCaptor.getValue();
            assertEquals(updateWorkoutTypeDto.name(),workoutTypeCaptured.getName());
            assertEquals(workoutType.getDescription(),workoutTypeCaptured.getDescription());

            verify(workoutTypeRepository,times(1)).findById(uuidArgumentCaptor.getValue());
            verify(workoutTypeRepository,times(1)).save(workoutType);
        }

        @Test
        @DisplayName("Should update workout type by id when workout type exists and only description is filled")
        void shouldUpdateWorkoutTypeByIdWhenWorkoutTypeExistsAndOnlyDescriptionIsFilled(){

            var updateWorkoutTypeDto = new UpdateWorkoutTypeDto(null, "New description for workout type");

            var workoutType = new WorkoutType(UUID.randomUUID(),"workout type","Description for workout type");

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());
            doReturn(workoutType).when(workoutTypeRepository).save(workoutTypeArgumentCaptor.capture());

            workoutTypeService.updateWorkoutTypeById(workoutType.getId().toString(), updateWorkoutTypeDto);

            assertEquals(workoutType.getId(), uuidArgumentCaptor.getValue());

            var workoutTypeCaptured = workoutTypeArgumentCaptor.getValue();
            assertEquals(workoutType.getName(),workoutTypeCaptured.getName());
            assertEquals(updateWorkoutTypeDto.description(),workoutTypeCaptured.getDescription());

            verify(workoutTypeRepository,times(1)).findById(uuidArgumentCaptor.getValue());
            verify(workoutTypeRepository,times(1)).save(workoutType);
        }

        @Test
        @DisplayName("Should not update workout type when name already exists")
        void shouldNotUpdateWorkoutTypeWhenNameAlreadyExists(){

            var updateWorkoutTypeDto = new UpdateWorkoutTypeDto("existingWorkoutType", "Description for workout type");

            var workoutType = new WorkoutType(UUID.randomUUID(),"workout type","Description for workout type");

            doReturn(Optional.of(workoutType)).when(workoutTypeRepository).findById(uuidArgumentCaptor.capture());

            doThrow(DataIntegrityViolationException.class).when(workoutTypeRepository).save(any());

            assertThrows(DataIntegrityViolationException.class, () -> workoutTypeService.updateWorkoutTypeById(workoutType.getId().toString(), updateWorkoutTypeDto));

            verify(workoutTypeRepository, times(1)).findById(uuidArgumentCaptor.getValue());
            verify(workoutTypeRepository, times(1)).save(any());
        }
    }

    @Nested
    class DeleteWorkoutTypeById{

        @Test
        @DisplayName("Should delete workout type by id with success when workout type exists")
        void shouldDeleteWorkoutTypeByIdWithSuccessWhenWorkoutTypeExists(){
            
            doReturn(true).when(workoutTypeRepository).existsById(uuidArgumentCaptor.capture());
            doNothing().when(workoutTypeRepository).deleteById(uuidArgumentCaptor.capture());

            var workoutTypeId = UUID.randomUUID();

            workoutTypeService.deleteWorkoutTypeById(workoutTypeId.toString());

            var ids = uuidArgumentCaptor.getAllValues();

            assertEquals(ids.get(0), workoutTypeId);
            assertEquals(ids.get(1), workoutTypeId);

            verify(workoutTypeRepository,times(1)).existsById(ids.get(0));
            verify(workoutTypeRepository,times(1)).deleteById(ids.get(1));
        }

        @Test
        @DisplayName("Should not delete workout type by id with success when workout type not exists")
        void shouldNotDeleteWorkoutTypeByIdWithSuccessWhenWorkoutTypeNotExists(){
            
            doReturn(false).when(workoutTypeRepository).existsById(uuidArgumentCaptor.capture());

            var workoutTypeId = UUID.randomUUID();

            assertThrows(WorkoutTypeNotFoundException.class, () -> workoutTypeService.deleteWorkoutTypeById(workoutTypeId.toString()));

            assertEquals(workoutTypeId,uuidArgumentCaptor.getValue());

            verify(workoutTypeRepository,times(1)).existsById(uuidArgumentCaptor.getValue());
            verify(workoutTypeRepository,times(0)).deleteById(any());
        }
    }
}
