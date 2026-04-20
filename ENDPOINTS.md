# 📡 API Endpoints — Microsaap

> **Base URL:** `http://localhost:8080`
> 
> **Autenticación:** Todos los endpoints (excepto `/api/auth/*`) requieren el header:
> ```
> Authorization: Bearer <token>
> ```

---

## 📋 Tabla de contenidos

- [🔐 Auth](#-auth)
- [📚 Courses (Cursos)](#-courses-cursos)
- [🏫 Clases](#-clases)
- [📝 Tareas](#-tareas)
- [📄 Exámenes](#-exámenes)
- [📅 Eventos Personales](#-eventos-personales)

---

## 🔐 Auth

### `POST /api/auth/register`
> 📬 **Nombre en Postman:** `[AUTH] Registrar Usuario`

Registra un nuevo usuario.

**Request Body:**
```json
{
  "email": "estudiante@ejemplo.com",
  "password": "Contrasena1@",
  "firstName": "Juan",
  "lastName": "Pérez"
}
```

**Response `201 Created`:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "email": "estudiante@ejemplo.com",
      "firstName": "Juan",
      "lastName": "Pérez",
      "fullName": "Juan Pérez",
      "isActive": true
    }
  }
}
```

---

### `POST /api/auth/login`
> 📬 **Nombre en Postman:** `[AUTH] Iniciar Sesión`

Inicia sesión con un usuario existente.

**Request Body:**
```json
{
  "email": "estudiante@ejemplo.com",
  "password": "Contrasena1@"
}
```

**Response `200 OK`:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "email": "estudiante@ejemplo.com",
      "firstName": "Juan",
      "lastName": "Pérez",
      "fullName": "Juan Pérez",
      "isActive": true
    }
  }
}
```

---

## 📚 Courses (Cursos)

> Todos los endpoints requieren `Authorization: Bearer <token>`

### `GET /api/courses`
> 📬 **Nombre en Postman:** `[COURSES] Listar Cursos del Usuario`

Lista todos los cursos del usuario autenticado.

**Response `200 OK`:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Cálculo Diferencial",
      "teacher": "Dr. García",
      "description": "Introducción al cálculo",
      "color": "#4A90D9",
      "locationOrPlatform": "Aula 201",
      "credits": 4
    }
  ]
}
```

---

### `GET /api/courses/{id}`
> 📬 **Nombre en Postman:** `[COURSES] Obtener Curso por ID`

Obtiene un curso por ID.

**Parámetros de ruta:** `id` (Long)

**Response `200 OK`:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Cálculo Diferencial",
    "teacher": "Dr. García",
    "description": "Introducción al cálculo",
    "color": "#4A90D9",
    "locationOrPlatform": "Aula 201",
    "credits": 4
  }
}
```

---

### `POST /api/courses`
> 📬 **Nombre en Postman:** `[COURSES] Crear Curso`

Crea un nuevo curso para el usuario autenticado.

**Request Body:**
```json
{
  "name": "Cálculo Diferencial",
  "teacher": "Dr. García",
  "description": "Introducción al cálculo diferencial e integral",
  "color": "#4A90D9",
  "locationOrPlatform": "Aula 201",
  "credits": 4
}
```

> `name` es **requerido**. Los demás campos son opcionales.
> `color` por defecto: `"#4A90D9"`

**Response `201 Created`:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Cálculo Diferencial",
    "teacher": "Dr. García",
    "description": "Introducción al cálculo diferencial e integral",
    "color": "#4A90D9",
    "locationOrPlatform": "Aula 201",
    "credits": 4
  }
}
```

---

### `PUT /api/courses/{id}`
> 📬 **Nombre en Postman:** `[COURSES] Actualizar Curso`

Actualiza un curso existente.

**Parámetros de ruta:** `id` (Long)

**Request Body:**
```json
{
  "name": "Cálculo Diferencial II",
  "teacher": "Dr. García",
  "description": "Continuación del cálculo",
  "color": "#E74C3C",
  "locationOrPlatform": "Zoom",
  "credits": 4
}
```

**Response `200 OK`:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Cálculo Diferencial II",
    "teacher": "Dr. García",
    "description": "Continuación del cálculo",
    "color": "#E74C3C",
    "locationOrPlatform": "Zoom",
    "credits": 4
  }
}
```

---

### `DELETE /api/courses/{id}`
> 📬 **Nombre en Postman:** `[COURSES] Eliminar Curso`

Elimina un curso por ID.

**Parámetros de ruta:** `id` (Long)

**Response `204 No Content`**

---

## 🏫 Clases

> Todos los endpoints requieren `Authorization: Bearer <token>`

### Enums disponibles

