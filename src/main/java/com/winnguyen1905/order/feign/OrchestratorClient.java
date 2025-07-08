package com.winnguyen1905.order.feign;

import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.winnguyen1905.order.secure.RestResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@FeignClient(name = "ORCHESTRATOR-SERVICE", url = "${microservices.orchestrator-service.url:http://orchestrator-service:8095}")
@CircuitBreaker(name = "orchestratorService")
@Retry(name = "orchestratorService")
public interface OrchestratorClient {

  /**
   * Start the saga process for a new order
   */
  @PostMapping("/api/sagas/orders/{orderId}")
  ResponseEntity<RestResponse<Map<String, UUID>>> startOrderSaga(@PathVariable("orderId") UUID orderId);
}
