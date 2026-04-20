# 📦 Modelos de Datos — EduTrack Backend

> Documento de referencia para el equipo de backend.
> Refleja exactamente los contratos que espera el cliente (Kotlin Multiplatform / Compose Web).
> Todos los campos se transmiten en **JSON**. Las fechas son strings ISO-8601.

---

## Índice de Entidades

| # | Entidad | Tabla sugerida | Descripción |
|---|---|---|---|
| 1 | `User` | `users` | Usuario del sistema |
| 2 | `Course` | `courses` | Curso / materia |
| 3 | `Clase` | `clases` | Sesión de clase (soporta recurrencia) |
| 4 | `Tarea` | `tareas` | Tarea académica |
| 5 | `Examen` | `examenes` | Examen / evaluación |
| 6 | `EventoPersonal` | `eventos_personales` | Evento personal del alumno |
| 7 | `RecurrenceRule` | *(embebido en `Clase`)* | Regla de recurrencia |
| 8 | `LoginRequest` | *(Auth)* | Payload de inicio de sesión |
| 9 | `LoginResponse` | *(Auth)* | Respuesta de token + user |

> `CalendarItem` y `CalendarEvent` son modelos de **solo lectura / vista** generados por el backend o por el motor del cliente. No requieren tabla propia; son respuestas agregadas.

---

## 1. User

**Tabla:** `users`

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `id` | `string` (UUID) | ✅ | Identificador único |
| `name` | `string` | ✅ | Nombre completo |
| `email` | `string` | ✅ | Email único (usado para login) |
| `role` | `enum` | ✅ | Rol del usuario |
| `password_hash` | `string` | ✅ | Hash de contraseña (solo backend, nunca serializado al cliente) |
| `created_at` | `datetime` | ✅ | Fecha de creación |
| `updated_at` | `datetime` | ✅ | Última actualización |

**Enum `role`:**
```
STUDENT | TEACHER | ADMIN
```

**Ejemplo JSON (respuesta al cliente):**
```json
{
  "id": "usr_abc123",
  "name": "Ana García",
  "email": "ana@universidad.edu",
  "role": "STUDENT"
}
```

---

## 2. Course

**Tabla:** `courses`

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `id` | `string` (UUID) | ✅ | Identificador único |
| `name` | `string` | ✅ | Nombre del curso |
| `teacher` | `string` | ❌ | Nombre del docente |
| `description` | `string` | ❌ | Descripción del curso (default: `""`) |
| `color` | `string` | ❌ | Color hex para UI (default: `"#4A90D9"`) |
| `location_or_platform` | `string` | ❌ | Aula física o URL de plataforma virtual |
| `credits` | `integer` | ❌ | Créditos académicos (`null` si no aplica) |
| `user_id` | `string` (FK) | ✅ | Propietario del curso (alumno o docente) |

**Ejemplo JSON:**
```json
{
  "id": "crs_001",
  "name": "Cálculo I",
  "teacher": "Dr. Pérez",
  "description": "Cálculo diferencial e integral.",
  "color": "#E74C3C",
  "locationOrPlatform": "Aula 204-B",
  "credits": 4
}
```

> ⚠️ El cliente envía `locationOrPlatform` (camelCase). El backend puede almacenarlo como `location_or_platform` (snake_case) pero debe serializar en camelCase en la respuesta.

---

## 3. Clase

