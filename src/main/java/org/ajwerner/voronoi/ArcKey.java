package org.ajwerner.voronoi;

/**
 * Created by ajwerner on 12/29/13.
 */
public abstract class ArcKey implements Comparable<ArcKey> {

    protected abstract V2fw getLeft();

    protected abstract V2fw getRight();

    public int compareTo(ArcKey that) {
        V2fw myLeft = getLeft();
        V2fw myRight = getRight();
        V2fw yourLeft = that.getLeft();
        V2fw yourRight = that.getRight();

        // If one arc contains the query then we'll say that they're the same
        if (((that.getClass() == ArcQuery.class) || (this.getClass() == ArcQuery.class)) &&
                ((myLeft.x() <= yourLeft.x() && myRight.x() >= yourRight.x()) ||
                        (yourLeft.x() <= myLeft.x() && yourRight.x() >= myRight.x()))) {
            return 0;
        }

        if (myLeft.x() == yourLeft.x() && myRight.x() == yourRight.x()) {
            return 0;
        }
        if (myLeft.x() >= yourRight.x()) {
            return 1;
        }
        if (myRight.x() <= yourLeft.x()) {
            return -1;
        }

        return V2fw.midpoint(myLeft, myRight).compareTo(V2fw.midpoint(yourLeft, yourRight));
    }

}
