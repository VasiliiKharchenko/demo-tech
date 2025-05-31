# Demo Tech - Микросервис управления пользователями и подписками

### Технические требования
- ✅ **Spring Boot 3** - использован Spring Boot 3.2.3
- ✅ **Java 17** - проект использует Java 17
- ✅ **PostgreSQL** - база данных PostgreSQL
- ✅ **Таблицы users, subscriptions** - созданы через Flyway миграции
- ✅ **Логирование SLF4J** - настроено логирование через SLF4J
- ✅ **Dockerfile** - создан для развертывания сервиса
- ✅ **docker-compose.yml** - настроен для локального запуска с БД

### Функциональные требования

#### API для управления пользователями
- ✅ **POST /users** - создание пользователя
- ✅ **GET /users/{id}** - получение информации о пользователе
- ✅ **PUT /users/{id}** - обновление данных пользователя
- ✅ **DELETE /users/{id}** - удаление пользователя

#### API для подписок
- ✅ **POST /users/{id}/subscriptions** - добавление подписки пользователю
- ✅ **GET /users/{id}/subscriptions** - получение списка подписок пользователя
- ✅ **DELETE /users/{id}/subscriptions/{sub_id}** - удаление подписки
- ✅ **GET /subscriptions/top** - получить ТОП-3 популярных подписок

### Дополнительно реализовано
- ✅ **Unit тесты**
- ✅ **Интеграционные тесты** - с H2 базой данных
- ✅ **JaCoCo отчеты** - HTML отчеты о покрытии кода
- ✅ **OpenAPI/Swagger** - документация API
- ✅ **Валидация данных** - с использованием Bean Validation
- ✅ **Обработка исключений** - глобальный обработчик
- ✅ **Spring Boot Actuator** - мониторинг и health checks

### Запуск :

git clone https://github.com/VasiliiKharchenko/demo-tech.git
cd demo-tech

./mvnw clean test jacoco:report package

docker build -t demo-tech-app:latest .

docker-compose up -d

,либо:

git clone https://github.com/VasiliiKharchenko/demo-tech.git && cd demo-tech && ./mvnw clean package -DskipTests && docker-compose up --build -d


## 📋 URL для проверки функциональности

После запуска проекта доступны следующие URL:

### Основные эндпоинты
- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Мониторинг и проверки
- **Health Check**: http://localhost:8080/actuator/health
- **Application Info**: http://localhost:8080/actuator/info

### Отчеты о тестировании (после сборки)
- **JaCoCo HTML отчет**: `target/site/jacoco/index.html`
- **Surefire отчет**: `target/site/surefire-report.html`

### База данных (в Docker)
- **PostgreSQL**: localhost:5432
    - Database: `demotech`
    - Username: `postgres`
    - Password: `postgres`