| Tipo | Valores |
|------|---------|
| `modalidad` (ClassMode) | `PRESENTIAL`, `VIRTUAL`, `HYBRID` |
| `recurrenceRule.type` (RecurrenceType) | `NONE`, `DAILY`, `WEEKLY`, `MONTHLY` |
| `recurrenceRule.endType` (RecurrenceEndType) | `NEVER`, `UNTIL_DATE`, `COUNT` |

---

### `GET /api/courses/{courseId}/clases`
> 📬 **Nombre en Postman:** `[CLASES] Listar Clases del Curso`

Lista las clases de un curso.

**Parámetros de ruta:** `courseId` (Long)

**Response `200 OK`:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "courseId": 1,
      "titulo": "Clase 1 - Introducción",
      "modalidad": "PRESENTIAL",
      "docente": "Dr. García",
      "aula": "201",
      "enlace": null,
      "notas": "Llevar calculadora",
      "startDate": "2026-03-01",
      "endDate": null,
      "startTime": "08:00",
      "endTime": "10:00",
      "recurrenceRule": {
        "type": "WEEKLY",
        "interval": 1,
        "daysOfWeek": [1, 3],
        "endType": "UNTIL_DATE",
        "untilDate": "2026-07-01",
        "occurrenceCount": null
      },
      "daysOfWeek": [1, 3],
      "isCancelled": false,
      "parentSeriesId": null
    }
  ]
}
```

---

### `GET /api/clases/{id}`
> 📬 **Nombre en Postman:** `[CLASES] Obtener Clase por ID`

Obtiene una clase por ID.

**Parámetros de ruta:** `id` (Long)

**Response `200 OK`:** (igual al objeto de arriba)

---

### `POST /api/clases`
> 📬 **Nombre en Postman:** `[CLASES] Crear Clase`

Crea una nueva clase.

**Request Body:**
```json
{
  "courseId": 1,
  "titulo": "Clase 1 - Introducción",
  "modalidad": "PRESENTIAL",
  "docente": "Dr. García",
  "aula": "201",
  "enlace": null,
  "notas": "Llevar calculadora",
  "startDate": "2026-03-01",
  "endDate": null,
  "startTime": "08:00",
  "endTime": "10:00",
  "recurrenceRule": {
    "type": "WEEKLY",
    "interval": 1,
    "daysOfWeek": [1, 3],
    "endType": "UNTIL_DATE",
    "untilDate": "2026-07-01",
    "occurrenceCount": null
  },
  "daysOfWeek": [1, 3],
  "parentSeriesId": null
}
```

> `courseId` y `startDate` son **requeridos**.
> `daysOfWeek`: 0=Domingo, 1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes, 6=Sábado

**Response `201 Created`:** (igual a la respuesta GET)

---

### `PUT /api/clases/{id}`
> 📬 **Nombre en Postman:** `[CLASES] Actualizar Clase`

Actualiza una clase existente.

**Parámetros de ruta:** `id` (Long)

**Request Body:**
```json
{
  "titulo": "Clase 1 - Introducción (Actualizada)",
  "modalidad": "VIRTUAL",
  "docente": "Dr. García",
  "aula": null,
  "enlace": "https://meet.google.com/abc-xyz",
  "notas": "Clase en línea esta semana",
  "startDate": "2026-03-01",
  "endDate": null,
  "startTime": "08:00",
  "endTime": "10:00",
  "recurrenceRule": {
    "type": "NONE",
    "interval": 1,
    "daysOfWeek": [],
    "endType": "NEVER",
    "untilDate": null,
    "occurrenceCount": null
  },
  "daysOfWeek": [],
  "isCancelled": false
}
```

**Response `200 OK`:** (igual a la respuesta GET)

---

### `DELETE /api/clases/{id}`
> 📬 **Nombre en Postman:** `[CLASES] Eliminar Clase`

Elimina una clase por ID.

**Parámetros de ruta:** `id` (Long)

**Response `204 No Content`**

---

## 📝 Tareas

> Todos los endpoints requieren `Authorization: Bearer <token>`

### Enums disponibles

| Tipo | Valores |
|------|---------|
| `prioridad` (TaskPriority) | `LOW`, `MEDIUM`, `HIGH` |
| `estado` (TaskStatus) | `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |

---

### `GET /api/courses/{courseId}/tareas`
> 📬 **Nombre en Postman:** `[TAREAS] Listar Tareas del Curso`

Lista las tareas de un curso.

**Parámetros de ruta:** `courseId` (Long)

