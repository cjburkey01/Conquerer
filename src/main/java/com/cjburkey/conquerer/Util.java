package com.cjburkey.conquerer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

import static com.cjburkey.conquerer.Log.*;
import static java.nio.charset.StandardCharsets.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Util {
    
    public static Optional<String> readResource(String name) {
        name = name.replaceAll(Pattern.quote("\\"), "/").trim();
        while (name.startsWith("/")) name = name.substring(1);
        while (name.endsWith("/")) name = name.substring(0, name.length() - 1);
        name = '/' + name;
        InputStream stream = Util.class.getResourceAsStream(name);
        if (stream == null) {
            exception(new FileNotFoundException("Failed to locate resource: " + name));
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
    
}
