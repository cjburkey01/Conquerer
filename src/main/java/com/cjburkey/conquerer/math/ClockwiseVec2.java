package com.cjburkey.conquerer.math;

import java.util.Comparator;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import static org.joml.Math.*;

/**
 * Created by CJ Burkey on 2019/01/18
 */
@SuppressWarnings("WeakerAccess")
public final class ClockwiseVec2 implements Comparator<Vector2fc> {
    
    private final Vector2fc center;
    
    public ClockwiseVec2(Vector2fc center) {
        this.center = new Vector2f(center);
    }
    
    public int compare(Vector2fc o1, Vector2fc o2) {
        return (int) round(diff(o1, o2));
    }
    
    private double diff(Vector2fc o1, Vector2fc o2) {
        double theta1 = atan2(o1.y() - center.y(), o1.x() - center.x());
        double theta2 = atan2(o2.y() - center.y(), o2.x() - center.x());
        return toDegrees(theta1 - theta2);
    }
    
}
