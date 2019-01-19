package com.cjburkey.conquerer.gl;

import com.cjburkey.conquerer.util.IAppender;
import com.cjburkey.conquerer.util.Util;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Objects;
import jdk.internal.jline.internal.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;

import static org.joml.Math.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings({"WeakerAccess", "SameParameterValue", "unused", "UnusedReturnValue"})
public final class Mesh {
    
    // The currently bound mesh
    private static int currentVao = -1;
    
    // Buffers
    private final int vao;
    private int[] vbo = new int[1];     // Vertices
    private int[] ebo = new int[1];     // Triangles
    private int[] cbo = new int[1];     // Colors
    
    // Lists of buffers and vertex attributes used by this mesh
    private IntOpenHashSet buffers = new IntOpenHashSet();
    private IntOpenHashSet attributes = new IntOpenHashSet();
    
    // The number of VERTICES composing this mesh (the number of indices)
    private int triangleVerts;
    
    public Mesh() {
        vao = glGenVertexArrays();
    }
    
    public Mesh setVertices(@Nullable FloatBuffer vertices) {
        bufferData(GL_ARRAY_BUFFER, vbo, vertices, 3, GL_FLOAT, 0);
        return this;
    }
    
    public Mesh setVertices(final float[] vertices) {
        try (final MemoryStack stack = stackPush()) {
            return setVertices(Util.bufferFloat(stack, vertices));
        }
    }
    
    public Mesh setIndices(@Nullable ShortBuffer indices) {
        triangleVerts = indices == null ? 0 : indices.limit();
        bufferData(GL_ELEMENT_ARRAY_BUFFER, ebo, indices);
        return this;
    }
    
    public Mesh setIndices(final short[] indices) {
        try (final MemoryStack stack = stackPush()) {
            return setIndices(Util.bufferShort(stack, indices));
        }
    }
    
    public Mesh setColors(@Nullable FloatBuffer colors) {
        bufferData(GL_ARRAY_BUFFER, cbo, colors, 3, GL_FLOAT, 1);
        return this;
    }
    
    public Mesh setColors(final float[] indices) {
        try (final MemoryStack stack = stackPush()) {
            return setColors(Util.bufferFloat(stack, indices));
        }
    }
    
    // Methods below here should not need to be changed when new attributes are added to meshes.
    // They use dynamic buffers allocated above when buffering data and automatically keep track
    //      of active attrbute ids
    
