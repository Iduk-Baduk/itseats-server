package com.idukbaduk.itseats.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.locationtech.jts.geom.Point;

@Getter
@AllArgsConstructor
public class PointDto {
    private double lng;
    private double lat;

    public PointDto(Point point) {
        if (point == null) {
            // Point가 null일 경우 쿠팡 본사 경도/위도 반환
            this.lng = 127.0991124;
            this.lat = 37.5157873;
        }
        this.lng = point.getX();
        this.lat = point.getY();
    }
}
