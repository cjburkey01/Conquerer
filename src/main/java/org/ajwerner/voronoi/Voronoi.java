package org.ajwerner.voronoi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.joml.Vector2fc;

/**
 * Created by ajwerner on 12/23/13.
 */
public class Voronoi {

    private final ArrayList<VoronoiEdge> edgeList;
    // Ghetto but just for drawing stuff
    private float sweepLoc;
    private Set<BreakPoint> breakPoints;
    private TreeMap<ArcKey, CircleEvent> arcs;
    private TreeSet<Event> events;

    public Voronoi(List<Vector2fc> sites) {
        float minPoint = Float.POSITIVE_INFINITY;
        // initialize data structures
        edgeList = new ArrayList<>(sites.size());
        events = new TreeSet<>();
        breakPoints = new HashSet<>();
        arcs = new TreeMap<>();

        for (Vector2fc site : sites) {
            minPoint = Math.min(minPoint, site.y() - 10.0f);
            events.add(new Event(new V2fw(site)));
        }
        sweepLoc = minPoint;
        do {
            Event cur = events.pollFirst();
            sweepLoc = Objects.requireNonNull(cur).p.y();
            if (cur.getClass() == Event.class) {
                handleSiteEvent(cur);
            } else {
                CircleEvent ce = (CircleEvent) cur;
                handleCircleEvent(ce);
            }
        } while ((events.size() > 0));

        for (BreakPoint bp : breakPoints) {
            bp.finish();
        }
    }

    float getSweepLoc() {
        return sweepLoc;
    }

    private void handleSiteEvent(Event cur) {
        // Deal with first point case
        if (arcs.size() == 0) {
            arcs.put(new Arc(cur.p), null);
            return;
        }

        // Find the arc above the site
        Map.Entry<ArcKey, CircleEvent> arcEntryAbove = arcs.floorEntry(new ArcQuery(cur.p));
        Arc arcAbove = (Arc) arcEntryAbove.getKey();

        // Deal with the degenerate case where the first two points are at the same y value
        if (arcs.size() == 0 && arcAbove.site.y() == cur.p.y()) {
            VoronoiEdge newEdge = new VoronoiEdge(arcAbove.site, cur.p);
            newEdge.p1 = new V2fw((cur.p.x() + arcAbove.site.x()) / 2, Float.POSITIVE_INFINITY);
            BreakPoint newBreak = new BreakPoint(arcAbove.site, cur.p, newEdge, false, this);
            breakPoints.add(newBreak);
            this.edgeList.add(newEdge);
            Arc arcLeft = new Arc(null, newBreak);
            Arc arcRight = new Arc(newBreak, null);
            arcs.remove(arcAbove);
            arcs.put(arcLeft, null);
            arcs.put(arcRight, null);
            return;
        }

        // Remove the circle event associated with this arc if there is one
        CircleEvent falseCE = arcEntryAbove.getValue();
        if (falseCE != null) {
            events.remove(falseCE);
        }

        BreakPoint breakL = arcAbove.left;
        BreakPoint breakR = arcAbove.right;
        VoronoiEdge newEdge = new VoronoiEdge(arcAbove.site, cur.p);
        this.edgeList.add(newEdge);
        BreakPoint newBreakL = new BreakPoint(arcAbove.site, cur.p, newEdge, true, this);
        BreakPoint newBreakR = new BreakPoint(cur.p, arcAbove.site, newEdge, false, this);
        breakPoints.add(newBreakL);
        breakPoints.add(newBreakR);

        Arc arcLeft = new Arc(breakL, newBreakL);
        Arc center = new Arc(newBreakL, newBreakR);
        Arc arcRight = new Arc(newBreakR, breakR);

        arcs.remove(arcAbove);
        arcs.put(arcLeft, null);
        arcs.put(center, null);
        arcs.put(arcRight, null);

        checkForCircleEvent(arcLeft);
        checkForCircleEvent(arcRight);
    }