**Response `200 OK`:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "courseId": 1,
      "titulo": "Tarea 1 - Derivadas",
      "descripcion": "Resolver ejercicios del capítulo 3",
      "prioridad": "HIGH",
      "startDate": "2026-04-01",
      "dueDate": "2026-04-15",
      "dueTime": "23:59",
      "endDate": null,
      "estado": "PENDING"
    }
  ]
}
```

---

### `GET /api/tareas/{id}`
> 📬 **Nombre en Postman:** `[TAREAS] Obtener Tarea por ID`

Obtiene una tarea por ID.

**Parámetros de ruta:** `id` (Long)

**Response `200 OK`:** (igual al objeto de arriba)

---

### `POST /api/tareas`
> 📬 **Nombre en Postman:** `[TAREAS] Crear Tarea`

Crea una nueva tarea.

**Request Body:**
```json
{
  "courseId": 1,
  "titulo": "Tarea 1 - Derivadas",
  "descripcion": "Resolver ejercicios del capítulo 3",
  "prioridad": "HIGH",
  "startDate": "2026-04-01",
  "dueDate": "2026-04-15",
  "dueTime": "23:59",
  "endDate": null
}
```

> `courseId`, `titulo` y `dueDate` son **requeridos**.
> `prioridad` por defecto: `MEDIUM`

**Response `201 Created`:** (igual a la respuesta GET)

---

### `PUT /api/tareas/{id}`
> 📬 **Nombre en Postman:** `[TAREAS] Actualizar Tarea`

Actualiza una tarea existente.

**Parámetros de ruta:** `id` (Long)

**Request Body:**
```json
{
  "titulo": "Tarea 1 - Derivadas (Actualizada)",
  "descripcion": "Resolver ejercicios del capítulo 3 y 4",
  "prioridad": "MEDIUM",
  "startDate": "2026-04-01",
  "dueDate": "2026-04-20",
  "dueTime": "23:59",
  "endDate": null,
  "estado": "IN_PROGRESS"
}
```

**Response `200 OK`:** (igual a la respuesta GET)

---

### `DELETE /api/tareas/{id}`
> 📬 **Nombre en Postman:** `[TAREAS] Eliminar Tarea`

Elimina una tarea por ID.

**Parámetros de ruta:** `id` (Long)

**Response `204 No Content`**

---

## 📄 Exámenes

> Todos los endpoints requieren `Authorization: Bearer <token>`

### Enums disponibles

| Tipo | Valores |
|------|---------|
| `estado` (ExamStatus) | `PENDING`, `COMPLETED`, `CANCELLED` |

---

### `GET /api/courses/{courseId}/examenes`
> 📬 **Nombre en Postman:** `[EXAMENES] Listar Exámenes del Curso`

Lista los exámenes de un curso.

**Parámetros de ruta:** `courseId` (Long)

**Response `200 OK`:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "courseId": 1,
      "titulo": "Primer Parcial",
      "tema": "Límites y Derivadas",
      "puntaje": 100.0,
      "fecha": "2026-05-10",
      "horaInicio": "09:00",
      "horaFin": "11:00",
      "duracion": 120,
      "estado": "PENDING"
    }
  ]
}
```

---

### `GET /api/examenes/{id}`
> 📬 **Nombre en Postman:** `[EXAMENES] Obtener Examen por ID`

Obtiene un examen por ID.

**Parámetros de ruta:** `id` (Long)

**Response `200 OK`:** (igual al objeto de arriba)

---

### `POST /api/examenes`
> 📬 **Nombre en Postman:** `[EXAMENES] Crear Examen`

Crea un nuevo examen.

**Request Body:**
```json
{
  "courseId": 1,
  "titulo": "Primer Parcial",
  "tema": "Límites y Derivadas",
  "puntaje": 100.0,
  "fecha": "2026-05-10",
  "horaInicio": "09:00",
  "horaFin": "11:00",
  "duracion": 120
}
```

> `courseId`, `titulo` y `fecha` son **requeridos**.
> `estado` por defecto: `PENDING`

**Response `201 Created`:** (igual a la respuesta GET)

---

### `PUT /api/examenes/{id}`
> 📬 **Nombre en Postman:** `[EXAMENES] Actualizar Examen`

Actualiza un examen existente.

**Parámetros de ruta:** `id` (Long)

**Request Body:**
```json
{
  "titulo": "Primer Parcial (Reprogramado)",
  "tema": "Límites, Derivadas e Integrales",
  "puntaje": 100.0,
  "fecha": "2026-05-17",
  "horaInicio": "10:00",
  "horaFin": "12:00",
  "duracion": 120,
  "estado": "PENDING"
}
```

**Response `200 OK`:** (igual a la respuesta GET)

---

### `DELETE /api/examenes/{id}`
> 📬 **Nombre en Postman:** `[EXAMENES] Eliminar Examen`

Elimina un examen por ID.

**Parámetros de ruta:** `id` (Long)

**Response `204 No Content`**

---

## 📅 Eventos Personales

> Todos los endpoints requieren `Authorization: Bearer <token>`

### `GET /api/eventos-personales`
> 📬 **Nombre en Postman:** `[EVENTOS] Listar Eventos Personales`

