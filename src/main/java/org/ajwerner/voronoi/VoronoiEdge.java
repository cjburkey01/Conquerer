package org.ajwerner.voronoi;

import java.util.Objects;
import org.joml.Vector2fc;

/**
 * Created by ajwerner on 12/28/13.
 */
public class VoronoiEdge {
    
    private final V2fw site1;
    private final V2fw main;
    final float m;
    final float b;
    private final boolean isVertical;
    V2fw p1, p2;
    
    VoronoiEdge(V2fw main, V2fw site1) {
        this.site1 = site1;
        this.main = main;
        isVertical = site1.y() == main.y();
        if (isVertical) {
            m = b = 0;
        } else {
            m = -1.0f / ((site1.y() - main.y()) / (site1.x() - main.x()));
            V2fw midpoint = V2fw.midpoint(site1, main);
            b = midpoint.y() - m * midpoint.x();
        }
    }
    
    V2fw intersection(VoronoiEdge that) {
        if (this.m == that.m && this.b != that.b && this.isVertical == that.isVertical) {
            return null; // no intersection
        }
        float x;
        float y;
        if (this.isVertical) {
            x = (this.site1.x() + this.main.x()) / 2;
            y = that.m * x + that.b;
        } else if (that.isVertical) {
            x = (that.site1.x() + that.main.x()) / 2;
            y = this.m * x + this.b;
        } else {
            x = (that.b - this.b) / (this.m - that.m);
            y = m * x + b;
        }
        return new V2fw(x, y);
    }
    
    public Vector2fc getPoint1() {
        return main.vector;
    }
    
    public Vector2fc getPoint2() {
        return site1.vector;
    }
    
    public Vector2fc getPointA() {
        return p1.vector;
    }
    
    public Vector2fc getPointB() {
        return p2.vector;
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VoronoiEdge that = (VoronoiEdge) o;
        return isVertical == that.isVertical && site1.equals(that.site1) && main.equals(that.main);
    }
    
    public int hashCode() {
        return Objects.hash(site1, main, isVertical);
    }
    
}
