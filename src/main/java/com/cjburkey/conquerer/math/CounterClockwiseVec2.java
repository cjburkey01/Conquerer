package com.cjburkey.conquerer.math;

import java.util.Comparator;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/18
 */
public final class CounterClockwiseVec2 implements Comparator<Vector2fc> {

    private final ClockwiseVec2 clockwiseVec2;

    public CounterClockwiseVec2(Vector2fc center) {
        clockwiseVec2 = new ClockwiseVec2(center);
    }

    public int compare(Vector2fc o1, Vector2fc o2) {
        return -clockwiseVec2.compare(o1, o2);      // Negate clockwise
    }

}