**Tabla:** `clases`

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `id` | `string` (UUID) | ✅ | Identificador único |
| `courseId` | `string` (FK) | ✅ | Curso al que pertenece |
| `titulo` | `string` | ❌ | Título de la sesión (default: `""`) |
| `modalidad` | `enum` | ❌ | Modalidad de la clase |
| `docente` | `string` | ❌ | Nombre del docente de la sesión |
| `aula` | `string` | ❌ | Ubicación física |
| `enlace` | `string` | ❌ | URL si es virtual |
| `notas` | `string` | ❌ | Notas adicionales |
| `startDate` | `string` (YYYY-MM-DD) | ✅ | Fecha de inicio |
| `endDate` | `string` (YYYY-MM-DD) | ❌ | Fecha de fin de la serie (default = `startDate`) |
| `startTime` | `string` (HH:mm) | ❌ | Hora de inicio (`null` = todo el día) |
| `endTime` | `string` (HH:mm) | ❌ | Hora de fin |
| `recurrenceRule` | `object` | ❌ | Regla de recurrencia (ver §7) |
| `daysOfWeek` | `array<integer>` | ❌ | Días activos: `0`=Lu … `6`=Do (para recurrencia WEEKLY) |
| `isCancelled` | `boolean` | ❌ | Si la ocurrencia fue cancelada (default: `false`) |
| `parentSeriesId` | `string` (FK) | ❌ | ID de la clase-padre (edición de ocurrencia individual) |

**Enum `modalidad`:**
```
PRESENTIAL | LIVE | SELF_PACED
```

**Ejemplo JSON:**
```json
{
  "id": "cls_001",
  "courseId": "crs_001",
  "titulo": "Introducción al límite",
  "modalidad": "PRESENTIAL",
  "docente": "Dr. Pérez",
  "aula": "Aula 204-B",
  "enlace": "",
  "notas": "Traer calculadora",
  "startDate": "2026-03-03",
  "endDate": "2026-06-30",
  "startTime": "08:00",
  "endTime": "10:00",
  "recurrenceRule": {
    "type": "WEEKLY",
    "interval": 1,
    "daysOfWeek": [0, 2],
    "endType": "UNTIL_DATE",
    "untilDate": "2026-06-30",
    "occurrenceCount": null
  },
  "daysOfWeek": [0, 2],
  "isCancelled": false,
  "parentSeriesId": null
}
```

---

## 4. Tarea

**Tabla:** `tareas`

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `id` | `string` (UUID) | ✅ | Identificador único |
| `courseId` | `string` (FK) | ✅ | Curso al que pertenece |
| `titulo` | `string` | ✅ | Título de la tarea |
| `descripcion` | `string` | ❌ | Descripción detallada (default: `""`) |
| `prioridad` | `enum` | ❌ | Prioridad (default: `MEDIUM`) |
| `startDate` | `string` (YYYY-MM-DD) | ❌ | Fecha de inicio (opcional) |
| `dueDate` | `string` (YYYY-MM-DD) | ✅ | Fecha límite de entrega |
| `dueTime` | `string` (HH:mm) | ❌ | Hora límite (`null` = todo el día) |
| `endDate` | `string` (YYYY-MM-DD) | ❌ | Fecha de fin de rango (opcional) |
| `estado` | `enum` | ❌ | Estado de la tarea (default: `PENDING`) |

**Enum `prioridad`:**
```
LOW | MEDIUM | HIGH
```

**Enum `estado`:**
```
PENDING | IN_PROGRESS | SUBMITTED | OVERDUE
```

**Ejemplo JSON:**
```json
{
  "id": "tar_001",
  "courseId": "crs_001",
  "titulo": "Ejercicios de derivadas",
  "descripcion": "Resolver los ejercicios 1-20 del capítulo 3.",
  "prioridad": "HIGH",
  "startDate": "2026-04-01",
  "dueDate": "2026-04-10",
  "dueTime": "23:59",
  "endDate": null,
  "estado": "PENDING"
}
```

---

## 5. Examen

**Tabla:** `examenes`

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `id` | `string` (UUID) | ✅ | Identificador único |
| `courseId` | `string` (FK) | ✅ | Curso al que pertenece |
| `titulo` | `string` | ✅ | Nombre del examen |
| `tema` | `string` | ❌ | Tema o unidad evaluada (default: `""`) |
| `puntaje` | `float` | ❌ | Puntaje obtenido (`null` hasta que sea calificado) |
| `fecha` | `string` (YYYY-MM-DD) | ✅ | Fecha del examen |
| `horaInicio` | `string` (HH:mm) | ❌ | Hora de inicio |
| `horaFin` | `string` (HH:mm) | ❌ | Hora de fin (`null` = horaInicio + 1h por convención) |
| `duracion` | `integer` | ❌ | Duración en minutos (informativo) |
| `estado` | `enum` | ❌ | Estado del examen (default: `PENDING`) |

