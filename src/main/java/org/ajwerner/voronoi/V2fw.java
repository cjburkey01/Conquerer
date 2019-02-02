package org.ajwerner.voronoi;

import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/12.
 * Serves as a wrapper for a (readonly) Vector2f that allows comparison within the Voronoi diagram algorithm
 */
public class V2fw implements Comparable<V2fw> {

    final Vector2fc vector;

    V2fw(float x, float y) {
        vector = new Vector2f(x, y);
    }

    V2fw(Vector2fc vector) {
        this(vector.x(), vector.y());
    }

    float x() {
        return vector.x();
    }

    float y() {
        return vector.y();
    }

    public int compareTo(V2fw o) {
        if ((x() == o.x()) || (Float.isNaN(x()) && Float.isNaN(o.x()))) {
            if (y() == o.y()) {
                return 0;
            }
            return (y() < o.y()) ? -1 : 1;
        }
        return (x() < o.x()) ? -1 : 1;
    }

    public String toString() {
        return String.format("(%.4f, %.4f)", x(), y());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        V2fw v2fw = (V2fw) o;
        return equals(vector.x(), v2fw.vector.x()) && equals(vector.y(), v2fw.vector.y());
    }

    public int hashCode() {
        return Objects.hash(vector);
    }

    double distanceTo(V2fw that) {
        return vector.distance(that.vector);
    }

    private static boolean equals(double a, double b) {
        return a == b || (Math.abs(a - b) < (0.0000001d * Math.max(Math.abs(a), Math.abs(b))));
    }

    static int ccw(V2fw a, V2fw b, V2fw c) {
        double area2 = (b.x() - a.x()) * (c.y() - a.y()) - (b.y() - a.y()) * (c.x() - a.x());
        if (area2 < 0) {
            return -1;
        } else if (area2 > 0) {
            return 1;
        }
        return 0;
    }

    static int minYOrderedCompareTo(V2fw p1, V2fw p2) {
        if (p1.y() < p2.y()) {
            return 1;
        }
        if (p1.y() > p2.y()) {
            return -1;
        }
        if (p1.x() == p2.x()) {
            return 0;
        }
        return (p1.x() < p2.x()) ? -1 : 1;
    }

    static V2fw midpoint(V2fw p1, V2fw p2) {
        float x = (p1.x() + p2.x()) / 2.0f;
        float y = (p1.y() + p2.y()) / 2.0f;
        return new V2fw(x, y);
    }

}
