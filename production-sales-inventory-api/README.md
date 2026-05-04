# HHJS Production Sales Inventory API

Backend service for the HHJS production, sales and inventory system.

## Stack

- Java 21
- Spring Boot 3
- Spring Security
- JWT
- MySQL 8
- Redis
- Flyway

## Local Services

Start MySQL and Redis from the project root:

```bash
docker compose -f docker-compose.dev.yml up -d
```

## Run API

```bash
cd production-sales-inventory-api
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

## Auth APIs

### Register Admin

Public registration is intended for system bootstrap. The first administrator receives the `SUPER_ADMIN` role; after the system has been initialized, new administrators should be created from the user management module.

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin123456",
  "nickname": "System Admin"
}
```

### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin123456"
}
```

The login response contains `data.accessToken`.

### Current Admin

```http
GET /api/v1/auth/me
Authorization: Bearer <accessToken>
```

### Login Status

```http
GET /api/v1/auth/status
Authorization: Bearer <accessToken>
```

If no valid token is provided, `data.authenticated` is `false`.

### Logout

```http
POST /api/v1/auth/logout
Authorization: Bearer <accessToken>
```

Logout stores the token `jti` in Redis until the JWT expires, so the same token cannot be reused.

## Security Notes

- Use a strong `JWT_SECRET` in production. It must be at least 32 bytes.
- Passwords are stored with BCrypt.
- API sessions are stateless.
- JWT contains `jti`, `roles`, `permissions`, `username`, and `tokenVersion`.
- RBAC data is stored in `role`, `permission`, `admin_user_role`, and `role_permission`.
- Redis is used as a token blacklist for logout.
- MySQL schema is managed by Flyway.
