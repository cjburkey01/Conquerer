package com.cjburkey.conquerer.world;

import com.cjburkey.conquerer.gl.Mesh;
import com.cjburkey.conquerer.math.CounterClockwiseVec2;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static com.cjburkey.conquerer.Conquerer.*;
import static com.cjburkey.conquerer.util.Util.*;
import static java.util.Collections.*;

/**
 * Created by CJ Burkey on 2019/01/15
 */
@SuppressWarnings("WeakerAccess")
public class Territory {
    
    public static final Vector3fc edgeColor = new Vector3f(0.5f, 1.0f, 0.5f);
    
    public final String name;
    public final Vector2fc location;
    public final Vector2fc center;
    private final ObjectArrayList<Vector2fc> vertices = new ObjectArrayList<>();
    public final TerritoryEdge[] edges;
    public final boolean isWater;
    
    private Territory(String name, Vector2fc location, TerritoryEdge[] edges, boolean isWater) {
        this.name = name;
        this.location = location;
        this.edges = edges;
        this.isWater = isWater;
        
        for (TerritoryEdge edge : edges) {
            if (!vertices.contains(edge.pointA)) vertices.add(edge.pointA);
            if (!vertices.contains(edge.pointB)) vertices.add(edge.pointB);
        }
        vertices.sort(new CounterClockwiseVec2(location));
        
        center = center(vertices);
    }
    
    public List<Vector2fc> vertices() {
        return unmodifiableList(vertices);
    }
    
    public void updateGraphics(Mesh.Builder meshBuilder) {
        float bthick = INSTANCE.worldHandler.borderThickness;
        for (int i = 0; i < vertices.size(); i ++) {
            vertices.set(i, moveVert(vertices.get(i),
                    vertices.get((i - 1 + vertices.size()) % vertices.size()),
                    vertices.get((i + 1 + vertices.size()) % vertices.size()), 
                    bthick * 1.5f));
        }
        meshBuilder.addLine(edgeColor, true, bthick, vertices.toArray(new Vector2fc[0]));
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder {
        
        private String name;
        private Vector2fc location;
        private final ObjectArrayList<TerritoryEdge> edges = new ObjectArrayList<>();
        private boolean isWater;
        
        private Builder() {
        }
        
        public String getName() {
            return name;
        }
        
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        
        public Vector2fc getLocation() {
            return location;
        }
        
        public Builder setLocation(Vector2fc location) {
            this.location = location;
            return this;
        }
        
        public ObjectArrayList<TerritoryEdge> getEdges() {
            return edges;
        }
        
        public boolean isWater() {
            return isWater;
        }
        
        public Builder setWater(boolean water) {
            isWater = water;
            return this;
        }
        
        public Territory build() {
            TerritoryInitializer.generate(this);
            return new Territory(name, location, edges.toArray(new TerritoryEdge[0]), isWater);
        }
        
    }
    
}
