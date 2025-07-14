package com.idukbaduk.itseats.global.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class GeoUtil {
    // 위도 1도 ≒ 111.32km
    private static final double EARTH_RADIUS_METERS = 6371000;
    private static final double DEGREE_PER_METER_LAT = 1 / 111320.0;

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

    public static String createBoundingPolygonWkt(Point point, double radiusMeters) {
        // 위도 범위
        double deltaLat = radiusMeters * DEGREE_PER_METER_LAT;

        // 경도는 위도에 따라 달라짐
        double degreePerMeterLng = 1 / (111320.0 * Math.cos(Math.toRadians(point.getY())));
        double deltaLng = radiusMeters * degreePerMeterLng;

        // 남서 → 남동 → 북동 → 북서 → 남서 순으로 POLYGON 구성
        double minLat = point.getY() - deltaLat;
        double maxLat = point.getY() + deltaLat;
        double minLng = point.getX() - deltaLng;
        double maxLng = point.getX() + deltaLng;

        return String.format(
                "POLYGON((%.6f %.6f, %.6f %.6f, %.6f %.6f, %.6f %.6f, %.6f %.6f))",
                minLng, minLat,
                maxLng, minLat,
                maxLng, maxLat,
                minLng, maxLat,
                minLng, minLat
        );
    }
}
