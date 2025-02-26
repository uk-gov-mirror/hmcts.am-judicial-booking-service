package uk.gov.hmcts.reform.judicialbooking.controller.advice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.reform.judicialbooking.controller.WelcomeController;
import uk.gov.hmcts.reform.judicialbooking.controller.advice.exception.BadRequestException;
import uk.gov.hmcts.reform.judicialbooking.controller.advice.exception.InvalidRequest;
import uk.gov.hmcts.reform.judicialbooking.controller.advice.exception.ResourceNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

class JudicialBookingControllerAdviceTest {

    private final JudicialBookingControllerAdvice csda = new JudicialBookingControllerAdvice();
    private final HttpServletRequest servletRequestMock = mock(HttpServletRequest.class);
    private final WelcomeController welcomeController = new WelcomeController();

    @Test
    void customValidationError() {
        InvalidRequest invalidRequestException = mock(InvalidRequest.class);
        ResponseEntity<Object> responseEntity = csda.customValidationError(invalidRequestException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void handleMethodArgumentNotValidException() {
        MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);
        ResponseEntity<Object> responseEntity = csda.handleMethodArgumentNotValidException(
            servletRequestMock, methodArgumentNotValidException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException resourceNotFoundException = mock(ResourceNotFoundException.class);
        ResponseEntity<Object> responseEntity = csda.handleResourceNotFoundException(
            servletRequestMock,resourceNotFoundException);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void handleHttpMessageConversionException() {
        HttpMessageConversionException httpMessageConversionException = mock(HttpMessageConversionException.class);
        ResponseEntity<Object> responseEntity = csda.handleHttpMessageConversionException(
            servletRequestMock, httpMessageConversionException);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getStatusCodeValue());
    }

    @Test
    void handleUnknownException() {
        Exception exception = mock(Exception.class);
        ResponseEntity<Object> responseEntity = csda.handleUnknownException(servletRequestMock, exception);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getStatusCodeValue());

    }

    @Test
    void getTimeStamp() {
        String time = csda.getTimeStamp();
        assertEquals(time.substring(0,16), new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH).format(new Date()));
    }

    @Test
    void testInvalidRequest() {
        Assertions.assertThrows(InvalidRequest.class, () -> {
            welcomeController.getException("invalidRequest");
        });
    }

    @Test
    void testResourceNotFoundException() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            welcomeController.getException("resourceNotFoundException");
        });
    }

    @Test
    void testHttpMessageConversionException() {
        Assertions.assertThrows(HttpMessageConversionException.class, () -> {
            welcomeController.getException("httpMessageConversionException");
        });
    }

    @Test
    void testBadRequestException() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            welcomeController.getException("badRequestException");
        });
    }
}