Lista todos los eventos personales del usuario autenticado.

**Response `200 OK`:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "titulo": "Reunión con tutor",
      "descripcion": "Revisión de avance de tesis",
      "fecha": "2026-04-25",
      "horaInicio": "14:00",
      "horaFin": "15:00"
    }
  ]
}
```

---

### `GET /api/eventos-personales/{id}`
> 📬 **Nombre en Postman:** `[EVENTOS] Obtener Evento por ID`

Obtiene un evento personal por ID.

**Parámetros de ruta:** `id` (Long)

**Response `200 OK`:** (igual al objeto de arriba)

---

### `POST /api/eventos-personales`
> 📬 **Nombre en Postman:** `[EVENTOS] Crear Evento Personal`

Crea un nuevo evento personal.

**Request Body:**
```json
{
  "titulo": "Reunión con tutor",
  "descripcion": "Revisión de avance de tesis",
  "fecha": "2026-04-25",
  "horaInicio": "14:00",
  "horaFin": "15:00"
}
```

> `titulo` y `fecha` son **requeridos**.
> `descripcion` por defecto: `""`
> Las horas son opcionales, formato `HH:mm`

**Response `201 Created`:** (igual a la respuesta GET)

---

### `PUT /api/eventos-personales/{id}`
> 📬 **Nombre en Postman:** `[EVENTOS] Actualizar Evento Personal`

Actualiza un evento personal.

**Parámetros de ruta:** `id` (Long)

**Request Body:**
```json
{
  "titulo": "Reunión con tutor (Reprogramada)",
  "descripcion": "Revisión de avance de tesis - nueva fecha",
  "fecha": "2026-04-28",
  "horaInicio": "10:00",
  "horaFin": "11:00"
}
```

**Response `200 OK`:** (igual a la respuesta GET)

---

### `DELETE /api/eventos-personales/{id}`
> 📬 **Nombre en Postman:** `[EVENTOS] Eliminar Evento Personal`

Elimina un evento personal por ID.

**Parámetros de ruta:** `id` (Long)

**Response `204 No Content`**

---

## 🔧 Estructura general de respuesta

Todos los endpoints devuelven el siguiente wrapper:

```json
{
  "success": true,
  "data": { }
}
```

En caso de error:
```json
{
  "success": false,
  "message": "Descripción del error",
  "data": null
}
```

---

## 📌 Resumen de endpoints

| Módulo | Método | Ruta | Descripción |
|--------|--------|------|-------------|
| Auth | `POST` | `/api/auth/register` | Registrar usuario |
| Auth | `POST` | `/api/auth/login` | Iniciar sesión |
| Courses | `GET` | `/api/courses` | Listar cursos del usuario |
| Courses | `GET` | `/api/courses/{id}` | Obtener curso por ID |
| Courses | `POST` | `/api/courses` | Crear curso |
| Courses | `PUT` | `/api/courses/{id}` | Actualizar curso |
| Courses | `DELETE` | `/api/courses/{id}` | Eliminar curso |
| Clases | `GET` | `/api/courses/{courseId}/clases` | Listar clases del curso |
| Clases | `GET` | `/api/clases/{id}` | Obtener clase por ID |
| Clases | `POST` | `/api/clases` | Crear clase |
| Clases | `PUT` | `/api/clases/{id}` | Actualizar clase |
| Clases | `DELETE` | `/api/clases/{id}` | Eliminar clase |
| Tareas | `GET` | `/api/courses/{courseId}/tareas` | Listar tareas del curso |
| Tareas | `GET` | `/api/tareas/{id}` | Obtener tarea por ID |
| Tareas | `POST` | `/api/tareas` | Crear tarea |
| Tareas | `PUT` | `/api/tareas/{id}` | Actualizar tarea |
| Tareas | `DELETE` | `/api/tareas/{id}` | Eliminar tarea |
| Exámenes | `GET` | `/api/courses/{courseId}/examenes` | Listar exámenes del curso |
| Exámenes | `GET` | `/api/examenes/{id}` | Obtener examen por ID |
| Exámenes | `POST` | `/api/examenes` | Crear examen |
| Exámenes | `PUT` | `/api/examenes/{id}` | Actualizar examen |
| Exámenes | `DELETE` | `/api/examenes/{id}` | Eliminar examen |
| Eventos | `GET` | `/api/eventos-personales` | Listar eventos del usuario |
| Eventos | `GET` | `/api/eventos-personales/{id}` | Obtener evento por ID |
| Eventos | `POST` | `/api/eventos-personales` | Crear evento personal |
| Eventos | `PUT` | `/api/eventos-personales/{id}` | Actualizar evento personal |
| Eventos | `DELETE` | `/api/eventos-personales/{id}` | Eliminar evento personal |

