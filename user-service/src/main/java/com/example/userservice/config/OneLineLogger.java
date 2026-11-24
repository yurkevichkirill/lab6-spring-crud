package com.example.userservice.config;

import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Пользовательский логгер Feign: каждая запись — одна строка.
 * <ul>
 *   <li>запрос: {@code [OrderClient#getOrdersByUserId(String)] ---> GET http://localhost:8082/orders/1};</li>
 *   <li>ответ: {@code [...] <--- 200 GET http://... (15 ms) body=[...]} — метод, URL, статус,
 *       время и (на уровне FULL) сокращённое тело;</li>
 *   <li>ошибки и повторы — тоже одной строкой.</li>
 * </ul>
 */
public class OneLineLogger extends Logger {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(OneLineLogger.class);
    static final int MAX_BODY = 300;

    @Override
    protected void log(String configKey, String format, Object... args) {
        // Форматировать сообщение лога в одну строку
        String message = String.format(methodTag(configKey) + format, args)
                .replaceAll("\\s*[\\r\\n]+\\s*", " ");
        LOG.info(message);
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        log(configKey, "---> %s %s", request.httpMethod(), request.url());
    }

    @Override
    protected void logRetry(String configKey, Level logLevel) {
        log(configKey, "---> RETRYING");
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel,
                                              Response response, long elapsedTime) throws IOException {
        Request request = response.request();
        byte[] body = null;
        if (response.body() != null) {
            body = Util.toByteArray(response.body().asInputStream());
        }
        String bodyPart = "";
        if (logLevel.ordinal() >= Level.FULL.ordinal() && body != null && body.length > 0) {
            bodyPart = " body=" + abbreviate(new String(body, StandardCharsets.UTF_8));
        }
        log(configKey, "<--- %d %s %s (%d ms, %d bytes)%s",
                response.status(), request.httpMethod(), request.url(), elapsedTime,
                body == null ? 0 : body.length, bodyPart);
        return response.toBuilder().body(body).build();
    }

    @Override
    protected IOException logIOException(String configKey, Level logLevel,
                                         IOException ioe, long elapsedTime) {
        log(configKey, "<--- ERROR %s: %s (%d ms)",
                ioe.getClass().getSimpleName(), ioe.getMessage(), elapsedTime);
        return ioe;
    }

    static String abbreviate(String text) {
        String oneLine = text.replaceAll("\\s*[\\r\\n]+\\s*", " ");
        return oneLine.length() <= MAX_BODY ? oneLine : oneLine.substring(0, MAX_BODY) + "...";
    }
}
