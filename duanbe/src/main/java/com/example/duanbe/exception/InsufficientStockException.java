package com.example.duanbe.exception;

/**
 * Exception thrown when there is insufficient stock to complete an order.
 * Used in stock concurrency control to prevent oversell scenarios.
 */
public class InsufficientStockException extends RuntimeException {

    private final Integer productId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;

    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }

    public InsufficientStockException(String message, Integer productId,
            Integer requestedQuantity, Integer availableQuantity) {
        super(message);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}
