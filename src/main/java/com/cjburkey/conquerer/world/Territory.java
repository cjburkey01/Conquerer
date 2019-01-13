package com.cjburkey.conquerer.world;

import com.cjburkey.conquerer.gen.Name;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.ajwerner.voronoi.VoronoiEdge;
import org.joml.Random;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/11
 */
@SuppressWarnings("WeakerAccess")
public final class Territory {
    
    public final UUID uuid = UUID.randomUUID();
    public final String name;
    public final Vector2fc location;
    public final VoronoiEdge[] voronoiEdges;
    public final boolean isWater;
    
    private Territory(String name, Vector2fc location, VoronoiEdge[] voronoiEdges, boolean isWater) {
        this.name = name;
        this.location = location;
        this.voronoiEdges = voronoiEdges;
        this.isWater = isWater;
    }
    
    private Territory(Builder builder) {
        this(builder.name, builder.location, builder.voronoiEdges.toArray(new VoronoiEdge[0]), builder.isWater);
    }
    
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Territory territory = (Territory) o;
        return uuid.equals(territory.uuid);
    }
    
    public int hashCode() {
        return Objects.hash(uuid);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        private String name;
        private Vector2fc location;
        private final Set<VoronoiEdge> voronoiEdges = new HashSet<>();
        private boolean isWater = false;
        
        private Builder() {
        }
        
        public Builder setName(String name) {
            this.name = name;
            return this;
        }
        
        public Builder setRandomName(Random random, int min, int maxInc) {
            return setName(Name.generateName(random, min, maxInc));
        }
        
        public Builder setLocation(Vector2fc location) {
            this.location = location;
            return this;
        }
        
        public Builder clearEdges() {
            this.voronoiEdges.clear();
            return this;
        }
        
        @SuppressWarnings("UnusedReturnValue")
        public Builder addEdge(VoronoiEdge voronoiEdge) {
            this.voronoiEdges.add(voronoiEdge);
            return this;
        }
        
        public Builder addEdges(Collection<VoronoiEdge> voronoiEdges) {
            this.voronoiEdges.addAll(voronoiEdges);
            return this;
        }
        
        @SuppressWarnings("unused")
        public Builder setEdges(Collection<VoronoiEdge> voronoiEdges) {
            return clearEdges().addEdges(voronoiEdges);
        }
        
        public Builder setIsWater(boolean isWater) {
            this.isWater = isWater;
            return this;
        }
        
        public Territory build() {
            return new Territory(this);
        }
        
    }
    
}
