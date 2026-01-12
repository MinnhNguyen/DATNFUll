package com.example.duanbe.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for handling stock-related and database errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle insufficient stock exception (custom app exception)
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(
            InsufficientStockException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", "INSUFFICIENT_STOCK");
        error.put("message", ex.getMessage());

        if (ex.getProductId() != null) {
            error.put("productId", ex.getProductId());
            error.put("requestedQuantity", ex.getRequestedQuantity());
            error.put("availableQuantity", ex.getAvailableQuantity());
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handle SQL Server THROW errors (error code 50010 from stored procedures)
     */
    @ExceptionHandler({ DataAccessException.class, SQLException.class })
    public ResponseEntity<Map<String, Object>> handleDatabaseError(Exception ex) {
        String message = ex.getMessage();

        // Check if this is our stock error (error code 50010)
        if (message != null && (message.contains("50010") ||
                message.contains("Số lượng tồn kho không đủ") ||
                message.contains("không đủ để bán"))) {

            // Extract the actual error message (after the error code)
            String cleanMessage = message;
            if (message.contains(":")) {
                cleanMessage = message.substring(message.lastIndexOf(":") + 1).trim();
            }

            Map<String, Object> error = new HashMap<>();
            error.put("error", "INSUFFICIENT_STOCK");
            error.put("message", cleanMessage);

            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }

        // Other database errors
        Map<String, Object> error = new HashMap<>();
        error.put("error", "DATABASE_ERROR");
        error.put("message", "Đã xảy ra lỗi khi xử lý yêu cầu");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
