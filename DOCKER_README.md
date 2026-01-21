# Docker Setup

## Предварителни изисквания

- Docker Desktop инсталиран и стартиран
- Docker Compose (обикновено идва с Docker Desktop)

## Структура на файловете

```
simple-poc/
├── docker-compose.yml          # Главен композит файл
├── backend/
│   ├── Dockerfile             # Backend image (Quarkus + Java 21)
│   └── .dockerignore          # Изключва ненужни файлове
└── frontend/
    ├── Dockerfile             # Frontend image (React + Nginx)
    ├── nginx.conf             # Nginx конфигурация
    └── .dockerignore          # Изключва ненужни файлове
```

## Стартиране

### Първоначален build и стартиране

```bash
docker-compose up --build
```

### Стартиране на вече build-нати images

```bash
docker-compose up
```

### Стартиране в background (detached mode)

```bash
docker-compose up -d
```

### Спиране на контейнерите

```bash
docker-compose down
```

### Спиране и изтриване на volumes

```bash
docker-compose down -v
```

## Достъп до приложението

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **Backend Health**: http://localhost:8080/hello

## Health Checks

И двата сървиса имат health checks:
- Backend се проверява на `/hello` endpoint
- Frontend се проверява на `/health` endpoint

Frontend няма да стартира докато backend не стане здрав (healthy).

## Логове

### Виж логовете на всички сървиси

```bash
docker-compose logs -f
```

### Виж логовете само на backend

```bash
docker-compose logs -f backend
```

### Виж логовете само на frontend

```bash
docker-compose logs -f frontend
```

## Debugging

### Влез в backend контейнера

```bash
docker exec -it quarkus-backend sh
```

### Влез в frontend контейнера

```bash
docker exec -it react-frontend sh
```

### Провери статуса на контейнерите

```bash
docker-compose ps
```

### Rebuild на конкретен сървис

```bash
docker-compose build backend
docker-compose build frontend
```

## Конфигурация

### Backend Environment Variables

- `QUARKUS_DATASOURCE_DB_KIND=h2` - Използва H2 in-memory database
- `QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=drop-and-create` - Пресъздава схемата при всеки restart
- `QUARKUS_HTTP_CORS` - CORS конфигурация

### Frontend Environment Variables

- `REACT_APP_API_URL=http://localhost:8080` - URL на backend API

## Възможни проблеми

### Port 80 е зает

Ако port 80 вече се използва, променете в docker-compose.yml:

```yaml
frontend:
  ports:
    - "3000:80"  # Вместо "80:80"
```

След това frontend ще е на http://localhost:3000

### Backend не може да се свърже към база данни

Backend използва H2 in-memory база данни, така че няма външни зависимости.

### CORS грешки

Проверете дали CORS origins в docker-compose.yml включват правилния frontend URL.
