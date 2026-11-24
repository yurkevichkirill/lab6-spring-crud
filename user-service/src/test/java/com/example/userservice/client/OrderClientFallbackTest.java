package com.example.userservice.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/** Fallback при ответе Order-Service ошибкой 5xx. */
@SpringBootTest(properties = "order-service.url=http://localhost:${wiremock.server.port}")
@AutoConfigureWireMock(port = 0)
class OrderClientFallbackTest {

    @Autowired
    private OrderClient orderClient;

    @Test
    void serverErrorTriggersFallbackForUserOrders() {
        stubFor(get(urlEqualTo("/orders/1")).willReturn(aResponse().withStatus(503)));

        assertThat(orderClient.getOrdersByUserId("1")).isEmpty();
    }

    @Test
    void serverErrorTriggersFallbackForStatusFilter() {
        stubFor(get(urlPathEqualTo("/orders")).willReturn(aResponse().withStatus(500)));

        assertThat(orderClient.getOrdersByStatus("PAID")).isEmpty();
    }
}
