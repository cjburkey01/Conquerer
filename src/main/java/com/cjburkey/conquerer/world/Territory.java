package com.cjburkey.conquerer.world;

import com.cjburkey.conquerer.ecs.component.render.MeshRender;
import com.cjburkey.conquerer.gl.Mesh;
import com.cjburkey.conquerer.math.ClockwiseVec2;
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
    private BiomeHandler.Biome biome;
    public final boolean isWater;
    public int entity = -1;
    private Empire currentOwner;
    
    private Territory(String name, Vector2fc location, TerritoryEdge[] edges, BiomeHandler.Biome biome, boolean isWater) {
        this.name = name;
        this.location = location;
        this.edges = edges;
        this.biome = biome;
        this.isWater = isWater;
        
        for (TerritoryEdge edge : edges) {
            if (!vertices.contains(edge.pointA)) vertices.add(edge.pointA);
            if (!vertices.contains(edge.pointB)) vertices.add(edge.pointB);
        }
        vertices.sort(new ClockwiseVec2(location));
        
        center = center(vertices);
    }
    
    public List<Vector2fc> vertices() {
        return unmodifiableList(vertices);
    }
    
    public void cleanupEntity() {
        if (entity >= 0) {
            INSTANCE.world().delete(entity);
            entity = -1;
        }
    }
    
    public void refreshGraphics() {
        if (entity < 0) return;
        Mesh.Builder meshBuilder = Mesh.builder();
        updateGraphics(meshBuilder);
        meshBuilder.apply(INSTANCE.world().getEntity(entity).getComponent(MeshRender.class).mesh);
    }
    
    public void updateGraphics(Mesh.Builder meshBuilder) {
        if (currentOwner != null) {
            float bthick = INSTANCE.worldHandler.borderThickness;
            Vector2fc[] vertices = this.vertices.toArray(new Vector2fc[0]);
            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = moveVert(vertices[i],
                        vertices[(i - 1 + vertices.length) % vertices.length],
                        vertices[(i + 1 + vertices.length) % vertices.length],
                        -bthick * 1.5f);
            }
            meshBuilder.addLine(edgeColor, true, bthick, vertices);
        }
        if (biome != null) {
            meshBuilder.addPolygon(biome.color/*.mul(0.5f, new Vector3f())*/, this.vertices.toArray(new Vector2fc[0]));
        }
    }
    
    public void setCurrentOwner(Empire empire) {
        currentOwner = empire;
        refreshGraphics();
    }

    public Empire getCurrentOwner() {
        return currentOwner;
    }

    public void setBiome(BiomeHandler.Biome biome) {
        if (biome == null) return;
        this.biome = biome;
        refreshGraphics();
    }

    public BiomeHandler.Biome getBiome() {
        return biome;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder {
        
        private String name;
        private Vector2fc location;
        private final ObjectArrayList<TerritoryEdge> edges = new ObjectArrayList<>();
        private BiomeHandler.Biome biome;
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
        
        public Builder setWater(boolean isWater) {
            this.isWater = isWater;
            return this;
        }
        
        public BiomeHandler.Biome getBiome() {
            return biome;
        }
        
        public Builder setBiome(BiomeHandler.Biome biome) {
            this.biome = biome;
            return this;
        }
        
        public Territory build() {
            TerritoryInitializer.generate(INSTANCE.worldHandler, this);
            return new Territory(name, location, edges.toArray(new TerritoryEdge[0]), biome, isWater);
        }
        
    }
    
}