    private void handleCircleEvent(CircleEvent ce) {
        arcs.remove(ce.arc);
        ce.arc.left.finish(ce.vert);
        ce.arc.right.finish(ce.vert);
        breakPoints.remove(ce.arc.left);
        breakPoints.remove(ce.arc.right);

        Entry<ArcKey, CircleEvent> entryRight = arcs.higherEntry(ce.arc);
        Entry<ArcKey, CircleEvent> entryLeft = arcs.lowerEntry(ce.arc);
        Arc arcRight = null;
        Arc arcLeft = null;

        V2fw ceArcLeft = ce.arc.getLeft();
        boolean cocircularJunction = ce.arc.getRight().equals(ceArcLeft);

        if (entryRight != null) {
            arcRight = (Arc) entryRight.getKey();
            while (cocircularJunction && arcRight.getRight().equals(ceArcLeft)) {
                arcs.remove(arcRight);
                arcRight.left.finish(ce.vert);
                arcRight.right.finish(ce.vert);
                breakPoints.remove(arcRight.left);
                breakPoints.remove(arcRight.right);

                CircleEvent falseCe = entryRight.getValue();
                if (falseCe != null) {
                    events.remove(falseCe);
                }

                entryRight = arcs.higherEntry(arcRight);
                arcRight = (Arc) entryRight.getKey();
            }

            CircleEvent falseCe = entryRight.getValue();
            if (falseCe != null) {
                events.remove(falseCe);
                arcs.put(arcRight, null);
            }
        }
        if (entryLeft != null) {
            arcLeft = (Arc) entryLeft.getKey();
            while (cocircularJunction && arcLeft.getLeft().equals(ceArcLeft)) {
                arcs.remove(arcLeft);
                arcLeft.left.finish(ce.vert);
                arcLeft.right.finish(ce.vert);
                breakPoints.remove(arcLeft.left);
                breakPoints.remove(arcLeft.right);

                CircleEvent falseCe = entryLeft.getValue();
                if (falseCe != null) {
                    events.remove(falseCe);
                }

                entryLeft = arcs.lowerEntry(arcLeft);
                arcLeft = (Arc) entryLeft.getKey();
            }

            CircleEvent falseCe = entryLeft.getValue();
            if (falseCe != null) {
                events.remove(falseCe);
                arcs.put(arcLeft, null);
            }
        }

        VoronoiEdge e = new VoronoiEdge(Objects.requireNonNull(arcLeft).right.s1, Objects.requireNonNull(arcRight).left.s2);
        edgeList.add(e);

        // Here we're trying to figure out if the org.ajwerner.voronoi.Voronoi vertex
        // we've found is the left
        // or right point of the new edge.
        // If the edges being traces out by these two arcs take a right turn then we
        // know
        // that the vertex is going to be above the current point
        boolean turnsLeft = (V2fw.ccw(arcLeft.right.edgeBegin, ce.p, arcRight.left.edgeBegin) == 1);
        // So if it turns left, we know the next vertex will be below this vertex
        // so if it's below and the slow is negative then this vertex is the left point
        boolean isLeftPoint = (turnsLeft) ? (e.m < 0) : (e.m > 0);
        if (isLeftPoint) {
            e.p1 = ce.vert;
        } else {
            e.p2 = ce.vert;
        }

        BreakPoint newBP = new BreakPoint(arcLeft.right.s1, arcRight.left.s2, e, !isLeftPoint, this);
        breakPoints.add(newBP);

        arcRight.left = newBP;
        arcLeft.right = newBP;

        checkForCircleEvent(arcLeft);
        checkForCircleEvent(arcRight);
    }

    private void checkForCircleEvent(Arc a) {
        V2fw circleCenter = a.checkCircle();
        if (circleCenter != null) {
            float radius = (float) a.site.distanceTo(circleCenter);
            V2fw circleEventPoint = new V2fw(circleCenter.x(), circleCenter.y() - radius);
            CircleEvent ce = new CircleEvent(a, circleEventPoint, circleCenter);
            arcs.put(a, ce);
            events.add(ce);
        }
    }

    @SuppressWarnings("unused")
    public List<VoronoiEdge> getEdges() {
        return Collections.unmodifiableList(edgeList);
    }

}

