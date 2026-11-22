package com.example.carservice.api.mapper;

import com.example.carservice.api.dto.AddWorkRequest;
import com.example.carservice.api.dto.OrderResponse;
import com.example.carservice.api.dto.WorkResponse;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.domain.ServiceWork;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public ServiceWork toDomain(AddWorkRequest request) {
        return new ServiceWork(
                request.description(),
                request.partsCost(),
                request.laborCost(),
                request.requiredSpecialization());
    }

    public WorkResponse toResponse(ServiceWork work, int index) {
        return new WorkResponse(
                index,
                work.getDescription(),
                work.getPartsCost(),
                work.getLaborCost(),
                work.getRequiredSpecialization(),
                work.getStatus());
    }

    public List<WorkResponse> toWorkResponses(List<ServiceWork> works) {
        List<WorkResponse> responses = new ArrayList<>();
        for (int index = 0; index < works.size(); index++) {
            responses.add(toResponse(works.get(index), index));
        }
        return responses;
    }

    public OrderResponse toResponse(RepairOrder order) {
        UUID mechanicId = order.getAssignedMechanic() == null
                ? null
                : order.getAssignedMechanic().getId();
        return new OrderResponse(
                order.getId(),
                order.getCar().getVin(),
                order.getStatus(),
                order.getPriority(),
                mechanicId,
                toWorkResponses(order.getWorks()),
                order.getFinalPrice(),
                order.getCompletedAt());
    }
}
