package com.ecommerce.order.controller;

import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "productId") String productId,
            @RequestParam(value = "quantity") int quantity) {
        
        log.info("Received order request: userId={}, productId={}, quantity={}", 
                userId, productId, quantity);
        
        try {
            orderService.createOrder(userId, productId, quantity);
            return ResponseEntity.ok("Order created successfully");
        } catch (Exception e) {
            log.error("Error creating order", e);
            return handleException(e);
        }
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", e.getMessage());
        errorResponse.put("stackTrace", sw.toString());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping
    public String test() {
        log.info("Received order request");
        return "Order test";
    }
} 