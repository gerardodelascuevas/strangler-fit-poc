package com.modern.catalog.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ErrorDTO {

    private final String code;
    private final String message;
    private final LocalDateTime timestamp;
    private final List<String> details;
    private final String path;

    public ErrorDTO(String code, String message, String path, List<String> details) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.path = path;
        this.details = details;
    }

    public ErrorDTO(String code, String message, String path) {
        this(code, message, path, null);
    }

    public ErrorDTO(String code, String message) {
        this(code, message, null, null);
    }

    @Schema(description = "Error code identifier", example = "PRODUCT_NOT_FOUND")
    public String getCode() {
        return code;
    }

    @Schema(description = "Human-readable error message", example = "Product not found with id: 99")
    public String getMessage() {
        return message;
    }

    @Schema(description = "Timestamp when the error occurred", example = "2024-01-15T10:30:00")
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Schema(description = "Detailed validation errors, if any")
    public List<String> getDetails() {
        return details;
    }

    @Schema(description = "Request path that caused the error", example = "/api/products/reports/products/99")
    public String getPath() {
        return path;
    }
}
