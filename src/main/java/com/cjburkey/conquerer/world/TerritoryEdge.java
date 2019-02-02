package com.cjburkey.conquerer.world;

import java.util.Formatter;
import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/18
 */
@SuppressWarnings("WeakerAccess")
public final class TerritoryEdge {

    private static final String str1 = "Edge from (";
    private static final String str2 = ") to (";
    private static final String strf = "%.2f";
    public final Vector2fc pointA;
    public final Vector2fc pointB;
    public final Vector2fc territoryLocA;
    @SuppressWarnings("unused")
    public Territory territoryA;
    public Territory territoryB;
    public Vector2fc territoryLocB;

    public TerritoryEdge(Vector2fc pointA, Vector2fc pointB, Vector2fc territoryLocA, Vector2fc territoryLocB) {
        this.pointA = new Vector2f(pointA);
        this.pointB = new Vector2f(pointB);
        this.territoryLocA = new Vector2f(territoryLocA);
        this.territoryLocB = new Vector2f(territoryLocB);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TerritoryEdge that = (TerritoryEdge) o;
        return pointA.equals(that.pointA) &&
                pointB.equals(that.pointB) &&
                territoryLocA.equals(that.territoryLocA) &&
                territoryLocB.equals(that.territoryLocB);
    }

    public int hashCode() {
        return Objects.hash(pointA, pointB, territoryLocA, territoryLocB);
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(str1);
        new Formatter(out).format(strf, pointA.x());
        out.append(',');
        out.append(' ');
        new Formatter(out).format(strf, pointA.y());
        out.append(str2);
        new Formatter(out).format(strf, pointB.x());
        out.append(',');
        out.append(' ');
        new Formatter(out).format(strf, pointB.y());
        return out.append(')').toString();
    }

}
