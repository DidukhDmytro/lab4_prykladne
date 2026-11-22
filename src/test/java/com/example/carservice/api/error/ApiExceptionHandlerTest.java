package com.example.carservice.api.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.carservice.exception.EntityAlreadyExistsException;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.InvalidStateTransitionException;
import com.example.carservice.exception.MechanicAlreadyAssignedException;
import com.example.carservice.exception.MechanicNotCompatibleException;
import com.example.carservice.exception.OrderModificationNotAllowedException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.context.request.ServletWebRequest;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();
    private final ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest());

    @ParameterizedTest
    @MethodSource("conflicts")
    void mapsConflictExceptions(Exception exception) {
        ResponseEntity<ProblemDetail> response = handler.handleConflict(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Conflict", response.getBody().getTitle());
        assertSafe(response.getBody());
    }

    static Stream<Arguments> conflicts() {
        return Stream.of(
                Arguments.of(new EntityAlreadyExistsException("Already exists")),
                Arguments.of(new InvalidStateTransitionException("Invalid transition")),
                Arguments.of(new OrderModificationNotAllowedException("Closed order")),
                Arguments.of(new MechanicAlreadyAssignedException("Busy mechanic")),
                Arguments.of(new MechanicNotCompatibleException("Incompatible mechanic")),
                Arguments.of(new ObjectOptimisticLockingFailureException("RepairOrder", 1)));
    }

    @Test
    void mapsNotFoundAndBadRequest() {
        ResponseEntity<ProblemDetail> notFound = handler.handleNotFound(
                new EntityNotFoundException("Customer '1' was not found."),
                request);
        ResponseEntity<ProblemDetail> badRequest = handler.handleBadRequest(
                new InvalidArgumentException("Bad identifier"),
                request);

        assertEquals(HttpStatus.NOT_FOUND, notFound.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());
        assertSafe(notFound.getBody());
        assertSafe(badRequest.getBody());
    }

    @Test
    void mapsDataIntegrityAndUnexpectedErrorsSafely() {
        ResponseEntity<ProblemDetail> integrity = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("SELECT * FROM secret_table"),
                request);
        ResponseEntity<ProblemDetail> unexpected = handler.handleUnexpected(
                new IllegalStateException("SELECT * FROM secret_table"),
                request);

        assertEquals(HttpStatus.CONFLICT, integrity.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, unexpected.getStatusCode());
        assertFalse(integrity.getBody().toString().contains("SELECT"));
        assertFalse(unexpected.getBody().toString().contains("IllegalStateException"));
        assertSafe(unexpected.getBody());
    }

    private void assertSafe(ProblemDetail body) {
        String text = body.toString();
        assertFalse(text.contains("SELECT"));
        assertFalse(text.contains("Exception"));
        assertFalse(text.contains("at com."));
        assertTrue(text.contains("status"));
    }
}
