package org.ajwerner.voronoi;

/**
 * Created by ajwerner on 12/28/13.
 */
class BreakPoint {

    private final Voronoi v;
    final V2fw s1;
    final V2fw s2;
    private VoronoiEdge e;
    private boolean isEdgeLeft;
    final V2fw edgeBegin;

    private float cacheSweepLoc;
    private V2fw cachePoint;

    BreakPoint(V2fw left, V2fw right, VoronoiEdge e, boolean isEdgeLeft, Voronoi v) {
        this.v = v;
        this.s1 = left;
        this.s2 = right;
        this.e = e;
        this.isEdgeLeft = isEdgeLeft;
        this.edgeBegin = this.getPoint();
    }

    private static float sq(float d) {
        return d * d;
    }

    void finish(V2fw vert) {
        if (isEdgeLeft) {
            e.p1 = vert;
        } else {
            e.p2 = vert;
        }
    }

    void finish() {
        V2fw p = getPoint();
        if (isEdgeLeft) {
            this.e.p1 = p;
        } else {
            this.e.p2 = p;
        }
    }

    public V2fw getPoint() {
        float l = v.getSweepLoc();
        if (l == cacheSweepLoc) {
            return cachePoint;
        }
        cacheSweepLoc = l;

        float x;
        float y;
        // Handle the vertical line case
        if (s1.y() == s2.y()) {
            x = (s1.x() + s2.x()) / 2; // x coordinate is between the two sites
            // comes from parabola focus-directrix definition:
            y = (sq(x - s1.x()) + sq(s1.y()) - sq(l)) / (2 * (s1.y() - l));
        } else {
            // This method works by intersecting the line of the edge with the parabola of the higher point
            // I'm not sure why I chose the higher point, either should work
            float px = (s1.y() > s2.y()) ? s1.x() : s2.x();
            float py = (s1.y() > s2.y()) ? s1.y() : s2.y();
            float m = e.m;
            float b = e.b;

            float d = 2 * (py - l);

            // Straight up quadratic formula
            float A = 1;
            float B = -2 * px - d * m;
            float C = sq(px) + sq(py) - sq(l) - d * b;
            int sign = (s1.y() > s2.y()) ? -1 : 1;
            float det = sq(B) - 4 * A * C;
            // When rounding leads to a very very small negative determinant, fix it
            if (det <= 0) {
                x = -B / (2 * A);
            } else {
                x = (-B + sign * (float) Math.sqrt(det)) / (2 * A);
            }
            y = m * x + b;
        }
        cachePoint = new V2fw(x, y);
        return cachePoint;
    }

    VoronoiEdge getEdge() {
        return this.e;
    }

}
