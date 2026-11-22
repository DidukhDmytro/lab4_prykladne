package com.example.carservice.api;

import com.example.carservice.exception.InvalidArgumentException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class OrderPageableResolver {
    public Pageable resolve(Pageable pageable, Integer requestedPage) {
        if (requestedPage != null && requestedPage < 0) {
            throw new InvalidArgumentException(ApiMessages.TYPE_MISMATCH_DETAIL);
        }

        int size = pageable.getPageSize() <= 0
                ? ApiMessages.DEFAULT_PAGE_SIZE
                : Math.min(pageable.getPageSize(), ApiMessages.MAX_PAGE_SIZE);
        List<Sort.Order> orders = new ArrayList<>();
        boolean idPresent = false;
        for (Sort.Order order : pageable.getSort()) {
            if (!ApiMessages.ORDER_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new InvalidArgumentException(String.format(
                        ApiMessages.SORT_PROPERTY_NOT_ALLOWED,
                        order.getProperty()));
            }
            if (ApiMessages.ORDER_ID.equals(order.getProperty())) {
                idPresent = true;
            }
            orders.add(order);
        }
        if (orders.isEmpty()) {
            orders.add(Sort.Order.asc(ApiMessages.ORDER_ID));
        } else if (!idPresent) {
            orders.add(Sort.Order.asc(ApiMessages.ORDER_ID));
        }

        return PageRequest.of(
                pageable.getPageNumber(),
                size,
                Sort.by(orders));
    }
}
