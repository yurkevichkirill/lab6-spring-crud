package com.example.userservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Feign-клиента {@code OrderClient}: логирование, повторные попытки,
 * перехватчик запросов. Таймауты — в application.properties
 * ({@code spring.cloud.openfeign.client.config.order-service.*}).
 */
@Configuration
public class FeignClientConfig {

    // Установите уровень логирования
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    // Зарегистрируйте пользовательский логгер
    @Bean
    public Logger logger() {
        return new OneLineLogger();
    }

    /** Повторные попытки: начальный интервал 100 мс, максимум 1 с, всего 3 попытки. */
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100, 1000, 3);
    }

    /** Перехватчик: помечает каждый запрос заголовком с именем вызывающего сервиса. */
    @Bean
    public RequestInterceptor callerHeaderInterceptor() {
        return template -> template.header("X-Caller-Service", "user-service");
    }
}
