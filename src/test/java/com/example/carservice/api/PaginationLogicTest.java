package com.example.carservice.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.carservice.exception.InvalidArgumentException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.mockito.Mockito;

class PaginationLogicTest {
    @Test
    void clampsLargePageSizeTo50() {
        Pageable pageable = OrderController.normalizePageable(
                PageRequest.of(0, 51, Sort.by("id").ascending()));

        assertEquals(ApiMessages.MAX_PAGE_SIZE, pageable.getPageSize());
    }

    @Test
    void keepsValidDefaultAndBoundarySizes() {
        assertEquals(ApiMessages.DEFAULT_PAGE_SIZE, OrderController.normalizePageable(
                PageRequest.of(0, ApiMessages.DEFAULT_PAGE_SIZE)).getPageSize());
        assertEquals(1, OrderController.normalizePageable(
                PageRequest.of(0, 1)).getPageSize());
        assertEquals(50, OrderController.normalizePageable(
                PageRequest.of(0, 50)).getPageSize());
    }

    @Test
    void rejectsZeroOrNegativeSize() {
        Pageable zeroSize = Mockito.mock(Pageable.class);
        org.mockito.Mockito.when(zeroSize.getPageNumber()).thenReturn(0);
        org.mockito.Mockito.when(zeroSize.getPageSize()).thenReturn(0);
        org.mockito.Mockito.when(zeroSize.getSort()).thenReturn(Sort.unsorted());
        assertThrows(InvalidArgumentException.class, () ->
                OrderController.normalizePageable(zeroSize));
    }

    @Test
    void appendsIdTieBreakerAndPreservesWhitelistedSort() {
        Pageable pageable = OrderController.normalizePageable(
                PageRequest.of(0, 20, Sort.by("status").descending()));

        assertEquals("status", pageable.getSort().toList().get(0).getProperty());
        assertEquals("id", pageable.getSort().toList().get(1).getProperty());
    }

    @Test
    void doesNotDuplicateIdAndAllowsWhitelist() {
        Pageable pageable = OrderController.normalizePageable(
                PageRequest.of(0, 20, Sort.by("id").descending()));

        assertEquals(1, pageable.getSort().stream().count());
        assertTrue(ApiMessages.ORDER_SORT_PROPERTIES.containsAll(
                java.util.Set.of("status", "priority", "finalPrice", "completedAt", "id")));
    }

    @Test
    void rejectsSortPropertyNotInWhitelist() {
        assertThrows(InvalidArgumentException.class, () ->
                OrderController.normalizePageable(
                        PageRequest.of(0, 20, Sort.by("works").ascending())));
    }
}