**Enum `estado`:**
```
PENDING | TAKEN | RESCHEDULED | CANCELLED
```

**Ejemplo JSON:**
```json
{
  "id": "exa_001",
  "courseId": "crs_001",
  "titulo": "Parcial 1",
  "tema": "Límites y continuidad",
  "puntaje": null,
  "fecha": "2026-04-20",
  "horaInicio": "10:00",
  "horaFin": "12:00",
  "duracion": 120,
  "estado": "PENDING"
}
```

---

## 6. EventoPersonal

**Tabla:** `eventos_personales`

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `id` | `string` (UUID) | ✅ | Identificador único |
| `userId` | `string` (FK) | ✅ | Propietario del evento |
| `titulo` | `string` | ✅ | Título del evento |
| `descripcion` | `string` | ❌ | Descripción (default: `""`) |
| `fecha` | `string` (YYYY-MM-DD) | ✅ | Fecha del evento |
| `horaInicio` | `string` (HH:mm) | ❌ | Hora de inicio (`null` = todo el día) |
| `horaFin` | `string` (HH:mm) | ❌ | Hora de fin |

> ⚠️ El campo `userId` no es transmitido al cliente, pero el backend **debe** asociarlo a partir del token JWT.

**Ejemplo JSON (respuesta al cliente, sin `userId`):**
```json
{
  "id": "evt_001",
  "titulo": "Cita médica",
  "descripcion": "Control anual",
  "fecha": "2026-04-22",
  "horaInicio": "09:30",
  "horaFin": "10:00"
}
```

---

## 7. RecurrenceRule *(objeto embebido)*

Se embebe dentro de `Clase`. No tiene tabla propia (recomendado: columna JSON o columnas planas en `clases`).

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `type` | `enum` | ✅ | Tipo de recurrencia (default: `NONE`) |
| `interval` | `integer` | ❌ | Cada cuántas unidades repetir (default: `1`) |
| `daysOfWeek` | `array<integer>` | ❌ | Días activos `0`=Lu … `6`=Do (solo para `WEEKLY`) |
| `endType` | `enum` | ❌ | Cómo termina la recurrencia (default: `NEVER`) |
| `untilDate` | `string` (YYYY-MM-DD) | ❌ | Fecha límite (solo para `UNTIL_DATE`) |
| `occurrenceCount` | `integer` | ❌ | Número de ocurrencias (solo para `AFTER_OCCURRENCES`) |

**Enum `type`:**
```
NONE | DAILY | WEEKLY | MONTHLY
```

**Enum `endType`:**
```
NEVER | UNTIL_DATE | AFTER_OCCURRENCES
```

---

## 8. Auth — LoginRequest / LoginResponse

### LoginRequest *(POST /auth/login)*

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `email` | `string` | ✅ | Email del usuario |
| `password` | `string` | ✅ | Contraseña en texto plano (debe ir por HTTPS) |

```json
{
  "email": "ana@universidad.edu",
  "password": "mi_password_segura"
}
```

