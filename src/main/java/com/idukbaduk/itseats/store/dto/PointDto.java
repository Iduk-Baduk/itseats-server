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
        this.lng = point.getX();
        this.lat = point.getY();
    }
}
