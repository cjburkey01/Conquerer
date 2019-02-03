package com.cjburkey.conquerer.world;

import com.cjburkey.conquerer.Conquerer;
import com.cjburkey.conquerer.GameEngine;
import com.cjburkey.conquerer.ecs.component.render.MeshRender;
import com.cjburkey.conquerer.gl.Mesh;
import com.cjburkey.conquerer.math.CounterClockwiseVec2;
import com.cjburkey.conquerer.util.Util;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntConsumer;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static com.cjburkey.conquerer.util.Util.*;
import static com.cjburkey.conquerer.world.TerritoryInitializer.*;

/**
 * Created by CJ Burkey on 2019/01/15
 */
@SuppressWarnings("WeakerAccess")
public final class Territory {

    public static final Vector3fc waterColor = new Vector3f(0.0f, 0.467f, 0.745f);

    public final String name;
    public final Vector2fc location;
    public final Vector2fc center;
    public final Vector2fc[] vertices;
    public final TerritoryEdge[] edges;
    public final boolean isWater;
    public final IntArrayList entities = new IntArrayList();
    private BiomeHandler.Biome biome;
    private EmpireHandler.Empire currentOwner;

    private Territory(String name, Vector2fc location, TerritoryEdge[] edges, BiomeHandler.Biome biome, boolean isWater) {
        this.name = name;
        this.location = location;
        this.edges = edges;
        this.biome = biome;
        this.isWater = isWater;

        final ObjectArrayList<Vector2fc> vertices = new ObjectArrayList<>();
        for (TerritoryEdge edge : edges) {
            if (!vertices.contains(edge.pointA)) vertices.add(edge.pointA);
            if (!vertices.contains(edge.pointB)) vertices.add(edge.pointB);
        }
        vertices.sort(new CounterClockwiseVec2(Util.center(vertices)));
        this.vertices = vertices.toArray(new Vector2fc[0]);

        center = center(vertices);

        GameEngine.onExit(this::cleanupEntity);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void cleanupEntity() {
        entities.forEach((IntConsumer) GameEngine::delete);
        entities.clear();
    }

    public void refreshGraphics() {
        if (entities.size() < 1) return;
        Mesh.Builder meshBuilder = Mesh.builder();
        updateGraphics(meshBuilder);
        meshBuilder.apply(GameEngine.getEntity(entities.getInt(0)).getComponent(MeshRender.class).mesh);
    }

    public void updateGraphics(Mesh.Builder meshBuilder) {
        if (biome != null) {
            meshBuilder.addPolygon(isWater ? waterColor : biome.color, this.vertices);
        }

        if (currentOwner != null) {
            float bthick = Conquerer.SELF.worldHandler.borderThickness;
            Vector2fc[] tmpVerts = Arrays.copyOf(vertices, vertices.length);
            for (int i = 0; i < tmpVerts.length; i++) {
                tmpVerts[i] = moveVert(tmpVerts[i],
                        tmpVerts[(i - 1 + tmpVerts.length) % tmpVerts.length],
                        tmpVerts[(i + 1 + tmpVerts.length) % tmpVerts.length],
                        -bthick * 1.5f);
            }
            meshBuilder.addLine(currentOwner.color, true, bthick, vertices);
        }
    }

    public EmpireHandler.Empire getCurrentOwner() {
        return currentOwner;
    }

    /**
     * @deprecated Use {@link com.cjburkey.conquerer.world.EmpireHandler.Empire#claimTerritory(com.cjburkey.conquerer.world.Territory)
     * Empire.claimTerritory}
     * or {@link com.cjburkey.conquerer.world.EmpireHandler.Empire#unclaimTerritory(com.cjburkey.conquerer.world.Territory)
     * Empire.unclaimTerritory} methods
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated()
    public void setCurrentOwner(EmpireHandler.Empire empire) {
        currentOwner = empire;
        refreshGraphics();
    }

    public BiomeHandler.Biome getBiome() {
        return biome;
    }

    public void setBiome(BiomeHandler.Biome biome) {
        if (biome == null) return;
        this.biome = biome;
        refreshGraphics();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Territory territory = (Territory) o;
        return name.equals(territory.name) && location.equals(territory.location);
    }

    public int hashCode() {
        return Objects.hash(name, location);
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static class Builder {

        private final ObjectArrayList<TerritoryEdge> edges = new ObjectArrayList<>();
        private String name;
        private Vector2fc location;
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

        public Territory build(WorldHandler worldHandler) {
            generateTerritoryBiome(worldHandler, this);
            return new Territory(name, location, edges.toArray(new TerritoryEdge[0]), biome, isWater);
        }

    }

}
