package org.ajwerner.voronoi;

/**
 * Created by ajwerner on 12/28/13.
 */
public class Arc extends ArcKey {
    
    BreakPoint left;
    BreakPoint right;
    final V2fw site;
    
    Arc(BreakPoint left, BreakPoint right) {
        if (left == null && right == null) {
            throw new RuntimeException("cannot make arc with null breakpoints");
        }
        this.left = left;
        this.right = right;
        site = (left != null) ? left.s2 : right.s1;
    }
    
    Arc(V2fw site) {
        left = null;
        right = null;
        this.site = site;
    }
    
    protected V2fw getRight() {
        if (right != null) return right.getPoint();
        return new V2fw(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    }
    
    protected V2fw getLeft() {
        if (left != null) return left.getPoint();
        return new V2fw(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
    }
    
    V2fw checkCircle() {
        if ((left == null) || (right == null)) return null;
        if (V2fw.ccw(left.s1, site, right.s2) != -1) return null;
        return (left.getEdge().intersection(right.getEdge()));
    }
    
}
