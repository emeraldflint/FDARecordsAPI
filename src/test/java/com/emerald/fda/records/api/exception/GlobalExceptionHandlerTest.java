package com.emerald.fda.records.api.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
    }

    @Test
    void handleRestClientException_ShouldReturnServiceUnavailable() {
        // given
        RestClientException exception = new RestClientException("API connection error");

        // when
        ResponseEntity<Object> response = exceptionHandler.handleRestClientException(exception, webRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody).containsKey("timestamp");
        assertThat(responseBody).containsEntry("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(responseBody).containsEntry("error", "Service Unavailable");
        assertThat(responseBody).containsEntry("message", "Error connecting to FDA API: API connection error");
        assertThat(responseBody).containsEntry("path", "uri=/test");
    }

    @Test
    void handleHttpClientErrorException_ShouldReturnOriginalStatusCode() {
        // given
        HttpClientErrorException exception = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "Resource not found".getBytes(),
                null
        );

        // when
        ResponseEntity<Object> response = exceptionHandler.handleHttpClientErrorException(exception, webRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        var responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody).containsKey("timestamp");
        assertThat(responseBody).containsEntry("status", HttpStatus.NOT_FOUND.value());
        assertThat(responseBody).containsEntry("error", "FDA API Error");
        assertThat(responseBody).containsKey("message");
        assertThat(responseBody).containsEntry("path", "uri=/test");
    }

    @Test
    void handleConstraintViolationException_ShouldReturnBadRequest() {
        // given
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", Set.of());

        // when
        ResponseEntity<Object> response = exceptionHandler.handleConstraintViolationException(exception, webRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody).containsKey("timestamp");
        assertThat(responseBody).containsEntry("status", HttpStatus.BAD_REQUEST.value());
        assertThat(responseBody).containsEntry("error", "Bad Request");
        assertThat(responseBody).containsEntry("message", "Validation error: Validation failed");
        assertThat(responseBody).containsEntry("path", "uri=/test");
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturnBadRequestWithFieldErrors() throws Exception {
        // given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "applicationNumber", "must not be blank");
        FieldError fieldError2 = new FieldError("object", "manufwhenurerName", "must not be blank");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // when
        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(exception, webRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody).containsKey("timestamp");
        assertThat(responseBody).containsEntry("status", HttpStatus.BAD_REQUEST.value());
        assertThat(responseBody).containsEntry("error", "Validation Error");
        assertThat(responseBody).containsEntry("path", "uri=/test");

        Map<String, String> errors = (Map<String, String>) responseBody.get("errors");
        assertThat(errors).containsEntry("applicationNumber", "must not be blank");
        assertThat(errors).containsEntry("manufwhenurerName", "must not be blank");
    }

    @Test
    void handleAllExceptions_ShouldReturnInternalServerError() {
        // given
        Exception exception = new RuntimeException("Unexpected error");

        // when
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception, webRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertThat(responseBody).containsKey("timestamp");
        assertThat(responseBody).containsEntry("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(responseBody).containsEntry("error", "Internal Server Error");
        assertThat(responseBody).containsEntry("message", "An unexpected error occurred: Unexpected error");
        assertThat(responseBody).containsEntry("path", "uri=/test");
    }
}