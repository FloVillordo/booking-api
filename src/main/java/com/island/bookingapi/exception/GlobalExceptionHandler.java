package com.island.bookingapi.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles UnavailableDatesException, throw when try to book a days not available
     *
     * @param ex      UnavailableDatesException
     * @param request
     * @return ResponseEntity with HTTP status 400
     */
    @ExceptionHandler(value = {UnavailableDatesException.class})
    protected ResponseEntity<Object> handleUnavailableDates(UnavailableDatesException ex, WebRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder().timestamp(new Date()).httpStatus(HttpStatus.BAD_REQUEST).message(ex.getMessage()).build();
        return this.handleExceptionInternal(ex, errorDetail, new HttpHeaders(), errorDetail.getHttpStatus(), request);
    }

    /**
     * Handles BookingNotFoundException, reports booking not found
     *
     * @param ex      BookingNotFoundException
     * @param request
     * @return ResponseEntity with HTTP status 404
     */
    @ExceptionHandler(value = {BookingNotFoundException.class})
    protected ResponseEntity<Object> handleBookingNotFound(BookingNotFoundException ex, WebRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder().timestamp(new Date()).httpStatus(HttpStatus.NOT_FOUND).message(ex.getMessage()).build();
        return this.handleExceptionInternal(ex, errorDetail, new HttpHeaders(), errorDetail.getHttpStatus(), request);
    }

    /**
     * Handles CancelledBookingException, reports cancelled booking trying to update
     *
     * @param ex      CancelledBookingException
     * @param request
     * @return ResponseEntity with HTTP status 405
     */
    @ExceptionHandler(value = {CancelledBookingException.class})
    protected ResponseEntity<Object> handleCancelledBooking(CancelledBookingException ex, WebRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder().timestamp(new Date()).httpStatus(HttpStatus.METHOD_NOT_ALLOWED).message(ex.getMessage()).build();
        return this.handleExceptionInternal(ex, errorDetail, new HttpHeaders(), errorDetail.getHttpStatus(), request);
    }


    /**
     * Handles ConstraintViolationException, reports the result of constraint violations
     *
     * @param ex      ConstraintViolationException
     * @param request
     * @return ResponseEntity with HTTP status 400
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() + " " +
                    violation.getPropertyPath() + ": " + violation.getMessage());
        }

        ErrorDetail errorDetail = ErrorDetail.builder().timestamp(new Date()).httpStatus(HttpStatus.BAD_REQUEST).message(ex.getLocalizedMessage()).details(errors).build();
        return new ResponseEntity<>(
                errorDetail, new HttpHeaders(), errorDetail.getHttpStatus());
    }

    /**
     * Handles MethodArgumentTypeMismatchException, thrown when method argument is not the expected type
     *
     * @param ex      MethodArgumentTypeMismatchException
     * @param request
     * @return ResponseEntity with HTTP status 400
     */
    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        String error =
                ex.getName() + " should be of type " + ex.getRequiredType().getName();
        ErrorDetail errorDetail = ErrorDetail.builder().timestamp(new Date()).httpStatus(HttpStatus.BAD_REQUEST).message(ex.getLocalizedMessage()).details(Arrays.asList(error)).build();
        return new ResponseEntity<>(
                errorDetail, new HttpHeaders(), errorDetail.getHttpStatus());
    }

    /**
     * Handles MethodArgumentNotValidException, thrown when argument annotated with @Valid failed validation
     *
     * @param ex      MethodArgumentNotValidException
     * @param request
     * @return ResponseEntity with HTTP status 400
     */

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }
        ErrorDetail errorDetail = ErrorDetail.builder().timestamp(new Date()).httpStatus(HttpStatus.BAD_REQUEST).message(ex.getLocalizedMessage()).details(errors).build();
        return this.handleExceptionInternal(
                ex, errorDetail, headers, errorDetail.getHttpStatus(), request);
    }

    /**
     * Handles MethodArgumentNotValidException, thrown when method argument is not the expected type
     *
     * @param ex      MissingServletRequestParameterException
     * @param request
     * @return ResponseEntity with HTTP status 400
     */

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";
        ErrorDetail errorDetail = ErrorDetail.builder().timestamp(new Date()).httpStatus(HttpStatus.BAD_REQUEST).message(ex.getLocalizedMessage()).details(Arrays.asList(error)).build();
        return new ResponseEntity<>(errorDetail, new HttpHeaders(), errorDetail.getHttpStatus());
    }

    /**
     * Handle previously unhandled exceptions to keep response format
     *
     * @param ex      Exception
     * @param request
     * @return ResponseEntity with HTTP status 500
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            Exception ex, WebRequest request) {
        ErrorDetail errorDetail = ErrorDetail.builder().timestamp(new Date()).httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).message(ex.getMessage()).build();
        return new ResponseEntity<>(
                errorDetail, new HttpHeaders(), errorDetail.getHttpStatus());
    }
}

