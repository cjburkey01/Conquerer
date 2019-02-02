package com.cjburkey.conquerer.math;

import java.util.Objects;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("WeakerAccess")
public class Ray {

    public final Vector3f origin = new Vector3f();
    public final Vector3f vector = new Vector3f();

    public Ray() {
    }

    public Ray(Vector3fc origin, Vector3fc vector) {
        this.origin.set(origin);
        this.vector.set(vector);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ray ray = (Ray) o;
        return origin.equals(ray.origin) && vector.equals(ray.vector);
    }

    public int hashCode() {
        return Objects.hash(origin, vector);
    }

}
