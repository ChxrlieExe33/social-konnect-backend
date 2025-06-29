# SocialKonnect backend

SocialKonnect is a social media style application I'm creating to teach myself Springboot and Angular.

This repository constitutes the backend of the application.

The technologies I'm using to build it are the following:

- Postgresql
- Spring-data-jpa
- Spring-security
- ...

# Main goals for the project

- Users can have 0-N posts, which in the beginning will have one photo per post, I will add video support later.
- Each post can have 0-N comments, comments will be text only.
- Each post can have 0-N likes.
- Will need an endpoint for getting a user with their posts paginated for the “profile” page.
- Possibly store the media for posts and profile pictures in an S3 bucket.
- Add email validation on register.
- more later.

