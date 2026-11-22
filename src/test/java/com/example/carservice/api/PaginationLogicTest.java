package com.example.carservice.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.carservice.exception.InvalidArgumentException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PaginationLogicTest {
    private final OrderPageableResolver resolver = new OrderPageableResolver();

    @Test
    void clampsLargePageSizeAndAddsIdTieBreaker() {
        Pageable pageable = resolver.resolve(
                PageRequest.of(0, ApiMessages.MAX_PAGE_SIZE + 1, Sort.by(ApiMessages.ORDER_STATUS).descending()),
                null);

        assertThat(pageable.getPageSize()).isEqualTo(ApiMessages.MAX_PAGE_SIZE);
        assertThat(pageable.getSort().toList()).containsExactly(
                Sort.Order.desc(ApiMessages.ORDER_STATUS),
                Sort.Order.asc(ApiMessages.ORDER_ID));
    }

    @Test
    void defaultsUnsortedPageToIdAscending() {
        Pageable pageable = resolver.resolve(PageRequest.of(0, ApiMessages.DEFAULT_PAGE_SIZE), null);

        assertThat(pageable.getSort().toList()).containsExactly(Sort.Order.asc(ApiMessages.ORDER_ID));
    }

    @Test
    void doesNotDuplicateRequestedIdSort() {
        Pageable pageable = resolver.resolve(
                PageRequest.of(0, ApiMessages.DEFAULT_PAGE_SIZE, Sort.by(ApiMessages.ORDER_ID).descending()),
                null);

        assertThat(pageable.getSort().toList()).containsExactly(Sort.Order.desc(ApiMessages.ORDER_ID));
    }

    @ParameterizedTest
    @MethodSource("invalidPageableRequests")
    void rejectsNegativePageAndNonWhitelistedSort(Pageable pageable, Integer requestedPage) {
        assertThatThrownBy(() -> resolver.resolve(pageable, requestedPage))
                .isInstanceOf(InvalidArgumentException.class);
    }

    static Stream<Arguments> invalidPageableRequests() {
        return Stream.of(
                Arguments.of(PageRequest.of(0, ApiMessages.DEFAULT_PAGE_SIZE), -1),
                Arguments.of(
                        PageRequest.of(
                                0,
                                ApiMessages.DEFAULT_PAGE_SIZE,
                                Sort.by("works")),
                        null));
    }
}
