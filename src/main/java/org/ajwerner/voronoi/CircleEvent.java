package org.ajwerner.voronoi;

/**
 * Created by ajwerner on 12/28/13.
 */
class CircleEvent extends Event {

    final Arc arc;
    final V2fw vert;

    CircleEvent(Arc a, V2fw p, V2fw vert) {
        super(p);
        this.arc = a;
        this.vert = vert;
    }

}
