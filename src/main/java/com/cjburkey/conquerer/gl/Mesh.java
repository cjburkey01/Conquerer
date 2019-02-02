package com.cjburkey.conquerer.gl;

import com.cjburkey.conquerer.Conquerer;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.util.IAppender;
import com.cjburkey.conquerer.util.Util;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Collections;
import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
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
    private int[] uvbo = new int[1];    // Texture coordinates

    // Lists of buffers and vertex attributes used by this mesh
    private IntOpenHashSet buffers = new IntOpenHashSet();
    private IntOpenHashSet attributes = new IntOpenHashSet();

    // The number of VERTICES composing this mesh (the number of indices)
    private int triangleVerts;

    public Mesh() {
        vao = glGenVertexArrays();
        Conquerer.onExit.add(this::destroy);
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
            //      go into an "x" parameter, but it's used for vector rotation here, so it's intended and muy bueno)
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
        smoothness = Util.max(smoothness, 4);

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

    public Mesh setVertices(FloatBuffer vertices) {
        bufferData(GL_ARRAY_BUFFER, vbo, vertices, 3, GL_FLOAT, 0);
        return this;
    }

    public Mesh setVertices(final float[] vertices) {
        try (final MemoryStack stack = stackPush()) {
            return setVertices(Util.bufferFloat(stack, vertices));
        }
    }

    // Methods below here should not need to be changed when new attributes are added to meshes.
    // They use dynamic buffers allocated above when buffering data and automatically keep track
    //      of active attrbute ids

    public Mesh setIndices(ShortBuffer indices) {
        triangleVerts = indices == null ? 0 : indices.limit();
        bufferData(GL_ELEMENT_ARRAY_BUFFER, ebo, indices);
        return this;
    }

    public Mesh setIndices(final short[] indices) {
        try (final MemoryStack stack = stackPush()) {
            return setIndices(Util.bufferShort(stack, indices));
        }
    }

    public Mesh setColors(FloatBuffer colors) {
        bufferData(GL_ARRAY_BUFFER, cbo, colors, 3, GL_FLOAT, 1);
        return this;
    }

    public Mesh setColors(final float[] indices) {
        try (final MemoryStack stack = stackPush()) {
            return setColors(Util.bufferFloat(stack, indices));
        }
    }

    public Mesh setUvs(FloatBuffer uvs) {
        bufferData(GL_ARRAY_BUFFER, cbo, uvs, 2, GL_FLOAT, 2);
        return this;
    }

    public Mesh setUvs(final float[] uvs) {
        try (final MemoryStack stack = stackPush()) {
            return setUvs(Util.bufferFloat(stack, uvs));
        }
    }

    public void destroy() {
        // Delete all the buffers
        for (int buffer : buffers) glDeleteBuffers(buffer);
        buffers.clear();

        // Delete vertex array
        glDeleteVertexArrays(vao);

        // Unbind this mesh if it was bound
        if (currentVao == vao) {
            glBindVertexArray(0);
            currentVao = 0;
        }
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

    private void bufferData(int buffer, int[] pointer, FloatBuffer data, int attribSize, int attribType, int attribId) {
        if (preBuff(buffer, pointer, data != null, attribId)) {
            glBufferData(buffer, Objects.requireNonNull(data), GL_STATIC_DRAW);
            postBuff(buffer, attribSize, attribType, attribId);
        }
    }

    private void bufferData(int buffer, int[] pointer, FloatBuffer data) {
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

    @SuppressWarnings("UnusedReturnValue")
    public static final class Builder {

        private final FloatArrayList vertices = new FloatArrayList();
        private final ShortArrayList indices = new ShortArrayList();
        private final FloatArrayList colors = new FloatArrayList();
        private final FloatArrayList uvs = new FloatArrayList();

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

        public Builder pushUv(float x, float y) {
            uvs.push(x);
            uvs.push(y);
            return this;
        }

        public Builder pushUv(Vector2fc uv) {
            return pushUv(uv.x(), uv.y());
        }

        public Builder addLine(Vector3fc color, boolean loops, float thickness, float z, Vector2fc... points) {
            if (points.length < 2) return this;
            int startVert = vertices.size();
            if (points.length == 2) {
                Mesh.addLineSegment(vertexAppender, indexAppender, thickness, points[0], points[1]);
            } else {
                Mesh.addMiterLine(vertexAppender, indexAppender, loops, thickness, points);
            }
            if (z != 0.0f) {
                for (int vert = startVert; vert < vertices.size(); vert += 3) {
                    vertices.set(vert + 2, z);
                }
            }
            return fillColor(color);
        }

        public Builder addLine(Vector3fc color, boolean loops, float thickness, Vector2fc... points) {
            return addLine(color, loops, thickness, 0.0f, points);
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

        public Builder addPolygon(Vector3fc color, Vector2fc... ccwVertices) {
            Vector2f center = Util.center(ccwVertices);
            short startingIndex = (short) (vertexAppender.getPos() / 3);
            pushVertex(center.x, center.y, 0.0f);
            for (Vector2fc vertex : ccwVertices) {
                pushVertex(vertex.x(), vertex.y(), 0.0f);
            }
            for (int i = 0; i < ccwVertices.length; i++) {
                indexAppender.put(startingIndex);
                indexAppender.put((short) (startingIndex + (i + 1)));
                indexAppender.put((short) (startingIndex + (((i + ccwVertices.length - 1) % ccwVertices.length) + 1)));
            }
            return fillColor(color);
        }

        public Builder addUvQuad(Vector2fc topLeft, Vector2fc bottomRight, Vector2fc uvTopLeft, Vector2fc uvBottomRight) {
            short startingIndex = (short) (vertexAppender.getPos() / 3);

            pushVertex(topLeft.x(), topLeft.y(), 0.0f);
            pushVertex(topLeft.x(), bottomRight.y(), 0.0f);
            pushVertex(bottomRight.x(), bottomRight.y(), 0.0f);
            pushVertex(bottomRight.x(), topLeft.y(), 0.0f);

            pushIndex(startingIndex);
            pushIndex((short) (startingIndex + 1));
            pushIndex((short) (startingIndex + 2));

            pushIndex(startingIndex);
            pushIndex((short) (startingIndex + 2));
            pushIndex((short) (startingIndex + 3));

            pushUv(uvTopLeft.x(), uvTopLeft.y());
            pushUv(uvTopLeft.x(), uvBottomRight.y());
            pushUv(uvBottomRight.x(), uvBottomRight.y());
            pushUv(uvBottomRight.x(), uvTopLeft.y());

            return this;
        }

        public Builder addColorQuad(Vector2fc topLeft, Vector2fc bottomRight, Vector3fc color) {
            short startingIndex = (short) (vertexAppender.getPos() / 3);

            pushVertex(topLeft.x(), topLeft.y(), 0.0f);
            pushVertex(topLeft.x(), bottomRight.y(), 0.0f);
            pushVertex(bottomRight.x(), bottomRight.y(), 0.0f);
            pushVertex(bottomRight.x(), topLeft.y(), 0.0f);

            pushIndex(startingIndex);
            pushIndex((short) (startingIndex + 1));
            pushIndex((short) (startingIndex + 2));

            pushIndex(startingIndex);
            pushIndex((short) (startingIndex + 2));
            pushIndex((short) (startingIndex + 3));

            for (int i = 0; i < 4; i++) pushColor(color);

            return this;
        }

        public Builder addText(FontHelper.FontBitmap fontBitmap, CharSequence text, float size, Vector2f mutSize, boolean flipInds) {
            if (text.length() < 1) return this;

            // Get basic global information about the font
            final float s = size / fontBitmap.lineHeight;
            final char[] characters = new char[text.length()];
            for (int i = 0; i < characters.length; i++) characters[i] = text.charAt(i);
            final FontHelper.Font font = fontBitmap.font;

            // Keep track of x position for the next character
            // Begin offset far enough for the next character not to intersect anything at 0,0
            float x = font.getBoundingBox(characters[0], fontBitmap.lineHeight).minX * s;

            // Loop through all the characters and generate their quads
            for (int i = 0; i < characters.length; i++) {
                // Get the bounds for this character and update the return size
                final Rectf bounds = font.getBoundingBox(characters[i], fontBitmap.lineHeight);
                if (mutSize != null) mutSize.y = max(mutSize.y, bounds.height);

                // Generate the position for the character
                float y = (-font.ascent * font.getScale(fontBitmap.lineHeight) * s) - ((flipInds ? 0.0f : 1.0f) * (bounds.minY * s));
                if (flipInds) {
                    y *= -1.0f;
                    y += bounds.maxY * s;
                }
                final Vector2f tl = new Vector2f(x, y);
                final Vector2f br = tl.add(bounds.width * s, -bounds.height * s, new Vector2f());

                // If necessary, flip the y-coords of the quad to make it visible on inverted-y coordinate systems (such as screen coordinates)
                if (flipInds) {
                    float tmp = br.y;
                    br.y = tl.y;
                    tl.y = tmp;
                }

                // Load the bounds of the UVs
                final Vector4fc uvBounds = fontBitmap.getUv(characters[i]);
                if (uvBounds == null) {
                    error("Failed to load character '{}' from provided font bitmap", characters[i]);
                    continue;
                }

                // Add the quad (TopLeft to BottomRight) with the provided UVs from the font bitmap
                addUvQuad(
                        tl,
                        br,
                        new Vector2f(uvBounds.x(), uvBounds.y()),
                        new Vector2f(uvBounds.z(), uvBounds.w())
                );

                // Increment the position
                float width = font.getCharacterWidth(characters[i], fontBitmap.lineHeight) * s;
                float kern = ((i < (characters.length - 1)) ? font.getCharacterKerning(characters[i], characters[i + 1], fontBitmap.lineHeight) : 0.0f) * s;
                if (mutSize != null) mutSize.x += width + kern;
                x += width + kern;
            }

            // Scale the y size because it's not accounted for
            if (mutSize != null) mutSize.y *= s;

            return this;
        }

        public Builder addText(FontHelper.FontBitmap fontBitmap, CharSequence text, float size, Vector2f mutSize) {
            return addText(fontBitmap, text, size, mutSize, false);
        }

        public Builder translate(float x, float y, float z) {
            for (int i = 0; i < vertexAppender.getPos(); i += 3) {
                vertexAppender.put(i, vertexAppender.at(i) + x);
                vertexAppender.put(i + 1, vertexAppender.at(i + 1) + y);
                vertexAppender.put(i + 2, vertexAppender.at(i + 2) + z);
            }
            return this;
        }

        public Builder flipTris() {
            Collections.reverse(indices);
            return this;
        }

        public Builder translate(Vector3fc offset) {
            return translate(offset.x(), offset.y(), offset.z());
        }

        public Builder clearVertices() {
            vertices.clear();
            return this;
        }

        public Builder clearIndices() {
            indices.clear();
            return this;
        }

        public Builder clearColors() {
            colors.clear();
            return this;
        }

        public Builder clearUvs() {
            uvs.clear();
            return this;
        }

        public Builder clear() {
            clearVertices();
            clearIndices();
            clearColors();
            return clearUvs();
        }

        public Mesh apply(Mesh mesh) {
            mesh.setVertices((vertices.size() > 0) ? vertices.toArray(new float[0]) : null);
            mesh.setIndices((indices.size() > 0) ? indices.toArray(new short[0]) : null);
            if (colors.size() > 0) mesh.setColors(colors.toArray(new float[0]));
            if (uvs.size() > 0) mesh.setUvs(uvs.toArray(new float[0]));
            return mesh;
        }

    }

}
