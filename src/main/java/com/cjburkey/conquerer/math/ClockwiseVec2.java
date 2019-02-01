package com.cjburkey.conquerer.math;

import java.util.Comparator;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import static com.cjburkey.conquerer.util.Util.*;
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
        // Return the sign of the result
        // If at all negative, then it must be counter clockwise,
        // If at all positive, then it must be clockwise
        // If 0, then these are the same points.
        return sign(diff(o1, o2));
    }
    
    private double diff(Vector2fc o1, Vector2fc o2) {
        // Convert to polar angle
        double theta1 = atan2(o1.y() - center.y(), o1.x() - center.x());
        double theta2 = atan2(o2.y() - center.y(), o2.x() - center.x());
        
        // Convert to degrees for a larger, more trust worthy number
        return toDegrees(theta1 - theta2);
    }
    
}
