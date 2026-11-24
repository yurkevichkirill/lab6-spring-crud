# Лабораторная работа №6 — клиент-серверное взаимодействие через Spring `@FeignClient`

Многомодульный Maven-проект из двух приложений Spring Boot:

| Модуль | Роль | Порт | Эндпоинты |
|--------|------|------|-----------|
| `order-service` | сервер: REST API заказов (H2 in-memory, JPA) | **8082** | `GET /orders/{userId}`, `GET /orders?status=…`, `GET /orders` |
| `user-service` | клиент: `@EnableFeignClients`, Feign-клиент `OrderClient` | **8081** | `GET /user/{id}/orders`, `GET /user/orders?status=…` |

```
curl ──> user-service :8081 ──(OrderClient, Feign HTTP)──> order-service :8082 ──> H2
         UserController -> UserOrderService -> OrderClient      OrderController -> OrderService -> OrderRepository
                                              └─ fallback: OrderClientFallback (Resilience4j CircuitBreaker)
```

## Версии

- Java 17, Maven 3.8+
- Spring Boot 3.5.16 (`spring-boot-starter-parent`)
- Spring Cloud 2025.0.3 (BOM `spring-cloud-dependencies` в `dependencyManagement` родительского pom;
  линейка 2025.0.x совместима с Boot 3.5.x)
- `spring-cloud-starter-openfeign`, `spring-cloud-starter-circuitbreaker-resilience4j`
  (Hystrix и Ribbon удалены из Spring Cloud 2020+, fallback реализован через Spring Cloud CircuitBreaker)
- Тесты: JUnit 5, MockMvc, Mockito, WireMock (`spring-cloud-contract-wiremock`), Instancio, Datafaker, JsonUnit

## Сборка и тесты

```bash
mvn -f pom.xml test          # тесты обоих модулей (из каталога project/)
```

## Запуск

Каждый сервис — в отдельном терминале (сначала сервер):

```bash
mvn -f order-service/pom.xml spring-boot:run     # http://localhost:8082
mvn -f user-service/pom.xml spring-boot:run      # http://localhost:8081
```

Адрес сервера для Feign задаётся свойством `order-service.url` в
`user-service/src/main/resources/application.properties` (по умолчанию `http://localhost:8082`;
Eureka не используется). Переопределить можно при запуске:
`-Dspring-boot.run.arguments=--order-service.url=http://host:port`.

### Консоль H2 (order-service)

- URL: http://localhost:8082/h2-console
- JDBC URL: `jdbc:h2:mem:orders`
- Пользователь: `sa`, пароль — пустой

Демо-данные (`order-service/src/main/resources/data.sql`): пользователь `1` — 3 заказа
(PAID, SHIPPED, NEW), пользователь `2` — 2 заказа (PAID, DELIVERED), пользователь `3` — 1 (CANCELLED).

## Демонстрация (curl)

1. Сервер напрямую:
   ```bash
   curl -s http://localhost:8082/orders/1
   curl -s "http://localhost:8082/orders?status=PAID"
   ```
2. Через клиента (user-service вызывает order-service по Feign):
   ```bash
   curl -s http://localhost:8081/user/1/orders
   curl -s "http://localhost:8081/user/orders?status=PAID"
   ```
   В логе user-service каждый вызов Feign — одна строка (`OneLineLogger`):
   ```
   ... OneLineLogger : [OrderClient#getOrdersByUserId] ---> GET http://localhost:8082/orders/1
   ... OneLineLogger : [OrderClient#getOrdersByUserId] <--- 200 GET http://localhost:8082/orders/1 (25 ms, 412 bytes) body=[...]
   ```
3. Неизвестный статус на сервере → 400:
   ```bash
   curl -s -i "http://localhost:8082/orders?status=LOST"
   ```
4. Fallback: остановите order-service и повторите
   ```bash
   curl -s http://localhost:8081/user/1/orders      # []
   ```
   В логе: три попытки (`Retryer`, строки `---> RETRYING` и `<--- ERROR ConnectException`),
   затем `OrderClientFallback : Order-Service unavailable: fallback ...`.

## Конфигурация Feign (user-service)

- `config/FeignClientConfig` — `Logger.Level.FULL`, `Logger` → `OneLineLogger`,
  `Retryer.Default(100 мс, 1 с, 3 попытки)`, `RequestInterceptor` (заголовок `X-Caller-Service: user-service`);
- `config/OneLineLogger extends feign.Logger` — переопределены `log`, `logRequest`,
  `logAndRebufferResponse`, `logIOException`, `logRetry`: метод, URL, статус, время, тело — одной строкой;
- `application.properties`: `spring.cloud.openfeign.client.config.order-service.connect-timeout/read-timeout`,
  `spring.cloud.openfeign.circuitbreaker.enabled=true`, `resilience4j.timelimiter.configs.default.timeout-duration`.

## Структура

```
project/
├── pom.xml                                   родитель: модули, Spring Cloud BOM, плагины
├── order-service/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/orderservice/
│       │   ├── OrderServiceApplication.java
│       │   ├── controller/OrderController.java
│       │   ├── service/OrderService.java
│       │   ├── repository/OrderRepository.java
│       │   ├── model/Order.java, OrderStatus.java
│       │   ├── dto/OrderDTO.java
│       │   ├── mapper/OrderMapper.java       (MapStruct)
│       │   └── exception/GlobalExceptionHandler.java, InvalidOrderStatusException.java
│       ├── main/resources/application.properties, data.sql
│       └── test/java/com/example/orderservice/
│           ├── controller/OrderControllerTest.java   (MockMvc)
│           └── service/OrderServiceTest.java         (Mockito)
└── user-service/
    ├── pom.xml
    └── src/
        ├── main/java/com/example/userservice/
        │   ├── UserServiceApplication.java   (@SpringBootApplication @EnableFeignClients)
        │   ├── client/OrderClient.java       (@FeignClient)
        │   ├── client/OrderClientFallback.java
        │   ├── config/FeignClientConfig.java, OneLineLogger.java
        │   ├── controller/UserController.java
        │   ├── service/UserOrderService.java
        │   └── model/Order.java
        ├── main/resources/application.properties
        └── test/java/com/example/userservice/
            ├── client/OrderClientTest.java              (Feign против WireMock + проверка лога)
            ├── client/OrderClientFallbackTest.java      (fallback при 5xx)
            ├── client/OrderServiceUnavailableTest.java  (fallback при недоступном сервере)
            ├── controller/UserControllerTest.java       (MockMvc, @MockitoBean OrderClient)
            └── service/UserOrderServiceTest.java        (Mockito)
```

## Gradle (альтернатива)

```groovy
implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
```
