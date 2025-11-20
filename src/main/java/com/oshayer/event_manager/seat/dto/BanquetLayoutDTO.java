package com.oshayer.event_manager.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BanquetLayoutDTO {
    @Builder.Default
    private List<BanquetTableDTO> tables = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BanquetTableDTO {
        @Builder.Default
        private UUID id = UUID.randomUUID();
        private String label;
        private String tierCode;
        private Double x;
        private Double y;
        private Double rotation;
        private Double radius;
        private Integer chairCount;
        @Builder.Default
        private List<BanquetChairDTO> chairs = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BanquetChairDTO {
        @Builder.Default
        private UUID id = UUID.randomUUID();
        private String label;
        private Double angle;
        private Double offsetX;
        private Double offsetY;
    }
}
