# HHJS Production Sales Inventory Web

Frontend application for the HHJS production, sales and inventory system.

## Stack

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Custom CSS

No third-party component library is used.

## Run

```bash
npm install
npm run dev
```

The app runs at `http://localhost:5173`.

## Environment

Create `.env.local` when needed:

```bash
VITE_API_BASE_URL=/api/v1
```

During development, Vite proxies `/api` to `http://localhost:8080`.

## Auth Pages

- `/login`：管理员登录。
- `/register`：管理员注册。
- `/dashboard`：登录后的后台首页占位页。

Registration calls `POST /api/v1/auth/register`. After successful registration, the app redirects back to `/login`.
