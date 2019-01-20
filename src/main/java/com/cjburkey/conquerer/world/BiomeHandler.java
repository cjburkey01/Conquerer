package com.cjburkey.conquerer.world;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/19
 */
public class BiomeHandler {
    
    private final ObjectOpenHashSet<Biome> biomes = new ObjectOpenHashSet<>();
    
    public Biome getBiome(float temperature, float precipitation) {
        Biome closest = null;
        float minDistSq = Float.POSITIVE_INFINITY;
        for (Biome biome : biomes) {
            float distSq = biome.distSq(temperature, precipitation);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closest = biome;
            }
        }
        return closest;
    }
    
    public Biome create(String name, float temperature, float precipitation) {
        Biome biome = new Biome(name, temperature, precipitation);
        biomes.add(biome);
        return biome;
    }
    
    @SuppressWarnings("WeakerAccess")
    public final class Biome {
        
        public final String name;
        private final Vector2fc biome;
        
        private Biome(String name, float temperature, float precipitation) {
            this.name = name;
            biome = new Vector2f(temperature, precipitation);
        }
        
        public float temperature() {
            return biome.x();
        }
        
        public float precipitation() {
            return biome.y();
        }
        
        public float distSq(float temperature, float precipitation) {
            return biome.distanceSquared(temperature, precipitation);
        }
        
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Biome biome1 = (Biome) o;
            return name.equals(biome1.name) &&
                    biome.equals(biome1.biome);
        }
        
        public int hashCode() {
            return Objects.hash(name, biome);
        }
        
    }
    
}
