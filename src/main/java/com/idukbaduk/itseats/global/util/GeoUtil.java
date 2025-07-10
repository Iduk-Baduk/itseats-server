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
        Point point = geometryFactory.createPoint(new Coordinate(lng, lat));
        point.setSRID(4326);
        return point;
    }


    /**
     * Point 객체를 문자열로 변환합니다. (예시: "POINT(37.4979 127.0276)")
     * MySQL 측에서 조회시 POINT(위도 경도) 순으로 저장되는 문제가 있어 문자열 변환시 반대로 변환합니다.
     * @param point 객체
     * @return String 문자열
     */
    public static String toString(Point point) {
        return "POINT(" + point.getY() + " " + point.getX() + ")";
    }
}
