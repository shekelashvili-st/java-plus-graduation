# Explore With Me

Explore With Me is a platform for managing events and user interactions, including event creation, participation, and comment management.

## Overview

This project allows users to create, view, and manage events, participate in them, and leave comments. It supports public, private, and admin roles with distinct permissions.

## Contributors:
- [@NikolayDruzhinin](https://github.com/NikolayDruzhinin)
- [@ladyCringe](https://github.com/ladyCringe)
- [@shekelashvili-st](https://github.com/shekelashvili-st)
- [@Dead-Rey](https://github.com/Dead-Rey)

## Features
- **Events**: Create, update, and view events with details and participation tracking.
- **Users**: Manage user profiles and event participation.
- **Comments**: Add, edit, and delete comments with statuses (`CREATED`, `UPDATED`, `DELETED`).
- **Roles**: Public access to view, private user management, and admin controls.

## API Endpoints
- **Public**: `/events`, `/events/{eventId}/comments`
- **Private**: `/users/{userId}/events`, `/users/{userId}/events/{eventId}/comments`
- **Admin**: `/admin/events`, `/admin/comments`
- **Internal**: `/internal/events`, `/internal/requests`, `/internal/users`

## Architecture

The project includes the following modules:
- **Core:** Content management, event publishing, moderation and administrative functions.
- **Statistics:** Collection and analysis of data on user views and interactions.
- **Infrastructure:** Externalized configuration, service discovery and gateway (Spring Cloud).

The core module consists of the following microservices: **Event**, **Request**, **User**, and **Comment**. Spring Cloud Feign is used for interactions between microservices (**Internal** API endpoints).

Configuration files are stored in the configuration server classpath (**config/{module name}/{application name}**).
## External API Endpoints

| Endpoint                                                       | Endpoint                                   |
|----------------------------------------------------------------|--------------------------------------------|
| **Category: Admin**                                            |                                            |
| `POST /admin/categories`                                       | Create new category                        |
| `PATCH /admin/categories/{catId}`                              | Update existing category                   |
| `DELETE /admin/categories/{catId}`                             | Delete existing category                   |
|                                                                |                                            |
| **Category: Public**                                           |                                            |
| `GET /categories`                                              | Get list of all categories                 |
| `GET /categories/{catId}`                                      | Get category info by its ID                |
|                                                                |                                            |
| **Comment: Admin**                                             |                                            |
| `POST /admin/comments/events/{eventId}`                        | Create new comment                         |
| `GET /admin/comments`                                          | Search all comments                        |
| `PATCH /admin/comments/{commentId}`                            | Update existing comment by admin           |
| `DELETE /admin/comments/{commentId}`                           | Delete existing comment by admin           |
|                                                                |                                            |
| **Comment: Private**                                           |                                            |
| `POST /users/{userId}/events/{eventId}/comments`               | Create new comment                         |
| `PATCH /users/{userId}/events/{eventId}/comments/{commentId}`  | Update existing comment                    |
| `DELETE /users/{userId}/events/{eventId}/comments/{commentId}` | Delete existing comment                    |
|                                                                |                                            |
| **Comment: Public**                                            |                                            |
| `GET /events/{eventId}/comments`                               | Search all non deleted comments            |
| `GET /events/{eventId}/comments/{commentId}`                   | Search  comment by ID                      |
|                                                                |                                            |
| **Compilations: Admin**                                        |                                            |
| `POST /admin/compilations`                                     | Create new compilation                     |
| `PATCH /admin/compilations/{compId}`                           | Update existing compilation                |
| `DELETE /admin/compilations/{compId}`                          | Delete existing compilation                |
|                                                                |                                            |
| **Compilations: Public**                                       |                                            |
| `GET /compilations/{compId}`                                   | Get compilation info by its ID             |
| `GET /compilations`                                            | Get list of all compilation by params      |
|                                                                |                                            |
| **Events: Admin**                                              |                                            |
| `GET "/admin/events"`                                          | Search all events                          |
| `PATCH "/admin/events"/{eventId}`                              | Update existing event by admin             |
|                                                                |                                            |
| **Events: Private**                                            |                                            |
| `GET /users/{userId}/events`                                   | Get user events by user ID                 |
| `POST /users/{userId}/events`                                  | Create new event by user ID                |
| `GET /users/{userId}/events/{eventId}`                         | Get user event by user and event ID        |
| `PATCH /users/{userId}/events/{eventId}`                       | Update existing event by user and event ID |
|                                                                |                                            |
| **Events: Public**                                             |                                            |
| `GET /events/`                                                 | Search all public events                   |
| `GET /events/{eventId}/`                                       | Search  published event by event ID        |
|                                                                |                                            |
| **Requests for event: Private**                                |                                            |
| `GET /users/{userId}/events/{eventId}/requests`                | Get participation requests for event       |
| `PATCH /users/{userId}/events/{eventId}/requests`              | Change status of participation request     |
|                                                                |                                            |
| **Requests of users: Private**                                 |                                            |
| `GET /users/{userId}/requests`                                 | Search all requests of user by user ID     |
| `POST /users/{userId}/requests`                                | Add participation request by user ID       |
| `PATCH /users/{userId}/requests/{requestId}/cancel`            | Cancel participation request by user ID    |
|                                                                |                                            |
| **Users: Admin**                                               |                                            |
| `POST /admin/users`                                            | Create new User                            |
| `GET /admin/users`                                             | Get list of all users                      |
| `GET /admin/users/page`                                        | Get users page                             |
| `PATCH /admin/users/{userId}`                                  | Delete existing user                       |
|                                                                |                                            |
