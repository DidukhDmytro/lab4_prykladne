package com.example.carservice.api.error;

import com.example.carservice.api.ApiMessages;
import com.example.carservice.api.ApiPaths;
import com.example.carservice.exception.EntityAlreadyExistsException;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.InvalidStateTransitionException;
import com.example.carservice.exception.MechanicAlreadyAssignedException;
import com.example.carservice.exception.MechanicNotCompatibleException;
import com.example.carservice.exception.OrderModificationNotAllowedException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Business-rule violations are conflicts because the HTTP request is well-formed and valid,
     * but the current state of the resource prevents the requested operation. They are not 422
     * responses because the request syntax and validation constraints are not at fault.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST,
                ApiMessages.BAD_REQUEST_TITLE,
                ApiMessages.VALIDATION_DETAIL,
                ApiMessages.VALIDATION_TYPE,
                request);
        List<Map<String, String>> errors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            Map<String, String> item = new LinkedHashMap<>();
            item.put(ApiMessages.VALIDATION_FIELD_PROPERTY, error.getField());
            item.put(ApiMessages.VALIDATION_MESSAGE_PROPERTY, error.getDefaultMessage());
            errors.add(item);
        });
        problem.setProperty(ApiMessages.VALIDATION_ERRORS_PROPERTY, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return problemResponse(
                HttpStatus.BAD_REQUEST,
                ApiMessages.BAD_REQUEST_TITLE,
                ApiMessages.MALFORMED_REQUEST_DETAIL,
                ApiMessages.BAD_REQUEST_TYPE,
                request,
                headers);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        if (exception instanceof MethodArgumentTypeMismatchException) {
            return problemResponse(
                    HttpStatus.BAD_REQUEST,
                    ApiMessages.BAD_REQUEST_TITLE,
                    ApiMessages.TYPE_MISMATCH_DETAIL,
                    ApiMessages.BAD_REQUEST_TYPE,
                    request,
                    headers);
        }
        return super.handleTypeMismatch(exception, headers, status, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException exception,
            WebRequest request) {
        return problemEntity(
                HttpStatus.BAD_REQUEST,
                ApiMessages.BAD_REQUEST_TITLE,
                ApiMessages.TYPE_MISMATCH_DETAIL,
                ApiMessages.BAD_REQUEST_TYPE,
                request);
    }

    @ExceptionHandler({
            InvalidArgumentException.class,
            PropertyReferenceException.class
    })
    public ResponseEntity<ProblemDetail> handleBadRequest(
            Exception exception,
            WebRequest request) {
        String detail = exception instanceof InvalidArgumentException
                ? exception.getMessage()
                : ApiMessages.TYPE_MISMATCH_DETAIL;
        return problemEntity(
                HttpStatus.BAD_REQUEST,
                ApiMessages.BAD_REQUEST_TITLE,
                detail,
                ApiMessages.BAD_REQUEST_TYPE,
                request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(
            EntityNotFoundException exception,
            WebRequest request) {
        return problemEntity(
                HttpStatus.NOT_FOUND,
                ApiMessages.NOT_FOUND_TITLE,
                exception.getMessage(),
                ApiMessages.NOT_FOUND_TYPE,
                request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return problemResponse(
                HttpStatus.NOT_FOUND,
                ApiMessages.NOT_FOUND_TITLE,
                ApiMessages.UNKNOWN_ROUTE_DETAIL,
                ApiMessages.UNKNOWN_ROUTE_TYPE,
                request,
                headers);
    }

    @ExceptionHandler({
            EntityAlreadyExistsException.class,
            InvalidStateTransitionException.class,
            OrderModificationNotAllowedException.class,
            MechanicAlreadyAssignedException.class,
            MechanicNotCompatibleException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ProblemDetail> handleConflict(
            Exception exception,
            WebRequest request) {
        String detail = exception instanceof EntityAlreadyExistsException
                || exception instanceof InvalidStateTransitionException
                || exception instanceof OrderModificationNotAllowedException
                || exception instanceof MechanicAlreadyAssignedException
                || exception instanceof MechanicNotCompatibleException
                ? exception.getMessage()
                : ApiMessages.CONFLICT_DETAIL;
        return problemEntity(
                HttpStatus.CONFLICT,
                ApiMessages.CONFLICT_TITLE,
                detail,
                ApiMessages.CONFLICT_TYPE,
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            WebRequest request) {
        return problemEntity(
                HttpStatus.CONFLICT,
                ApiMessages.CONFLICT_TITLE,
                ApiMessages.DATA_INTEGRITY_DETAIL,
                ApiMessages.DATA_INTEGRITY_TYPE,
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception exception,
            WebRequest request) {
        log.error("Unhandled exception while processing API request", exception);
        return problemEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiMessages.INTERNAL_ERROR_TITLE,
                ApiMessages.INTERNAL_ERROR_DETAIL,
                ApiMessages.INTERNAL_ERROR_TYPE,
                request);
    }

    private ResponseEntity<ProblemDetail> problemEntity(
            HttpStatus status,
            String title,
            String detail,
            String type,
            WebRequest request) {
        return ResponseEntity.status(status)
                .body(problem(status, title, detail, type, request));
    }

    private ResponseEntity<Object> problemResponse(
            HttpStatus status,
            String title,
            String detail,
            String type,
            WebRequest request,
            HttpHeaders headers) {
        return ResponseEntity.status(status)
                .headers(headers)
                .body(problem(status, title, detail, type, request));
    }

    private ProblemDetail problem(
            HttpStatus status,
            String title,
            String detail,
            String type,
            WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(type));
        problem.setInstance(instance(request));
        return problem;
    }

    private URI instance(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return URI.create(servletRequest.getRequest().getRequestURI());
        }
        String description = request.getDescription(false);
        if (description.startsWith("uri=")) {
            return URI.create(description.substring(4));
        }
        return URI.create(ApiPaths.ROOT_PATH);
    }
}
