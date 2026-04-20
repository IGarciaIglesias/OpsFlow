# OpsFlow Manager

OpsFlow Manager es una aplicación enterprise de gestión de solicitudes operativas construida con Spring Boot y Angular. El sistema permite autenticación con JWT, gestión de solicitudes con workflow de estados, trazabilidad mediante histórico, procesamiento asíncrono y visualización de métricas básicas. [web:1]

## Objetivo

El proyecto está diseñado como ejercicio formativo para practicar un stack moderno Java/Angular con separación clara entre backend, frontend, persistencia, seguridad, colas y cache. [file:139]

## Funcionalidades principales

- Login con JWT. [file:139]
- Gestión de solicitudes operativas. [file:139]
- Workflow de estados: `DRAFT`, `PENDING_VALIDATION`, `VALIDATED`, `REJECTED`, `APPROVED`, `IN_PROGRESS`, `COMPLETED`, `FAILED`, `CANCELLED`. [file:139]
- Histórico de cambios de estado por solicitud. [file:139]
- Listado paginado con filtros. [file:139]
- Dashboard con métricas simples. [file:139]
- Control de acceso por roles: `ADMIN`, `MANAGER`, `OPERATOR`, `VIEWER`. [file:139]

## Stack tecnológico

### Backend
- Java
- Spring Boot
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- RabbitMQ
- Hazelcast
- OpenAPI / Swagger [file:139]

### Frontend
- Angular
- Standalone components
- Guards
- HTTP interceptors
- Formularios
- Routing lazy-loaded [file:139]

## Roles

- `ADMIN`: acceso total; puede aprobar, cancelar y mantener catálogos. [file:139]
- `MANAGER`: puede aprobar y cancelar solicitudes, además de operar funcionalmente. [file:139]
- `OPERATOR`: puede crear y operar requests, pero no aprobar ni cancelar. [file:139]
- `VIEWER`: solo consulta listados, detalle y dashboard. [file:139]

## Flujo funcional

1. Se crea una solicitud en estado `DRAFT`. [file:139]
2. Se envía a validación con `submit`, pasando a `PENDING_VALIDATION`. [file:139]
3. El backend procesa la validación y la mueve a `VALIDATED`, `FAILED` o `REJECTED` según corresponda. [file:139]
4. Un usuario autorizado puede aprobar la solicitud. [file:139]
5. Al aprobar, se dispara el flujo asíncrono de ejecución. [file:139]
6. La solicitud pasa por `IN_PROGRESS` y termina en `COMPLETED` o `FAILED`. [file:139]
7. Cada transición queda registrada en el histórico. [file:139]

## Estructura del proyecto

### Frontend Angular

```text
src/app/
├── core/
│   ├── guards/
│   ├── interceptors/
│   ├── layout/
│   ├── models/
│   ├── services/
│   └── utils/
├── features/
│   ├── auth/
│   ├── requests/
│   └── dashboard/
```

La estructura sigue la separación recomendada entre `core` para infraestructura transversal y `features` para funcionalidades de negocio. [file:139]

## Endpoints principales

### Auth
- `POST /auth/login` [file:139]

### Requests
- `GET /requests` [file:139]
- `POST /requests` [file:139]
- `GET /requests/{id}` [file:139]
- `PUT /requests/{id}` [file:139]
- `POST /requests/{id}/submit` [file:139]
- `POST /requests/{id}/validate` [file:139]
- `POST /requests/{id}/approve` [file:139]
- `POST /requests/{id}/reject` [file:139]
- `POST /requests/{id}/cancel` [file:139]
- `POST /requests/{id}/retry` [file:139]
- `GET /requests/{id}/history` [file:139]

### Dashboard
- `GET /dashboard/summary` [file:139]

## Requisitos previos

- Node.js + npm
- Java 17+ (o la versión usada en el backend)
- PostgreSQL
- RabbitMQ
- Docker / Docker Compose recomendado [file:139]

## Cómo arrancar

### Backend
1. Configurar variables de entorno o `application-local.yml`.
2. Levantar PostgreSQL y RabbitMQ.
3. Ejecutar migraciones Flyway.
4. Arrancar Spring Boot.

### Frontend
```bash
npm install
npm start
```

La aplicación Angular queda disponible normalmente en `http://localhost:4200`. [file:152]

## Usuarios y permisos

Añadir aquí los usuarios demo si los tienes en seeds, por ejemplo:

- admin / admin123
- manager / manager123
- operator / operator123
- viewer / viewer123

## Casos de uso demostrables

- Login de usuario. [file:139]
- Crear una solicitud nueva. [file:139]
- Enviar a validación. [file:139]
- Aprobar o rechazar según rol. [file:139]
- Consultar histórico de estados. [file:139]
- Revisar el dashboard con métricas. [file:139]

## Calidad y arquitectura

El proyecto busca evitar un CRUD plano y demostrar:
- separación por capas, [file:139]
- uso de DTOs, [file:139]
- seguridad por roles, [file:139]
- workflow de negocio real, [file:139]
- asincronía con colas, [file:139]
- y una interfaz Angular operativa de punta a punta. [file:139]

## Mejoras futuras

- WebSocket o SSE para actualizar estados en tiempo real. [file:139]
- Outbox pattern simplificado. [file:139]
- Dead-letter queue y reintentos avanzados. [file:139]
- Auditoría extendida con before/after. [file:139]
- Búsqueda avanzada con Specification o QueryDSL. [file:139]

## Demo sugerida

1. Login con usuario autorizado. [file:139]
2. Crear una request en `DRAFT`. [file:139]
3. Enviarla a validación. [file:139]
4. Consultar el detalle e histórico. [file:139]
5. Aprobarla. [file:139]
6. Mostrar evolución asíncrona y resultado final. [file:139]
7. Abrir dashboard y comprobar métricas. [file:139]