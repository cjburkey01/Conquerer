package com.cjburkey.conquerer.math;

import com.cjburkey.conquerer.util.Util;
import java.util.Formatter;
import java.util.Objects;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import static java.lang.Float.*;
import static org.joml.Math.*;

/**
 * Created by CJ Burkey on 2019/01/18
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Rectf {
    
    public final float minX;
    public final float minY;
    public final float maxX;
    public final float maxY;
    
    // Calculated values
    public final float width;
    public final float height;
    public final float centerX;
    public final float centerY;
    
    public Rectf(float x1, float y1, float x2, float y2) {
        this.minX = Util.min(x1, x2);
        this.minY = Util.min(y1, y2);
        this.maxX = Util.max(x1, x2);
        this.maxY = Util.max(y1, y2);
        
        this.width = abs(maxX - minX);
        this.height = abs(maxY - minY);
        
        this.centerX = (minX + maxX) / 2.0f;
        this.centerY = (minY + maxY) / 2.0f;
    }
    
    public Vector2f min() {
        return new Vector2f(minX, minY);
    }
    
    public Vector2f max() {
        return new Vector2f(maxX, maxY);
    }
    
    public Vector2f size() {
        return new Vector2f(width, height);
    }
    
    public Vector2f center() {
        return new Vector2f(centerX, centerY);
    }
    
    public Rectf grow(float left, float down, float right, float up) {
        return new Rectf(minX - left, minY - down, maxX + right, maxY + up);
    }
    
    public Rectf grow(float x, float y) {
        return grow(x, y, x, y);
    }
    
    public Rectf grow(float amount) {
        return grow(amount, amount);
    }
    
    public Vector2f getRandomPoint(Random random) {
        float x = random.nextFloat() * width - minX;
        float y = random.nextFloat() * height - minY;
        return new Vector2f(x, y);
    }
    
    public boolean contains(float x, float y, boolean incMax) {
        return x >= minX
                && y >= minY
                && (incMax && x <= maxX || !incMax && x < maxX)
                && (incMax && y <= maxY || !incMax && y < maxY);
    }
    
    public boolean contains(Vector2fc point, boolean incMax) {
        return point.x() >= minX
                && point.y() >= minY
                && (incMax && point.x() <= maxX || !incMax && point.x() < maxX)
                && (incMax && point.y() <= maxY || !incMax && point.y() < maxY);
    }
    
    public boolean contains(float x, float y) {
        return x >= minX
                && y >= minY
                && x <= maxX
                && y <= maxY;
    }
    
    public boolean contains(Vector2fc point) {
        return point.x() >= minX
                && point.y() >= minY
                && point.x() <= maxX
                && point.y() <= maxY;
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Rectf rectf = (Rectf) o;
        return Float.compare(rectf.minX, minX) == 0 &&
                Float.compare(rectf.minY, minY) == 0 &&
                Float.compare(rectf.maxX, maxX) == 0 &&
                Float.compare(rectf.maxY, maxY) == 0;
    }
    
    public int hashCode() {
        return Objects.hash(minX, minY, maxX, maxY);
    }
    
    // Cache these strings for a TINY amount of efficiency and abstraction :)
    private static final String str1 = "Rect (";
    private static final String str2 = ") to (";
    private static final String strf = "%.2f";
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(str1);
        new Formatter(out).format(strf, minX);
        out.append(',');
        out.append(' ');
        new Formatter(out).format(strf, minY);
        out.append(str2);
        new Formatter(out).format(strf, maxX);
        out.append(',');
        out.append(' ');
        new Formatter(out).format(strf, maxY);
        return out.append(')').toString();
    }
    
    public static Rectf fromCenter(float centerX, float centerY, float width, float height) {
        float w2 = width / 2.0f;
        float h2 = height / 2.0f;
        return new Rectf(centerX - w2, centerY - w2, centerX + w2, centerY + w2);
    }
    
    public static Rectf infinite() {
        return new Rectf(NEGATIVE_INFINITY, NEGATIVE_INFINITY, POSITIVE_INFINITY, POSITIVE_INFINITY);
    }
    
}
