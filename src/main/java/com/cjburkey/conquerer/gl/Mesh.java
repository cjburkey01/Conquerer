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
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;

import static com.cjburkey.conquerer.Log.*;
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
    
    private static void addLineSegment(IAppender<Float> vertices,
                                IAppender<Short> indices,
                                float thickness,
                                Vector2fc pointA,
                                Vector2fc pointB) {
        // Calculate the normalized vector describing A -> B
        Vector2f dir = pointB.sub(pointA, new Vector2f()).normalize();
        
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
        vertices.put(r.x + pointA.x());
        vertices.put(r.y + pointA.y());
        vertices.put(0.0f);
        
        // Top right vertex (Index: 1)
        vertices.put(r.x + pointB.x());
        vertices.put(r.y + pointB.y());
        vertices.put(0.0f);
        
        // Top left vertex (Index: 2)
        vertices.put(-r.x + pointB.x());
        vertices.put(-r.y + pointB.y());
        vertices.put(0.0f);
        
        // Bottom left vertex (Index: 3)
        vertices.put(-r.x + pointA.x());
        vertices.put(-r.y + pointA.y());
        vertices.put(0.0f);
        
        // First triangle (Indices: 0, 1, 2)
        indices.put(startingIndex);
        indices.put((short) (1 + startingIndex));
        indices.put((short) (2 + startingIndex));
        
        // Second triangle (Indices: 0, 2, 3)
        indices.put(startingIndex);
        indices.put((short) (2 + startingIndex));
        indices.put((short) (3 + startingIndex));
    }
    
    private static void addMiterLine(IAppender<Float> vertices,
                                IAppender<Short> indices,
                                boolean loops,
                                float thickness,
                                Vector2fc[] points) {
        if (points.length < 3) {
            return;
        }
        
        short startingIndex = (short) (vertices.getPos() / 3);
        
        // Loop from P1 to P(n-2)
        for (int i = 0; i < points.length; i++) {
            // Get the current point and its neighboring points
            Vector2fc pointA = null;
            if (i > 0 || loops) {
                pointA = points[(i - 1 + points.length) % points.length];
            }
            Vector2fc pointB = points[i];
            Vector2fc pointC = null;
            if (i < points.length - 1 || loops) {
                pointC = points[(i + 1) % points.length];
            }
            
            // Calculate the normalized vector describing A -> B
            Vector2f dir;
            if (pointA != null) {
                dir = pointB.sub(pointA, new Vector2f()).normalize();
            } else if (pointC != null) {
                dir = pointC.sub(pointB, new Vector2f()).normalize();
            } else {
                error("Miter line point has neither previous nor subsequent point");
                return;
            }
            
            // Calculate the line running tangent to this point
            Vector2f tan;
            if (pointA != null && pointC != null) {
                tan = ((pointC.sub(pointB, new Vector2f())).normalize().add((pointB.sub(pointA, new Vector2f())).normalize(), new Vector2f())).normalize();
            } else {
                tan = new Vector2f(dir);
            }
            
            // The normal line is the 90 degree right turn from the A -> B vector
            // (The "SuspiciousNameCombination" warning is IDEA telling me a "y" argument shouldn't
            //      go into an "x" parameter, but it's used for vector rotation here, so it's intended)
            @SuppressWarnings("SuspiciousNameCombination")
            Vector2f normal = new Vector2f(-dir.y, dir.x).normalize();
            
            // The miter line is a leftward 90 degree rotation of the tangent line
            @SuppressWarnings("SuspiciousNameCombination")
            Vector2f miter = new Vector2f(-tan.y, tan.x).normalize();
            float miterLength = thickness / (2.0f * miter.dot(normal));
            miter.mul(miterLength);
            
            // Create the vertices along the miter line for this given point
            vertices.put(pointB.x() + miter.x);
            vertices.put(pointB.y() + miter.y);
            vertices.put(0.0f);
            
            vertices.put(pointB.x() - miter.x);
            vertices.put(pointB.y() - miter.y);
            vertices.put(0.0f);
        }
        
        // Triangulate (fill in the indices)
        short vertsAdded = (short) ((vertices.getPos() / 3) - startingIndex);
        for (short i = 0; i < points.length - (loops ? 0 : 1); i++) {
            int index1 = startingIndex + (((i * 2) + 1 + vertsAdded) % vertsAdded);
            int index2 = startingIndex + (((i * 2) + 2 + vertsAdded) % vertsAdded);
            
            indices.put((short) index1);
            indices.put((short) index2);
            indices.put((short) (startingIndex + (((i * 2) + vertsAdded) % vertsAdded)));
            
            indices.put((short) index1);
            indices.put((short) (startingIndex + (((i * 2) + 3 + vertsAdded) % vertsAdded)));
            indices.put((short) index2);
        }
    }
    
    private static void addQuad(IAppender<Float> vertices,
                                IAppender<Short> indices,
                                Vector2fc ccw0,
                                Vector2fc ccw1,
                                Vector2fc ccw2,
                                Vector2fc ccw3) {
        short startVertex = (short) (vertices.getPos() / 3);
        
        vertices.put(ccw0.x());
        vertices.put(ccw0.y());
        vertices.put(0.0f);
        
        vertices.put(ccw1.x());
        vertices.put(ccw1.y());
        vertices.put(0.0f);
        
        vertices.put(ccw2.x());
        vertices.put(ccw2.y());
        vertices.put(0.0f);
        
        vertices.put(ccw3.x());
        vertices.put(ccw3.y());
        vertices.put(0.0f);
        
        indices.put(startVertex);
        indices.put((short) (startVertex + 1));
        indices.put((short) (startVertex + 2));
        
        indices.put(startVertex);
        indices.put((short) (startVertex + 2));
        indices.put((short) (startVertex + 3));
    }
    
    private static void addEllipse(IAppender<Float> vertices,
                                   IAppender<Short> indices,
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
            
            // Add the triangle
            indices.put(startingIndex);
            indices.put((short) (startingIndex + (i + 1)));
            indices.put((short) (startingIndex + (((i + smoothness - 1) % smoothness) + 1)));
        }
    }
    
    private static void addCircle(IAppender<Float> vertices,
                                  IAppender<Short> indices,
                                  float radius,
                                  int smoothness) {
        addEllipse(vertices, indices, radius * 2.0f, radius * 2.0f, smoothness);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public static final class Builder {
        
        private final FloatArrayList vertices = new FloatArrayList();
        private final ShortArrayList indices = new ShortArrayList();
        private final FloatArrayList colors = new FloatArrayList();
        
        private final IAppender<Float> vertexAppender = new IAppender.FloatCollectionAppender(vertices);
        private final IAppender<Short> indexAppender = new IAppender.ShortCollectionAppender(indices);
        
        private Builder() {
        }
        
        public Builder pushVertex(float x, float y, float z) {
            vertices.push(x);
            vertices.push(y);
            vertices.push(z);
            return this;
        }
        
        public Builder pushVertex(Vector3fc point) {
            return pushVertex(point.x(), point.y(), point.z());
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
            return pushColor(color.x(), color.y(), color.z());
        }
        
        public Builder fillColor(Vector3fc color) {
            for (int i = colors.size(); i < vertices.size(); i += 3) {
                colors.add(color.x());
                colors.add(color.y());
                colors.add(color.z());
            }
            return this;
        }
        
        public Builder addLine(Vector3fc color, boolean loops, float thickness, Vector2fc... points) {
            if (points.length < 2) return this;
            if (points.length == 2) {
                Mesh.addLineSegment(vertexAppender, indexAppender, thickness, points[0], points[1]);
            } else {
                Mesh.addMiterLine(vertexAppender, indexAppender, loops, thickness, points);
            }
            return fillColor(color);
        }
        
        public Builder addRay(Vector3fc color, float thickness, Vector2fc center, Vector2fc direction, float length) {
            Mesh.addLineSegment(vertexAppender, indexAppender, thickness, center, direction.mul(length, new Vector2f()).add(center));
            return fillColor(color);
        }
        
        public Builder addRay(Vector3fc color, float thickness, Vector2fc center, Vector2fc direction) {
            return addRay(color, thickness, center, direction, 1.0f);
        }
        
        public Builder addEllipse(Vector3fc color, float width, float height, int smoothness) {
            Mesh.addEllipse(vertexAppender, indexAppender, width, height, smoothness);
            return fillColor(color);
        }
        
        public Builder addCircle(Vector3fc color, float radius, int smoothness) {
            Mesh.addCircle(vertexAppender, indexAppender, radius, smoothness);
            return fillColor(color);
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
