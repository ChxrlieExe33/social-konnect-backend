# Use cases for SocialKonnect

I will implement these over time, this list might also grow.

# ApplicationUser

- Sign up to be a user. **(DONE)**
- Get the user summary (username, bio, pfp). **(DONE)**
- Get the user summary and list of posts (paginated) for the profile page.
- Get a list of all posts created by a user (paginated)
- Update profile data (bio, email, pfp)
- Update username (separate to rest)
- Change password.
- Delete account.

# Post

- Create posts. (With or without media) **(DONE)**
- Update posts (Change caption, add/remove media, etc.) **(Change caption DONE)**
- Retrieve post by ID. **(DONE)**
- Delete posts. **(DONE)**
- Retrieve a paginated list of posts ordered by most recent (for the “explore” page). **(DONE)**
- Do the same as the last one, but for users that the current user follows.

# Comment

- Add new comment to posts. **(DONE)**
- Delete comments. **(DONE)**
- Get count of comments by post-ID. **(DONE)**
- Get all comments of a post paginated **(DONE)**

# Likes

- Add new like to post. **(DONE)**
- Delete likes. **(DONE)**
- Get count of likes by post-ID. **(DONE)**
- Get list of usernames who liked post **(DONE)**

