package com.example.carservice.api;

import com.example.carservice.api.dto.AddWorkRequest;
import com.example.carservice.api.dto.AddWorksRequest;
import com.example.carservice.api.dto.AssignMechanicRequest;
import com.example.carservice.api.dto.ChangePriorityRequest;
import com.example.carservice.api.dto.CreateOrderRequest;
import com.example.carservice.api.dto.OrderResponse;
import com.example.carservice.api.dto.PageResponse;
import com.example.carservice.api.dto.PresentResponse;
import com.example.carservice.api.dto.PriceResponse;
import com.example.carservice.api.mapper.OrderMapper;
import com.example.carservice.api.mapper.PageMapper;
import com.example.carservice.api.mapper.PresentMapper;
import com.example.carservice.api.mapper.PriceMapper;
import com.example.carservice.domain.enumerate.OrderStatus;
import com.example.carservice.domain.Present;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.ServiceMessages;
import com.example.carservice.service.RepairOrderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final RepairOrderService repairOrderService;
    private final OrderMapper orderMapper;
    private final PriceMapper priceMapper;
    private final PresentMapper presentMapper;
    private final PageMapper pageMapper;

    /**
     * Lists repair orders by optional status. Page size defaults to 20 and Spring clamps page size
     * to 50 through the configured maximum. Sort properties are restricted to the API whitelist.
     * An {@code id} sort is appended as a tie-breaker when not already requested.
     */
    @GetMapping(ApiPaths.ORDERS)
    public PageResponse<OrderResponse> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = ApiMessages.DEFAULT_PAGE_SIZE, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        Pageable validatedPageable = normalizePageable(pageable);
        Page<RepairOrder> page = repairOrderService.findOrders(status, validatedPageable);
        return pageMapper.toResponse(page, orderMapper::toResponse);
    }

    @PostMapping(ApiPaths.ORDERS)
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        RepairOrder order = repairOrderService.createOrder(orderId, request.vin());
        URI location = URI.create(ApiPaths.ORDERS + "/" + orderId);
        return ResponseEntity.created(location).body(orderMapper.toResponse(order));
    }

    @GetMapping(ApiPaths.ORDER_BY_ID)
    public OrderResponse getOrder(@PathVariable UUID id) {
        RepairOrder order = repairOrderService.findOrder(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ServiceMessages.REPAIR_ORDER_NOT_FOUND, id)));
        return orderMapper.toResponse(order);
    }

    @PostMapping(ApiPaths.ORDER_WORKS)
    public ResponseEntity<OrderResponse> addWork(
            @PathVariable UUID id,
            @Valid @RequestBody AddWorkRequest request) {
        ServiceWork work = orderMapper.toDomain(request);
        RepairOrder order = repairOrderService.addWork(id, work);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(order));
    }

    @PostMapping(ApiPaths.ORDER_WORKS_BATCH)
    public ResponseEntity<OrderResponse> addWorks(
            @PathVariable UUID id,
            @Valid @RequestBody AddWorksRequest request) {
        List<ServiceWork> works = request.works().stream()
                .map(orderMapper::toDomain)
                .toList();
        RepairOrder order = repairOrderService.addWorks(id, works);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toResponse(order));
    }

    @PostMapping(ApiPaths.COMPLETE_ORDER_WORK)
    public OrderResponse completeWork(@PathVariable UUID id, @PathVariable int index) {
        RepairOrder order = repairOrderService.completeWork(id, index);
        return orderMapper.toResponse(order);
    }

    @PutMapping(ApiPaths.ASSIGN_ORDER_MECHANIC)
    public OrderResponse assignMechanic(
            @PathVariable UUID id,
            @Valid @RequestBody AssignMechanicRequest request) {
        RepairOrder order = repairOrderService.assignMechanic(id, request.mechanicId());
        return orderMapper.toResponse(order);
    }

    @PutMapping(ApiPaths.CHANGE_ORDER_PRIORITY)
    public OrderResponse changePriority(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePriorityRequest request) {
        RepairOrder order = repairOrderService.changePriority(id, request.priority());
        return orderMapper.toResponse(order);
    }

    @PostMapping(ApiPaths.DIAGNOSE_ORDER)
    public OrderResponse diagnose(@PathVariable UUID id) {
        return orderMapper.toResponse(repairOrderService.diagnose(id));
    }

    @PostMapping(ApiPaths.APPROVE_ORDER)
    public OrderResponse approve(@PathVariable UUID id) {
        return orderMapper.toResponse(repairOrderService.approve(id));
    }

    @PostMapping(ApiPaths.START_ORDER)
    public OrderResponse startProgress(@PathVariable UUID id) {
        return orderMapper.toResponse(repairOrderService.startProgress(id));
    }

    @PostMapping(ApiPaths.COMPLETE_ORDER)
    public OrderResponse complete(@PathVariable UUID id) {
        return orderMapper.toResponse(repairOrderService.complete(id));
    }

    @PostMapping(ApiPaths.CANCEL_ORDER)
    public OrderResponse cancel(@PathVariable UUID id) {
        return orderMapper.toResponse(repairOrderService.cancel(id));
    }

    @GetMapping(ApiPaths.ORDER_PRICE)
    public PriceResponse getPrice(@PathVariable UUID id) {
        return priceMapper.toResponse(
                repairOrderService.calculateBasePrice(id),
                repairOrderService.calculateFinalPrice(id));
    }

    @GetMapping(ApiPaths.ORDER_PRESENT)
    public PresentResponse getPresent(@PathVariable UUID id) {
        Present present = repairOrderService.createPresent(id);
        return presentMapper.toResponse(present);
    }

    static Pageable normalizePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new InvalidArgumentException(ApiMessages.TYPE_MISMATCH_DETAIL);
        }
        int requestedSize = pageable.getPageSize();
        if (requestedSize < 1) {
            throw new InvalidArgumentException(ApiMessages.TYPE_MISMATCH_DETAIL);
        }
        int size = Math.min(requestedSize, ApiMessages.MAX_PAGE_SIZE);
        return withIdTieBreaker(PageRequest.of(pageable.getPageNumber(), size, pageable.getSort()));
    }

    private static Pageable withIdTieBreaker(Pageable pageable) {
        List<Sort.Order> orders = new ArrayList<>();
        boolean idPresent = false;
        for (Sort.Order order : pageable.getSort()) {
            if (!ApiMessages.ORDER_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new InvalidArgumentException(String.format(
                        ApiMessages.SORT_PROPERTY_NOT_ALLOWED,
                        order.getProperty()));
            }
            if ("id".equals(order.getProperty())) {
                idPresent = true;
            }
            orders.add(order);
        }
        if (!idPresent) {
            orders.add(Sort.Order.asc("id"));
        }
        if (orders.isEmpty()) {
            orders.add(Sort.Order.asc("id"));
        }
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(orders));
    }
}