### LoginResponse

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `token` | `string` | ✅ | JWT de sesión |
| `user` | `User` | ✅ | Objeto usuario (sin `password_hash`) |

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "usr_abc123",
    "name": "Ana García",
    "email": "ana@universidad.edu",
    "role": "STUDENT"
  }
}
```

---

## 9. CalendarItem *(solo lectura — vista agregada)*

No requiere tabla. Es una respuesta calculada que el backend puede opcionalmente proveer como endpoint de agenda.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `string` | Único por ocurrencia: `{sourceId}_{fecha}` |
| `sourceId` | `string` | ID de la entidad original (Clase/Tarea/Examen/EventoPersonal) |
| `type` | `enum` | `CLASS \| TASK \| EXAM \| PERSONAL` |
| `title` | `string` | Título del evento |
| `date` | `string` (YYYY-MM-DD) | Fecha de la ocurrencia |
| `startTime` | `string` (HH:mm) | Hora de inicio (`null` = todo el día) |
| `endTime` | `string` (HH:mm) | Hora de fin |
| `isAllDay` | `boolean` | Si es un evento de todo el día |
| `courseColor` | `string` | Color hex del curso (`null` para PersonalEvent) |
| `courseId` | `string` | ID del curso (`null` para PersonalEvent) |
| `courseName` | `string` | Nombre del curso (`null` para PersonalEvent) |
| `status` | `string` | Estado legible (`"PENDING"`, `"TAKEN"`, etc.) |
| `isDeadline` | `boolean` | `true` solo para Tareas |

---

## Resumen de Enums

| Enum | Valores |
|---|---|
| `UserRole` | `STUDENT`, `TEACHER`, `ADMIN` |
| `ClassMode` | `PRESENTIAL`, `LIVE`, `SELF_PACED` |
| `TaskPriority` | `LOW`, `MEDIUM`, `HIGH` |
| `TaskStatus` | `PENDING`, `IN_PROGRESS`, `SUBMITTED`, `OVERDUE` |
| `ExamStatus` | `PENDING`, `TAKEN`, `RESCHEDULED`, `CANCELLED` |
| `RecurrenceType` | `NONE`, `DAILY`, `WEEKLY`, `MONTHLY` |
| `RecurrenceEndType` | `NEVER`, `UNTIL_DATE`, `AFTER_OCCURRENCES` |
| `EventType` | `CLASS`, `EXAM`, `TASK`, `PERSONAL` |

---

## Convenciones Generales

| Aspecto | Convención |
|---|---|
| Formato de fechas | `YYYY-MM-DD` (ISO-8601 date) |
| Formato de horas | `HH:mm` (24h) |
| Formato de IDs | UUID v4 o string único (ej. `usr_abc123`) |
| Serialización JSON | camelCase (igual que el cliente Kotlin) |
| Autenticación | Bearer JWT en header `Authorization` |
| Campos opcionales | Se omiten o se envían `null` — nunca se omiten en la respuesta |
| Zona horaria | UTC en backend; conversión en cliente |

---

## Endpoints mínimos esperados por el cliente

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/login` | Iniciar sesión |
| `POST` | `/auth/logout` | Cerrar sesión |
| `GET` | `/auth/me` | Usuario actual (token) |
| `GET` | `/courses` | Listar cursos del usuario |
| `POST` | `/courses` | Crear curso |
| `PUT` | `/courses/{id}` | Actualizar curso |
| `DELETE` | `/courses/{id}` | Eliminar curso |
| `GET` | `/courses/{id}/clases` | Listar clases del curso |
| `POST` | `/clases` | Crear clase |
| `PUT` | `/clases/{id}` | Actualizar clase |
| `DELETE` | `/clases/{id}` | Eliminar clase |
| `GET` | `/courses/{id}/tareas` | Listar tareas del curso |
| `POST` | `/tareas` | Crear tarea |
| `PUT` | `/tareas/{id}` | Actualizar tarea |
| `DELETE` | `/tareas/{id}` | Eliminar tarea |
| `GET` | `/courses/{id}/examenes` | Listar exámenes del curso |
| `POST` | `/examenes` | Crear examen |
| `PUT` | `/examenes/{id}` | Actualizar examen |
| `DELETE` | `/examenes/{id}` | Eliminar examen |
| `GET` | `/eventos-personales` | Listar eventos personales |
| `POST` | `/eventos-personales` | Crear evento personal |
| `PUT` | `/eventos-personales/{id}` | Actualizar evento personal |
| `DELETE` | `/eventos-personales/{id}` | Eliminar evento personal |
| `GET` | `/calendar?from=YYYY-MM-DD&to=YYYY-MM-DD` | *(Opcional)* Agenda agregada (CalendarItem) |

