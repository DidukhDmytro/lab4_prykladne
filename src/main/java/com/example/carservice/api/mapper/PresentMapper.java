package com.example.carservice.api.mapper;

import com.example.carservice.api.dto.PresentResponse;
import com.example.carservice.domain.Present;
import org.springframework.stereotype.Component;

@Component
public class PresentMapper {
    public PresentResponse toResponse(Present present) {
        String giftName = present.getGift()
                .map(gift -> gift.getName())
                .orElse(null);
        return new PresentResponse(present.getPartsTotal(), giftName);
    }
}
