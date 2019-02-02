package com.cjburkey.conquerer.math;

import java.util.Objects;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("WeakerAccess")
public class Plane {

    public final Vector3f center = new Vector3f();
    public final Vector3f normal = new Vector3f();

    private final Vector3f tmp = new Vector3f();

    public Plane() {
    }

    public Plane(Vector3fc center, Vector3fc normal) {
        this.center.set(center);
        this.normal.set(normal);
    }

    public Vector3f getIntersectionPoint(Ray ray) {
        return getIntersectionPoint(ray.origin, ray.vector);
    }

    public Vector3f getIntersectionPoint(Vector3fc rayOrigin, Vector3fc rayDir) {
        float rDotn = rayDir.dot(normal);

        // If the plane is parallel to the ray, there is no point of intersection
        if (((Float) rDotn).equals(0.0f)) {
            return null;
        }

        float dp = normal.dot(center.sub(rayOrigin, tmp)) / rDotn;
        return rayOrigin.add(rayDir.mul(dp, tmp), new Vector3f());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Plane plane = (Plane) o;
        return center.equals(plane.center) && normal.equals(plane.normal) && Objects.equals(tmp, plane.tmp);
    }

    public int hashCode() {
        return Objects.hash(center, normal, tmp);
    }

}
