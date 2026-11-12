package com.example.library.exception;

import com.example.library.controller.GenreRestController;
import com.example.library.controller.LightNovelRestController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns exceptions thrown by the REST controllers into JSON error
 * responses (RFC 7807 problem details).
 */
@RestControllerAdvice(assignableTypes = {LightNovelRestController.class,
        GenreRestController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Not found", e.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException e) {
        ProblemDetail problem =
                problem(HttpStatus.CONFLICT, "Duplicate value", e.getMessage());
        problem.setProperty("field", e.getField());
        return problem;
    }

    /**
     * Returns every failed {@code @Valid} constraint as field: message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST,
                "Validation failed", "One or more fields are invalid");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({FileStorageException.class,
            IllegalArgumentException.class,
            PropertyReferenceException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            MultipartException.class})
    public ProblemDetail handleBadRequest(Exception e) {
        return problem(HttpStatus.BAD_REQUEST, "Bad request", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException e) {
        return problem(HttpStatus.BAD_REQUEST, "Bad request",
                "Request body is missing or is not valid JSON");
    }

    private ProblemDetail problem(HttpStatus status, String title,
            String detail) {
        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
