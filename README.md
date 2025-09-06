# 🧾 VisTrack – Visitor Tracking & Analytics Platform

**VisTrack** is a full-stack application designed to efficiently manage visitor interactions within an organization. The system supports **visitor registration**, **check-in/check-out tracking**, **service logging**, and **payment recording** — including **OCR-based receipt parsing**. An **Admin Dashboard** visualizes aggregate metrics such as total revenue, number of visitors, and service utilization.

---

## 📌 Table of Contents

1. [Project Features](#project-features)
2. [System Architecture](#system-architecture)
3. [Tech Stack](#tech-stack)
4. [Frontend (React)](#frontend-react)
5. [Backend (Spring Boot)](#backend-spring-boot)
6. [Google Vision API Integration](#google-vision-api-integration)
7. [Authentication & Authorization](#authentication--authorization)
8. [Database Schema Overview](#database-schema-overview)
9. [Deployment Setup](#deployment-setup)
10. [Contributors](#contributors)

---

## 🎯 Project Features

### Receptionist Panel
- Register visitors with name, phone number, and address.
- Check-in and check-out visitors using phone number.
- Add services used by a visitor (Consultation, Surgery, etc.).
- Upload payments:
    - Via form input.
    - Via image receipt using **Google OCR**.

### Admin Dashboard
- Restricted to admin login only.
- Visual analytics of:
    - Total revenue by day, week, and month.
    - Total check-ins and check-outs.
    - Filter by category (Consultation, Medicine, Surgery, etc.)
    - Lookup history of a specific visitor by name or phone number.

---

## 🧱 System Architecture

```
                 +----------------------+
                 |    React Frontend    |
                 |  (Receptionist & UI) |
                 +----------+-----------+
                            |
                            | Axios HTTP Requests (JWT-secured)
                            ▼
                 +----------------------+
                 |   Spring Boot API    |
                 | (Business Logic +    |
                 |  Security + OCR Call)|
                 +----------+-----------+
                            |
                            | JPA / Hibernate
                            ▼
                 +----------------------+
                 |     MySQL DB         |
                 | (Visitors, Payments, |
                 |  Services, Logins)   |
                 +----------------------+
```

---

## 🧰 Tech Stack

| Layer      | Technology           |
|------------|----------------------|
| Frontend   | React.js, Bootstrap  |
| Backend    | Spring Boot, Spring Security, JPA |
| OCR        | Google Vision API    |
| Auth       | JWT Token            |
| Database   | MySQL                |
| Charts     | Chart.js / Recharts  |

---

## 🖥️ Frontend (React)

### 🔹 Location: `/frontend`

### 🔧 Setup Instructions:

```bash
cd frontend
npm install
npm start
```

- React server runs at `http://localhost:3000`
- Bootstrap used for styling tabs, forms, buttons.
- Stores JWT token in `localStorage`.
- Axios headers set like:
```js
const headers = {
  Authorization: `Bearer ${localStorage.getItem("token")}`
};
```

### 💡 Key Components

| Tab           | Description |
|---------------|-------------|
| Register      | Add visitor details |
| Check-in/out  | Mark arrival or departure |
| Services      | Log service used |
| Payments      | Log or upload receipt |
| Receipt Upload| Upload `.jpg`, `.png`, `.heic` (converted + resized before sending) |
| Dashboard     | Analytics for admin |

---

## 🔙 Backend (Spring Boot)

### 🔹 Location: `/backend`

### 🔧 Setup Instructions:

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

- Server runs at `http://localhost:8080`

### 💡 Notable Endpoints

| Endpoint                   | Description                          |
|---------------------------|--------------------------------------|
| `/api/visitors/register`  | Add new visitor                      |
| `/api/visits/checkin`     | Mark check-in                        |
| `/api/visits/checkout`    | Mark check-out                       |
| `/api/services`           | Add service record                   |
| `/api/payments`           | Record manual/automated payment      |
| `/api/ocr/extract-payments` | Upload image and extract payments |

### 🛠 Sample `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vistrack
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8080

google:
  vision:
    api-key: YOUR_GOOGLE_API_KEY
```

> ✅ **Note**: Add `application.yml` to `.gitignore` for security.

---

## 🤖 Google Vision API Integration

- Endpoint: `POST /api/ocr/extract-payments`
- Accepts: Image (base64 or multipart)
- Uses `RestTemplate` in Spring Boot to call Google Vision
- Extracts text using Google's ML and processes it with Java **Regex**
- Maps to:
    - `visitorName`
    - `phoneNumber`
    - `category`
    - `serviceType`
    - `amount`
    - `timestamp`

---

## 🔐 Authentication & Authorization

### 🔑 User Roles:

| Role        | Access                          |
|-------------|----------------------------------|
| `ADMIN`     | Full Access (Dashboard + Forms) |
| `RECEPTIONIST` | Forms only (no dashboard)    |

- JWT issued on successful login
- Token sent in headers for all protected endpoints

```http
Authorization: Bearer eyJhbGciOi...
```

---

## 🧾 Database Schema Overview

### Table: `visitors`

| Column        | Type     |
|---------------|----------|
| id (PK)       | bigint   |
| name          | varchar  |
| phone_number  | varchar  |
| address       | text     |

---

### Table: `payments`

| Column       | Type     |
|--------------|----------|
| id (PK)      | bigint   |
| visitor_id   | FK       |
| category     | varchar  |
| service_type | varchar  |
| amount       | double   |
| paid_at      | datetime |

---

### Table: `users`

| Column     | Type     |
|------------|----------|
| id         | bigint   |
| username   | varchar  |
| password   | varchar  |
| role       | enum     |

---

---

## 👨‍💻 Contributors

| Name                 | Role                   |
|----------------------|------------------------|
| **Sasank Dattu Talluri** | Full-stack Developer, Architect |

---

## 📌 Next Steps / Improvements

- Role-based dashboard visualizations
- Monthly email reports of visitor trends
- More accurate NLP-based receipt extraction
- Mobile app (React Native)