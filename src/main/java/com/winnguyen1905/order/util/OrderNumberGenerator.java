package com.winnguyen1905.order.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.springframework.stereotype.Component;

/**
 * Utility class for generating unique order numbers
 */
@Component
public class OrderNumberGenerator {
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Random RANDOM = new Random();
    
    /**
     * Generates a unique order number in the format: ORD-YYYYMMDD-XXXXX
     * where XXXXX is a random 5-digit number
     * 
     * @return Unique order number
     */
    public String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DATE_FORMAT);
        String randomPart = String.format("%05d", RANDOM.nextInt(100000));
        
        return String.format("ORD-%s-%s", datePart, randomPart);
    }
} 
