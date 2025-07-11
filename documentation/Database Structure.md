# Database structure of SocialKonnect

## Users table

- `id PRIMARY KEY BIGINT` -> Currently BIGINT but will convert to UUID later, easier to debug with BIGINT
- `username VARCHAR UNIQUE` -> Username of user, must be unique, not primary key but used in a lot of operations.
- `bio VARCHAR` -> User biography, short text visible on profile page.
- `email VARCHAR` -> Email of the user for email verification and account recovery / sensitive actions.
- `enabled BOOLEAN` -> Indicates if the account is enabled or not.
- `password VARCHAR` -> Contains the BCrypt hashed password of the user.
- `profile_picture_url VARCHAR` -> Contains the URL of the user's profile picture.
- `created_at TIMESTAMP WITH TIMEZONE` -> An automatically generated timestamp of when the user account was created.

## Roles table

- `id PRIMARY KEY BIGINT` -> Currently BIGINT, could change this to UUID later too, but not as important as changing user Id.
- `authority VARCHAR` -> The name of the role, used by spring security as a SimpleGrantedAuthority.

## Users_roles junction table

Used for the ManyToMany relationship between users and roles

- `user_id BIGINT FOREIGN KEY` -> User involved in the relationship.
- `role_id BIGINT FOREIGN KEY` -> Role involved in the relationship.
- `PRIMARY KEY` -> Complex key composed of previous fields.

## Posts table

- `post_id UUID PRIMARY KEY` -> Uniquely identifies a post.
- `caption VARCHAR` -> Text caption added to post, allowed to contain basic formatting HTML tags only.
- `posted_at TIMESTAMP WITH TIMEZONE` -> An automatically generated timestamp of when the post was created.
- `user_id BIGINT FOREIGN KEY` -> User ID of who created the post.

## Post media table

Contains information about media added to posts.

- `media_id UUID PRIMARY KEY` -> Uniquely identifies a piece of media.
- `media_type VARCHAR` -> Indicates what type of media this is (IMAGE / VIDEO)
- `media_url VARCHAR` -> URL pointing to the piece of media.
- `post_id UUID FOREIGN KEY` -> ID of the associated post, many pieces of media can be associated to one post.

## Comments table

- `comment_id UUID PRIMARY KEY` -> Uniquely identifies a comment.
- `content VARCHAR` -> The text content of the comment, allowed to contain basic formatting HTML tags only.
- `created_at TIMESTAMP WITH TIMEZONE` -> An automatically generated timestamp of when the comment was created.
- `post_id UUID` -> ID of the post this comment was left on.
- `user_id BIGINT` -> ID of the user who left the comment.

## Likes table

- `like_id UUID PRIMARY KEY` -> Uniquely identifies a like.
- `created_at TIMESTAMP WITH TIMEZONE` -> An automatically generated timestamp of when the like was added.
- `post_id UUID` -> ID of the post this like was left on.
- `user_id BIGINT` -> ID of the user who left the like.