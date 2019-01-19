package com.cjburkey.conquerer.gen;

import com.cjburkey.conquerer.math.Rectf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Objects;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import static com.cjburkey.conquerer.Log.*;
import static java.lang.Integer.*;
import static org.joml.Math.*;

/**
 * Created by CJ Burkey on 2019/01/10
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class Poisson {
    
    private static final float SQRT_2f = (float) sqrt(2.0d);
    private static final float PI2f = (float) PI * 2.0f;
    
    public static List<Vector2fc> disc(final Random random, final float minDist, final int maxIterations, final int stopAfter, final Rectf bounds) {
        final Object2ObjectOpenHashMap<Vector2ic, Vector2fc> finalPoints = new Object2ObjectOpenHashMap<>();
        final ObjectArrayList<Vector2fc> activePoints = new ObjectArrayList<>();
        
        final Vector2fc initialPoint = getRandomPoint(random, new Vector2f(0.0f, 0.0f), minDist);
        finalPoints.put(getCell(initialPoint, minDist), initialPoint);
        activePoints.add(initialPoint);
        
        while (!activePoints.isEmpty() && finalPoints.size() < stopAfter) {
            final Vector2fc currentPoint = activePoints.get(random.nextInt(activePoints.size()));
            boolean foundPoint = false;
            
            for (int iteration = 0; iteration < maxIterations && !foundPoint; iteration++) {
                final Vector2fc newPoint = getRandomPoint(random, currentPoint, minDist);
                if (!bounds.contains(newPoint)) {
                    continue;   // Make sure the new point is within the bounds
                }
                
                final Vector2ic newPointGridPos = getCell(newPoint, minDist);
                boolean duplicateCell = finalPoints.containsKey(newPointGridPos);
                
                // Check neighbor grid cells to ensure no other points are too close
                for (int x = -2; x <= 2 && !duplicateCell; x++) {
                    for (int y = -2; y <= 2 && !duplicateCell; y++) {
                        if (x == 0 && y == 0) continue;
                        final Vector2ic testCell = newPointGridPos.add(x, y, new Vector2i());
                        
                        if (finalPoints.containsKey(testCell)) {
                            final Vector2fc pointAt = Objects.requireNonNull(finalPoints.get(testCell));
                            if (pointAt.distanceSquared(newPoint) < minDist * minDist) {
                                // The new cell is too close to another cell
                                duplicateCell = true;
                            }
                        }
                    }
                }
                
                if (!duplicateCell) {
                    finalPoints.put(newPointGridPos, newPoint);
                    activePoints.add(newPoint);
                    foundPoint = true;
                }
            }
            
            if (!foundPoint) {
                activePoints.remove(currentPoint);
            }
        }
        
        return new ObjectArrayList<>(finalPoints.values());
    }
    
    public static List<Vector2fc> disc(final Random random, final float minDist, final int maxIterations, final int stopAfter) {
        return disc(random, minDist, maxIterations, stopAfter, Rectf.infinite());
    }
    
    public static List<Vector2fc> disc(final Random random, final float minDist, final int maxIterations, Rectf bounds) {
        return disc(random, minDist, maxIterations, MAX_VALUE, bounds);
    }
    
    private static Vector2fc getRandomPoint(Random random, Vector2fc center, float minDist) {
        float dist = random.nextFloat() * minDist + minDist;
        float angle = random.nextFloat() * PI2f;
        return new Vector2f(center.x() + dist * (float) cos(angle), center.y() + dist * (float) sin(angle));
    }
    
    private static Vector2ic getCell(Vector2fc point, float minDist) {
        float cellSize = minDist / SQRT_2f;
        return new Vector2i((int) floor(point.x() / cellSize), (int) floor(point.y() / cellSize));
    }
    
}
