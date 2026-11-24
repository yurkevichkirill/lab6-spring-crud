package com.example.userservice.client;

import com.example.userservice.model.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

/** Feign-клиент против WireMock-заглушки Order-Service. */
@SpringBootTest(properties = "order-service.url=http://localhost:${wiremock.server.port}")
@AutoConfigureWireMock(port = 0)
@ExtendWith(OutputCaptureExtension.class)
class OrderClientTest {

    private static final String ORDERS_USER_1 = """
            [
              {"id":1,"userId":"1","product":"Laptop","quantity":1,"amount":2450.00,
               "status":"PAID","createdAt":"2026-10-05T12:00:00"},
              {"id":2,"userId":"1","product":"USB-C Hub","quantity":2,"amount":89.90,
               "status":"SHIPPED","createdAt":"2026-10-05T12:05:00"}
            ]
            """;

    @Autowired
    private OrderClient orderClient;

    @Test
    void getOrdersByUserIdCallsPathAndDeserializes() {
        stubFor(get(urlEqualTo("/orders/1"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ORDERS_USER_1)));

        List<Order> orders = orderClient.getOrdersByUserId("1");

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).id()).isEqualTo(1L);
        assertThat(orders.get(0).userId()).isEqualTo("1");
        assertThat(orders.get(0).product()).isEqualTo("Laptop");
        assertThat(orders.get(0).amount()).isEqualByComparingTo(new BigDecimal("2450.00"));
        assertThat(orders.get(1).status()).isEqualTo("SHIPPED");
        assertThat(orders.get(1).createdAt()).hasMinute(5);

        // путь с подставленным userId и заголовок от RequestInterceptor
        verify(getRequestedFor(urlEqualTo("/orders/1"))
                .withHeader("X-Caller-Service", equalTo("user-service")));
    }

    @Test
    void getOrdersByStatusSendsRequestParam() {
        stubFor(get(urlPathEqualTo("/orders"))
                .withQueryParam("status", equalTo("PAID"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [{"id":4,"userId":"2","product":"Monitor","quantity":1,
                                  "amount":780.00,"status":"PAID","createdAt":"2026-10-05T12:00:00"}]
                                """)));

        List<Order> orders = orderClient.getOrdersByStatus("PAID");

        assertThat(orders).singleElement().satisfies(o -> {
            assertThat(o.userId()).isEqualTo("2");
            assertThat(o.status()).isEqualTo("PAID");
        });
        verify(getRequestedFor(urlEqualTo("/orders?status=PAID")));
    }

    @Test
    void oneLineLoggerWritesMethodUrlAndStatus(CapturedOutput output) {
        stubFor(get(urlEqualTo("/orders/7"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));

        orderClient.getOrdersByUserId("7");

        List<String> feignLines = output.getAll().lines()
                .filter(l -> l.contains("OneLineLogger"))
                .filter(l -> l.contains("/orders/7"))
                .toList();
        assertThat(feignLines).anySatisfy(l ->
                assertThat(l).contains("[OrderClient#getOrdersByUserId]").contains("---> GET http://localhost:")
                        .endsWith("/orders/7"));
        assertThat(feignLines).anySatisfy(l ->
                assertThat(l).contains("<--- 200 GET http://localhost:").contains("/orders/7").contains("body=[]"));
    }
}
