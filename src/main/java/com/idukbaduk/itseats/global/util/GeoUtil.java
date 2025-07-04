package com.idukbaduk.itseats.global.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class GeoUtil {
    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 경도/위도를 Point 객체로 변환한다.
     * @param lng 경도
     * @param lat 위도
     * @return Point 객체
     */
    public static Point toPoint(double lng, double lat) {
        return geometryFactory.createPoint(new Coordinate(lng, lat));
    }


    /**
     * Point 객체를 문자열로 변환합니다. (예시: "POINT(127.0276 37.4979)")
     * @param point 객체
     * @return String 문자열
     */
    public static String toString(Point point) {
        return "POINT(" + point.getX() + " " + point.getY() + ")";
    }
}