    public void destroy() {
        // Delete buffers
        for (int buffer : buffers) glDeleteBuffers(buffer);
        buffers.clear();
        
        // Delete vertex array
        glDeleteVertexArrays(vao);
        
        currentVao = 0;
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public final boolean bind() {
        if (isBound()) {
            return true;
        }
        currentVao = vao;
        if (currentVao > 0) {
            glBindVertexArray(vao);
            return true;
        }
        return false;
    }
    
    public boolean isBound() {
        return vao == currentVao;
    }
    
    public int getTriangleCount() {
        return triangleVerts;
    }
    
    private boolean preBuff(int buffer, int[] pointer, boolean hasData, int attribId) {
        if (!bind()) {
            return false;
        }
        if (!hasData && pointer[0] > 0) {
            glDeleteBuffers(pointer[0]);
            buffers.remove(pointer[0]);
            pointer[0] = 0;
            if (attribId >= 0) {
                attributes.remove(attribId);
            }
            return false;
        } else if (!hasData) {
            return false;
        }
        if (pointer[0] <= 0) {
            pointer[0] = glGenBuffers();
            buffers.add(pointer[0]);
        }
        glBindBuffer(buffer, pointer[0]);
        return true;
    }
    
    private void postBuff(int buffer, int attribSize, int attribType, int attribId) {
        if (attribSize > 0 && attribId >= 0) {
            glVertexAttribPointer(attribId, attribSize, attribType, false, 0, 0L);
            attributes.add(attribId);
        }
        glBindBuffer(buffer, 0);
    }
    
    private void bufferData(int buffer, int[] pointer, @Nullable FloatBuffer data, int attribSize, int attribType, int attribId) {
        if (preBuff(buffer, pointer, data != null, attribId)) {
            glBufferData(buffer, Objects.requireNonNull(data), GL_STATIC_DRAW);
            postBuff(buffer, attribSize, attribType, attribId);
        }
    }
    
    private void bufferData(int buffer, int[] pointer, @Nullable FloatBuffer data) {
        bufferData(buffer, pointer, data, -1, -1, -1);
    }
    
    private void bufferData(int buffer, int[] pointer, ShortBuffer data, int attribSize, int attribType, int attribId) {
        if (preBuff(buffer, pointer, data != null, attribId)) {
            glBufferData(buffer, Objects.requireNonNull(data), GL_STATIC_DRAW);
            postBuff(buffer, attribSize, attribType, attribId);
        }
    }
    
    private void bufferData(int buffer, int[] pointer, ShortBuffer data) {
        bufferData(buffer, pointer, data, -1, -1, -1);
    }
    
    public void render() {
        if (!bind() || vbo[0] <= 0 || ebo[0] <= 0) {
            return;
        }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        for (int attribute : attributes) glEnableVertexAttribArray(attribute);
        glDrawElements(GL_TRIANGLES, triangleVerts, GL_UNSIGNED_SHORT, 0L);
        for (int attribute : attributes) glDisableVertexAttribArray(attribute);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    private static void addLine(IAppender<Float> vertices,
                                IAppender<Short> indices,
                                IAppender<Float> colors,
                                Vector3fc color,
                                float thickness,
                                Vector2fc... points) {
        // Each loop cycle adds a quad segment of the line.
        // p0---p1...p(n-2)---p(n-1)
        for (int i = 1; i < points.length; i++) {
            // Get the previous point and the current point
            Vector2fc a = points[i - 1];
            Vector2fc b = points[i];
            
            // Calculate the normalized vector describing A -> B
            Vector2f dir = b.sub(a, new Vector2f()).normalize();
            
            // Calculate the point to the right half of the thickness away
            // This means when we invert it, the left will also be half the thickness away;
            //      the two put together gives us our <THICKNESS> meter(s) thick line.
            // (The "SuspiciousNameCombination" warning is IDEA telling me a "y" argument shouldn't
            //      go into an "x" parameter, but it's used for vector rotation here, so it's intended)
            @SuppressWarnings("SuspiciousNameCombination")
            Vector2f r = new Vector2f(dir.y, -dir.x).mul(thickness / 2.0f);
            
            // Get the index at which the first vertex of this quad will be.
            // We must divide by 3 because each 3 floating points is one vertex (x, y, and z)
            short startingIndex = (short) (vertices.getPos() / 3);
            
            // Bottom right vertex (Index: 0)
            vertices.put(r.x + a.x());
            vertices.put(r.y + a.y());
            vertices.put(0.0f);
            
            if (color != null) {
                colors.put(color.x());
                colors.put(color.y());
                colors.put(color.z());
            }
            
            // Top right vertex (Index: 1)
            vertices.put(r.x + b.x());
            vertices.put(r.y + b.y());
            vertices.put(0.0f);
            
            if (color != null) {
                colors.put(color.x());
                colors.put(color.y());
                colors.put(color.z());
            }
            
            // Top left vertex (Index: 2)
            vertices.put(-r.x + b.x());
            vertices.put(-r.y + b.y());
            vertices.put(0.0f);
            
            if (color != null) {
                colors.put(color.x());
                colors.put(color.y());
                colors.put(color.z());
            }
            
            // Bottom left vertex (Index: 3)
            vertices.put(-r.x + a.x());
            vertices.put(-r.y + a.y());
            vertices.put(0.0f);
            
            if (color != null) {
                colors.put(color.x());
                colors.put(color.y());
                colors.put(color.z());
            }
            
            // First triangle (Indices: 0, 1, 2)
            indices.put(startingIndex);
            indices.put((short) (1 + startingIndex));
            indices.put((short) (2 + startingIndex));
            
            // Second triangle (Indices: 0, 2, 3)
            indices.put(startingIndex);
            indices.put((short) (2 + startingIndex));
            indices.put((short) (3 + startingIndex));
        }
    }
    
    private static void addEllipse(IAppender<Float> vertices,
                                   IAppender<Short> indices,
                                   IAppender<Float> colors,
                                   Vector3fc color, 
                                   float width, 
                                   float height, 
                                   int smoothness) {
        // Require at least vertices
        smoothness = max(smoothness, 4);
        
        // Get the rotation amount between vertices
        final float frac = 2.0f * (float) PI / smoothness;
        
        // Get the index at which the first vertex of this circle will be.
        // We must divide by 3 because each 3 floating points is one vertex (x, y, and z)
        short startingIndex = (short) (vertices.getPos() / 3);
        
        // Add the center vertex
        vertices.put(0.0f);
        vertices.put(0.0f);
        vertices.put(0.0f);
        
        // Vertices [0, smoothness)
        for (int i = 0; i < smoothness; i++) {
            float angle = frac * i;
            
            // Add new vertex
            vertices.put(width * (float) cos(-angle));
            vertices.put(height * (float) sin(-angle));
            vertices.put(0.0f);
            
            if (color != null) {
                colors.put(color.x());
                colors.put(color.y());
                colors.put(color.z());
            }
            
            // Add the triangle
            indices.put(startingIndex);
            indices.put((short) (startingIndex + (i + 1)));
            indices.put((short) (startingIndex + (((i + smoothness - 1) % smoothness) + 1)));
        }
    }
    
    private static void addCircle(IAppender<Float> vertices,
                                  IAppender<Short> indices,
                                  IAppender<Float> colors,
                                  Vector3fc color,
                                  float radius,
                                  int smoothness) {
        addEllipse(vertices, indices, colors, color, radius * 2.0f, radius * 2.0f, smoothness);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public static final class Builder {
        
        public static final class Colors {
            
            public static Vector3fc RGB(float r, float g, float b) { return new Vector3f(r, g, b); }
            public static Vector3fc RGB(float gray) { return new Vector3f(gray); }
            public static Vector3fc brightness(Vector3fc color, float brightness) { return color.mul(brightness, new Vector3f()); }
            
            public static final Vector3fc BLACK = RGB(0.0f);
            public static final Vector3fc WHITE = RGB(1.0f);
            public static final Vector3fc GRAY = RGB(0.5f);
            
            public static final Vector3fc BLUE = RGB(0.0f, 0.0f, 1.0f);
            public static final Vector3fc GREEN = RGB(0.0f, 1.0f, 0.0f);
            public static final Vector3fc CYAN = RGB(0.0f, 1.0f, 1.0f);
            public static final Vector3fc RED = RGB(1.0f, 0.0f, 0.0f);
            public static final Vector3fc PURPLE = RGB(1.0f, 0.0f, 1.0f);
            public static final Vector3fc YELLOW = RGB(1.0f, 1.0f, 0.0f);
            
            public static final Vector3fc DARK_BLUE = brightness(BLUE, 0.5f);
            public static final Vector3fc DARK_GREEN = brightness(GREEN, 0.5f);
            public static final Vector3fc DARK_CYAN = brightness(CYAN, 0.5f);
            public static final Vector3fc DARK_RED = brightness(RED, 0.5f);
            public static final Vector3fc DARK_PURPLE = brightness(PURPLE, 0.5f);
            public static final Vector3fc DARK_YELLOW = brightness(YELLOW, 0.5f);
            
        }
        
        private final FloatArrayList vertices = new FloatArrayList();
        private final ShortArrayList indices = new ShortArrayList();
        private final FloatArrayList colors = new FloatArrayList();
        
        private final IAppender<Float> vertexAppender = new IAppender.FloatCollectionAppender(vertices);
        private final IAppender<Short> indexAppender = new IAppender.ShortCollectionAppender(indices);
        private final IAppender<Float> colorAppender = new IAppender.FloatCollectionAppender(colors);
        
        private Builder() {
        }
        
        public Builder pushVertex(float x, float y, float z) {
            vertices.push(x);
            vertices.push(y);
            vertices.push(z);
            return this;
        }
        
        public Builder pushVertex(Vector3fc point) {
            pushVertex(point.x(), point.y(), point.z());
            return this;
        }
        
        public Builder pushIndex(short index) {
            indices.push(index);
            return this;
        }
        
        public Builder pushColor(float r, float g, float b) {
            colors.push(r);
            colors.push(g);
            colors.push(b);
            return this;
        }
        
        public Builder pushColor(Vector3fc color) {
            pushColor(color.x(), color.y(), color.z());
            return this;
        }
        
        public Builder addLine(Vector3fc color, float thickness, Vector2fc... points) {
            Mesh.addLine(vertexAppender, indexAppender, colorAppender, color, thickness, points);
            return this;
        }
        
        public Builder addRay(Vector3fc color, float thickness, Vector2fc center, Vector2fc direction, float length) {
            Mesh.addLine(vertexAppender, indexAppender, colorAppender, color, thickness, center, direction.mul(length, new Vector2f()).add(center));
            return this;
        }
        
        public Builder addRay(Vector3fc color, float thickness, Vector2fc center, Vector2fc direction) {
            return addRay(color, thickness, center, direction, 1.0f);
        }
        
        public Builder addEllipse(Vector3fc color, float width, float height, int smoothness) {
            Mesh.addEllipse(vertexAppender, indexAppender, colorAppender, color, width, height, smoothness);
            return this;
        }
        
        public Builder addCircle(Vector3fc color, float radius, int smoothness) {
            Mesh.addCircle(vertexAppender, indexAppender, colorAppender, color, radius, smoothness);
            return this;
        }
        
        public Builder clear() {
            vertices.clear();
            indices.clear();
            colors.clear();
            return this;
        }
        
        public Mesh apply(Mesh mesh) {
            mesh.setVertices(vertices.toArray(new float[0]));
            mesh.setIndices(indices.toArray(new short[0]));
            if (colors.size() > 0) mesh.setColors(colors.toArray(new float[0]));
            return mesh;
        }
        
    }
    
}
