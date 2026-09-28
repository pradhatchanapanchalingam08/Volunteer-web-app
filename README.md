# Campus NGO Community Event and Volunteer Management System

A Spring Boot backend for a campus NGO to manage community events, volunteer signups, attendance and contributed hours instead of spreadsheets.

## Core features

- Create, view, update and delete events with date, location and volunteer capacity.
- Register volunteers for an event only while capacity remains.
- Reject duplicate signups and signups beyond event capacity in the service layer.
- Mark a signup as attended or not attended after the event.
- Record contributed hours only when the volunteer is marked as attended.
- View total contributed hours for each volunteer.
- List upcoming events with registered counts and open slots.
- REST APIs with validation and a global exception handler for clear errors.
- MySQL integration through Spring Data JPA.
- Simple built-in browser UI for demonstrating the core features.

## Technology

- Java 21
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA
- Spring Validation
- MySQL
- Maven

## Database entities

- Event
- Volunteer
- Signup
- AttendanceRecord

Relationships:

- Event 1 -> many Signups
- Volunteer 1 -> many Signups
- Signup 1 -> 0..1 AttendanceRecord

## Business rules

1. Signups beyond event capacity are rejected before saving.
2. A volunteer cannot register twice for the same event.
3. Hours can only be logged when attended is true.
4. Attended volunteers must have hours greater than zero.
5. Event capacity cannot be reduced below its current signup count.

## Run

1. Install Java 21, Maven and MySQL 8.
2. Make sure MySQL is running.
3. Update `src/main/resources/application.properties` with your MySQL username/password.
4. From the folder containing `pom.xml`, run:

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

5. Open `http://localhost:8080`.

The database `campus_ngo` is created automatically if it does not already exist. JPA creates/updates the tables using `spring.jpa.hibernate.ddl-auto=update`.

## REST endpoints

### Events

- `POST /api/events`
- `GET /api/events`
- `GET /api/events/{id}`
- `GET /api/events/upcoming`
- `PUT /api/events/{id}`
- `DELETE /api/events/{id}`

### Volunteers

- `POST /api/volunteers`
- `GET /api/volunteers`
- `GET /api/volunteers/{id}`
- `GET /api/volunteers/{id}/hours`
- `PUT /api/volunteers/{id}`
- `DELETE /api/volunteers/{id}`

### Signups

- `POST /api/signups`
- `GET /api/signups`
- `GET /api/signups/{id}`
- `DELETE /api/signups/{id}`

### Attendance

- `POST /api/attendance`
- `GET /api/attendance`
- `GET /api/attendance/{id}`
- `GET /api/attendance/signup/{signupId}`
- `PUT /api/attendance/{id}`

## Example request bodies

Create event:

```json
{
  "name": "Tree Plantation Drive",
  "date": "2026-10-10",
  "location": "College Campus",
  "capacity": 30
}
```

Create volunteer:

```json
{
  "name": "Priya",
  "email": "priya@example.com",
  "phone": "9876543210"
}
```

Signup:

```json
{
  "eventId": 1,
  "volunteerId": 1
}
```

Mark attendance and hours:

```json
{
  "signupId": 1,
  "attended": true,
  "hoursContributed": 4
}
```
