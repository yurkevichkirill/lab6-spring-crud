package com.example.userservice.client;

import com.example.userservice.config.FeignClientConfig;
import com.example.userservice.model.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Декларативный HTTP-клиент Order-Service.
 *
 * <ul>
 *   <li>{@code name} — логическое имя сервиса (используется как ключ конфигурации
 *       {@code spring.cloud.openfeign.client.config.order-service.*} и имя circuit breaker);</li>
 *   <li>{@code url} — адрес сервера из свойства {@code order-service.url}
 *       (Eureka не используется);</li>
 *   <li>{@code configuration} — логгер {@code OneLineLogger}, уровень логирования, Retryer,
 *       перехватчик запросов;</li>
 *   <li>{@code fallback} — ответ при недоступности Order-Service (через Spring Cloud
 *       CircuitBreaker / Resilience4j).</li>
 * </ul>
 */
@FeignClient(
        name = "order-service",
        url = "${order-service.url}",
        configuration = FeignClientConfig.class,
        fallback = OrderClientFallback.class)
public interface OrderClient {

    /**
     * Заказы пользователя.
     * <p>HTTP: {@code GET {order-service.url}/orders/{userId}}</p>
     *
     * @param userId идентификатор пользователя (подставляется в путь)
     * @return список заказов; пустой список, если заказов нет или сервер недоступен
     */
    @GetMapping("/orders/{userId}")
    List<Order> getOrdersByUserId(@PathVariable("userId") String userId);

    /**
     * Заказы с заданным статусом.
     * <p>HTTP: {@code GET {order-service.url}/orders?status={orderStatus}}</p>
     *
     * @param orderStatus статус заказа (NEW, PAID, SHIPPED, DELIVERED, CANCELLED),
     *                    передаётся как query-параметр {@code status}
     * @return список заказов; пустой список при недоступности сервера
     */
    @GetMapping("/orders")
    List<Order> getOrdersByStatus(@RequestParam("status") String orderStatus);
}
