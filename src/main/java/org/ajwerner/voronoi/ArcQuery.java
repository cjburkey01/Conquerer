package org.ajwerner.voronoi;

/**
 * Created by ajwerner on 12/29/13.
 */
public class ArcQuery extends ArcKey {
    
    private final V2fw p;
    
    ArcQuery(V2fw p) {
        this.p = p;
    }
    
    protected V2fw getLeft() {
        return p;
    }
    
    protected V2fw getRight() {
        return p;
    }
    
}
