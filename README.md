# SocialKonnect Backend

**SocialKonnect** is a social media-style application I'm creating to teach myself **Spring Boot** and **Angular**.

This repository holds the backend of the application.

The technologies I'm using to build it are the following:

- PostgreSQL
- Spring Data JPA
- Spring Security
- ...

## Main Goals for the Project

- Have full stateless JWT based authentication and authorization.
- Should follow typical social media functionality.
- Users can have posts.
- Posts can have comments and likes.
- Users can customize their profile and update/delete posts.
- Users can follow other users.
- Will have a "for you" page with recommended posts.
- Post media (images / videos) are stored locally currently, but later I want to create an implementation using AWS S3.
- Create a set of routes for administrators, only available to users with the 'admin' role.
- Correct exception handling.
- Protect against common web attacks such as CSRF, SSRF, LFI, RCE, etc.


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

Alternatively, you could **import the project** in your IDE of choice and make sure the JDK is set correctly, then run normally.

## Run it as a docker container

If you simply want to use the application without installing Java, you can run it as a docker container

First build it:

```cmd
docker build . -t social-konnect-backend
```

Then you can run the image as a container, don't forget to add the environment variables which are in the .env.example, like so:

```cmd
docker run -p 8080:8080 social-konnect-backend -e DATABASE_URL=jdbc:postgresql://localhost:5432/your_database -e DATABASE_USER=your_username etc.
```

You could even add it to the dev docker compose file as another service and set the database host env variable to "postgres".
