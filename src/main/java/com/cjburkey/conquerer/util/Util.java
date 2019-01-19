package com.cjburkey.conquerer.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

import static com.cjburkey.conquerer.Log.*;
import static java.nio.charset.StandardCharsets.*;
import static org.lwjgl.system.MemoryStack.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Util {
    
    public static Optional<String> readResource(String path) {
        path = path.replaceAll(Pattern.quote("\\"), "/").trim();
        while (path.startsWith("/")) path = path.substring(1);
        while (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        path = '/' + path;
        InputStream stream = Util.class.getResourceAsStream(path);
        if (stream == null) {
            exception(new FileNotFoundException("Failed to locate resource: " + path));
            return Optional.empty();
        }
        return readStream(stream);
    }
    
    public static Optional<String> readStream(InputStream stream) {
        if (stream == null) return Optional.empty();
        StringWriter output = new StringWriter();
        try {
            IOUtils.copy(stream, output, UTF_8);
        } catch (Exception e) {
            exception(e);
            return Optional.empty();
        }
        return Optional.of(output.toString().trim());
    }
    
    public static FloatBuffer bufferVec3(MemoryStack stack, Collection<Vector3f> vectors) {
        FloatBuffer buffer = stack.mallocFloat(vectors.size() * 3);
        vectors.forEach(vector -> vector.get(buffer));
        buffer.flip();
        return buffer;
    }
    
    public static FloatBuffer bufferVec3(MemoryStack stack, Vector3f[] vectors) {
        FloatBuffer buffer = stack.mallocFloat(vectors.length * 3);
        for (Vector3f vector : vectors) vector.get(buffer);
        buffer.flip();
        return buffer;
    }
    
    public static FloatBuffer bufferVec2(MemoryStack stack, Collection<Vector2f> vectors) {
        FloatBuffer buffer = stack.mallocFloat(vectors.size() * 2);
        vectors.forEach(vector -> vector.get(buffer));
        buffer.flip();
        return buffer;
    }
    
    public static FloatBuffer bufferVec2(MemoryStack stack, Vector2f[] vectors) {
        FloatBuffer buffer = stack.mallocFloat(vectors.length * 2);
        for (Vector2f vector : vectors) vector.get(buffer);
        buffer.flip();
        return buffer;
    }
    
    public static ShortBuffer bufferShort(MemoryStack stack, Collection<Short> shorts) {
        ShortBuffer buffer = stack.mallocShort(shorts.size());
        shorts.forEach(buffer::put);
        buffer.flip();
        return buffer;
    }
    
    public static ShortBuffer bufferShort(MemoryStack stack, short[] shorts) {
        ShortBuffer buffer = stack.mallocShort(shorts.length);
        for (short s : shorts) buffer.put(s);
        buffer.flip();
        return buffer;
    }
    
    public static FloatBuffer bufferFloat(MemoryStack stack, Collection<Float> floats) {
        FloatBuffer buffer = stack.mallocFloat(floats.size());
        floats.forEach(buffer::put);
        buffer.flip();
        return buffer;
    }
    
    public static FloatBuffer bufferFloat(MemoryStack stack, float[] floats) {
        FloatBuffer buffer = stack.mallocFloat(floats.length);
        for (float f : floats) buffer.put(f);
        buffer.flip();
        return buffer;
    }
    
    public static float lerp(float start, float goal, float progress) {
        return start + (goal - start) * progress;
    }
    
    public static Vector2f lerp(Vector2fc start, Vector2fc goal, float progress) {
        return new Vector2f(lerp(start.x(), goal.x(), progress),
                lerp(start.y(), goal.y(), progress));
    }
    
    public static Vector3f lerp(Vector3fc start, Vector3fc goal, float progress) {
        return new Vector3f(lerp(start.x(), goal.x(), progress),
                lerp(start.y(), goal.y(), progress),
                lerp(start.z(), goal.z(), progress));
    }
    
    public static Vector4f lerp(Vector4fc start, Vector4fc goal, float progress) {
        return new Vector4f(lerp(start.x(), goal.x(), progress),
                lerp(start.y(), goal.y(), progress),
                lerp(start.z(), goal.z(), progress),
                lerp(start.w(), goal.w(), progress));
    }
    
    // Stolen (translated) from Unity C# (shhh)
    public static float dampSpringCrit(float target, float current, float[] velocity, float smoothTime, float deltaTime) {
             smoothTime = Math.max(0.0001f, smoothTime);
             float num = 2.0f / smoothTime;
             float num2 = num * deltaTime;
             float num3 = 1.0f / (1.0f + num2 + 0.48f * num2 * num2 + 0.235f * num2 * num2 * num2);
             float num4 = current - target;
             float num5 = target;
             float num6 = 1000.0f * smoothTime;
             num4 = Math.min(Math.max(num4, -num6), num6);
             target = current - num4;
             float num7 = (velocity[0] + num * num4) * deltaTime;
             velocity[0] = (velocity[0] - num * num7) * num3;
             float num8 = target + (num4 + num7) * num3;
             if ((num5 - current) > 0.0f == (num8 > num5)) {
                 num8 = num5;
                 velocity[0] = (num8 - num5) / deltaTime;
             }
             return num8;
    }
    
    public static float dampSpringCrit(float target, float current, FloatBuffer velocity, float smoothTime, float deltaTime) {
            smoothTime = Math.max(0.0001f, smoothTime);
             float num = 2.0f / smoothTime;
             float num2 = num * deltaTime;
             float num3 = 1.0f / (1.0f + num2 + 0.48f * num2 * num2 + 0.235f * num2 * num2 * num2);
             float num4 = current - target;
             float num5 = target;
             float num6 = 1000.0f * smoothTime;
             num4 = Math.min(Math.max(num4, -num6), num6);
             target = current - num4;
             float num7 = (velocity.get(0) + num * num4) * deltaTime;
             velocity.put(0, (velocity.get(0) - num * num7) * num3);
             float num8 = target + (num4 + num7) * num3;
             if ((num5 - current) > 0.0f == (num8 > num5)) {
                 num8 = num5;
                 velocity.put(0, (num8 - num5) / deltaTime);
             }
             return num8;
    }
    
    public static Vector2f dampSpringCrit(Vector2fc target, Vector2fc current, Vector2f velocity, float smoothTime, float deltaTime) {
        float valueX;
        float valueY;
        try (MemoryStack stack = stackPush()) {
            FloatBuffer xVelocity = stack.mallocFloat(1);
            FloatBuffer yVelocity = stack.mallocFloat(1);
            xVelocity.put(velocity.x);
            yVelocity.put(velocity.y);
            
            valueX = dampSpringCrit(target.x(), current.x(), xVelocity, smoothTime, deltaTime);
            valueY = dampSpringCrit(target.y(), current.y(), yVelocity, smoothTime, deltaTime);
            
            velocity.set(xVelocity.get(0), yVelocity.get(0));
        }
        return new Vector2f(valueX, valueY);
    }
    
    public static Vector3f dampSpringCrit(Vector3fc target, Vector3fc current, Vector3f velocity, float smoothTime, float deltaTime) {
        float valueX;
        float valueY;
        float valueZ;
        try (MemoryStack stack = stackPush()) {
            FloatBuffer xVelocity = stack.mallocFloat(1);
            FloatBuffer yVelocity = stack.mallocFloat(1);
            FloatBuffer zVelocity = stack.mallocFloat(1);
            xVelocity.put(velocity.x);
            yVelocity.put(velocity.y);
            zVelocity.put(velocity.z);
            
            valueX = dampSpringCrit(target.x(), current.x(), xVelocity, smoothTime, deltaTime);
            valueY = dampSpringCrit(target.y(), current.y(), yVelocity, smoothTime, deltaTime);
            valueZ = dampSpringCrit(target.z(), current.z(), zVelocity, smoothTime, deltaTime);
            
            velocity.set(xVelocity.get(0), yVelocity.get(0), zVelocity.get(0));
        }
        return new Vector3f(valueX, valueY, valueZ);
    }
    
    public static int nextInt(Random random, int min, int maxInc) {
        return random.nextInt(maxInc - min + 1) + min;
    }
    
    // -- MORE PURE UTILS -- //
    
    public static byte min(byte a, byte b) {
        return a > b ? b : a;
    }
    
    public static short min(short a, short b) {
        return a > b ? b : a;
    }
    
    public static int min(int a, int b) {
        return a > b ? b : a;
    }
    
    public static long min(long a, long b) {
        return a > b ? b : a;
    }
    
    public static float min(float a, float b) {
        return a > b ? b : a;
    }
    
    public static double min(double a, double b) {
        return a > b ? b : a;
    }
    
    public static byte max(byte a, byte b) {
        return a < b ? b : a;
    }
    
    public static short max(short a, short b) {
        return a < b ? b : a;
    }
    
    public static int max(int a, int b) {
        return a < b ? b : a;
    }
    
    public static long max(long a, long b) {
        return a < b ? b : a;
    }
    
    public static float max(float a, float b) {
        return a < b ? b : a;
    }
    
    public static double max(double a, double b) {
        return a < b ? b : a;
    }
    
    public static byte clamp(byte val, byte minInc, byte maxInc) {
        return min(max(val, minInc), maxInc);
    }
    
    public static short clamp(short val, short minInc, short maxInc) {
        return min(max(val, minInc), maxInc);
    }
    
    public static int clamp(int val, int minInc, int maxInc) {
        return min(max(val, minInc), maxInc);
    }
    
    public static long clamp(long val, long minInc, long maxInc) {
        return min(max(val, minInc), maxInc);
    }
    
    public static float clamp(float val, float minInc, float maxInc) {
        return min(max(val, minInc), maxInc);
    }
    
    public static double clamp(double val, double minInc, double maxInc) {
        return min(max(val, minInc), maxInc);
    }
    
    public static Vector2f getNormal(Vector2fc convexVertex, Vector2fc convexCenter) {
        return convexVertex.sub(convexCenter, new Vector2f()).normalize();
    }
    
    public static Vector2f retractVert(Vector2fc convexVertex, Vector2fc convexCenter, float amount) {
        return getNormal(convexVertex, convexCenter).mul(-amount).add(convexVertex);
    }
    
}
