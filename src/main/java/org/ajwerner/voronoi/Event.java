package org.ajwerner.voronoi;

/**
 * Created by ajwerner on 12/23/13.
 */
public class Event implements Comparable<Event> {
    
    final V2fw p;
    
    Event(V2fw p) {
        this.p = p;
    }
    
    @Override
    public int compareTo(Event o) {
        return V2fw.minYOrderedCompareTo(this.p, o.p);
    }
    
}
