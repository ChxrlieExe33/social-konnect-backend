<img src="/social-konnect-ui.png" alt="Social Konnect UI" style="margin-top: 10px;">

# SocialKonnect Backend

This is a personal project I have been building out for a couple of months to learn Spring Boot & Angular while creating a reasonably complex application.

It is a Social media style application, like Twitter in some ways, it includes User profiles, posts, comments and likes, as well as the logic for users following each other.

This project is built on Spring Boot version 3.5.3.

During the development of this codebase, I implemented many things such as:

- _Security_
  - Full **JWT authentication** using Spring Security.
  - User registration including email sending for verification.
  - Forgot password functionality, which sends an email to the user.
  - CORS Configuration.
  - Submitted text validation, to **block malicious HTML** payloads, therefore **avoiding XSS**. 
- _Data and performance_
  - DTO Projections for only fetching necessary data in complex JPA queries.
  - **Asynchronous event listeners** for performing tasks in the background to avoid blocking the response to the user.
  - User **following-feed generation** using **fan-out write**, which works better for read-intensive applications like this.
- _Files_
  - File upload functionality for posts including mime type checking with Apache Tika to block malicious file types.
  - File cleanup after posts are deleted.
- _General_
  - Usage of the **DTO Pattern** to only return relevant data to the user.
  - Global **exception handling** for correct error payloads and to **avoid information exposure.**
  - Proper usage of **Inversion of Dependencies** for scalability and testing
  - **Unit tests** using JUnit5 and Mockito for many core functionalities.

## How to run this project locally

Install and **configure JDK24**, either directly in the cmd or through your IDE of choice, also **have docker/docker-compose** ready.

Then **clone the project** to your machine:
````cmd
git clone https://github.com/ChxrlieExe33/social_konnect_backend.git
````

Then enter the project:
```cmd
cd social-konnect-backend
```

After that, you need to **create a .env file**, you can use .env.example in the root as a template, just change out the values to whatever you need.

Now you have to **build and run** the development **docker-compose file** in the root of the project, run:
````cmd
docker-compose up
````

Then finally **run it with maven**, this will install the necessary dependencies.
```cmd
./mvnw spring-boot:run
```

**Alternatively**, you could **import the project** in your IDE of choice and make sure the JDK is set correctly, then run normally.

## Run it as a docker container

If you simply want to use the application without installing Java, you can run it as a docker container.

First build it:

```cmd
docker build . -t social-konnect-backend
```

After that, you can run the image as a container, remember to add the environment variables, which are in the .env.example, like so:

```cmd
docker run -p 8080:8080 social-konnect-backend -e DATABASE_URL=jdbc:postgresql://localhost:5432/your_database -e DATABASE_USER=your_username etc.
```

You could even add it to the dev docker compose file as another service and set the database host env variable to "postgres".
