<h1 align = "center">👨‍💻Workout Manager Backend</h1>

<p>
  <b>This is the backend project that allows you to manage workouts, exercises, workout types and users.
  </b>
</p>
<p>
  <b>It is important to highlight that security and authentication features are not included, as the focus of this project was to improve Spring Boot skills, implement unit tests and demonstrate basic functionalities.</b>
</p>
<p>
  <b>In the future, security features may be added, such as authentication and authorization, and a persistent database.</b>
</p>

<p>
  • <a href="#started">Getting Started</a> <br>
  • <a href="#technologies">Technologies</a> <br>
  • <a href="#endpoints">Endpoints</a> <br>
  • <a href="#license">License</a> <br>
</p>

<h2 id="started">🚀 Getting started</h2>

1. Clone the repository:

```bash
$ git clone https://github.com/Tryopenman/workout-manager-backend.git
```

2. Install dependencies with Maven

```bash
  $ cd workout-manager-backend
  $ mvn install
```

3. Start the application with Maven

 ```bash
  $ mvn spring-boot:run
```

<h2 id="technologies">Technologies</h2>

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)

![jUnit 5](https://img.shields.io/badge/jUnit%205-%23F2C300.svg?style=for-the-badge&logo=junit&logoColor=black)

![H2](https://img.shields.io/badge/H2-%230f4c81.svg?style=for-the-badge&logo=h2&logoColor=white)

<h2 id="endpoints">📌Endpoints</h2>

### [Users](requests/userRequests.http)

- <kbd>POST /users</kbd>
  - **Description**: Create a new user. 

- <kbd>GET /users/{userId}</kbd>
  - **Description**: Get user by id.

- <kbd>PUT /users/{userId}</kbd>
  - **Description**: Update user by id.

- <kbd>DELETE /users/{userId}</kbd>
  - **Description**: Delete a user by id.

### [Exercises](requests/exerciseRequests.http)

- <kbd>POST /exercises</kbd>
  - **Description**: Create a new exercise.

- <kbd>GET /exercises/{exerciseId}</kbd>
  - **Description**: Get exercise by id.

- <kbd>PUT /exercises/{exerciseId}</kbd>
  - **Description**: Update exercise by id.

- <kbd>DELETE /exercises/{exerciseId}</kbd>
  - **Description**: Delete an exercise by id.

### [Workout Types](requests/workoutTypeRequests.http)

- <kbd>POST /workout-type</kbd>
  - **Description**: Create a new workout type.

- <kbd>GET /workout-type/{workoutTypeId}</kbd>
  - **Description**: Get workout type by id.

- <kbd>PUT /workout-type/{workoutTypeId}</kbd>
  - **Description**: Update workout type by id.

- <kbd>DELETE /workout-type/{workoutTypeId}</kbd>
  - **Description**: Delete a workout type by id.

### [Workouts](requests/worjoutRequests.http)

- <kbd>POST /users/{userId}/workouts</kbd>
  - **Description**: Create a new workout.

- <kbd>GET /users/{userId}/workouts/{workoutId}</kbd>
  - **Description**: Get workout by id.

- <kbd>PUT /users/{userId}/workouts/{workoutId}</kbd>
  - **Description**: Update workout by id.

- <kbd>DELETE /users/{userId}/workouts/{workoutId}</kbd>
  - **Description**: Delete a workout by id.

<h2 id="license">📝License</h2>
<p>
  <b>All rights reserved.</b>
</p>
