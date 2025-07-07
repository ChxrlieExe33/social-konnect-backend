# SocialKonnect Backend

SocialKonnect is a social media-style application I'm creating to teach myself Spring Boot and Angular.

This repository holds the backend of the application.

The technologies I'm using to build it are the following:

- PostgreSQL
- Spring Data JPA
- Spring Security
- ...

## Notes

I started this project to learn Spring Security. I have now built out a mostly complete authentication workflow.
I am going to build out the backend for a while, then I will create a frontend in Angular for it.

## Main Goals for the Project

- Users can have 0-N posts, which in the beginning will have one photo per post. I will add video support later.
- Each post can have 0-N comments. Comments will be text only.
- Each post can have 0-N likes.
- Will need an endpoint for getting a user with their posts paginated for the "profile" page.
- Possibly store the media for posts and profile pictures in an S3 bucket.
- Add email verification for when a user creates an account.
- More later