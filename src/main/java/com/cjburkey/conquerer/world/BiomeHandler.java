package com.cjburkey.conquerer.world;

import com.cjburkey.conquerer.math.Rectf;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/19
 */
@SuppressWarnings({"UnusedReturnValue", "WeakerAccess"})
public final class BiomeHandler {

    private final ObjectOpenHashSet<Biome> biomes = new ObjectOpenHashSet<>();
    private Rectf bounds;

    public BiomeHandler() {
        // Defaults based loosely upon the Whittaker Biome Diagram found at https://w3.marietta.edu/~biol/biomes/biome_main.htm
        create("Tropical Rainforest", 25.0f, 3.35f, 0.082f, 0.941f, 0.227f);
        create("Temperate Rainforest", 12.0f, 2.6f, 0.027f, 0.976f, 0.6735f);
        create("Tropical Seasonal Forest", 23.0f, 1.8f, 0.608f, 0.878f, 0.137f);
        create("Temperate Deciduous Forest", 10.0f, 1.3f, 0.18f, 0.694f, 0.325f);
        create("Taiga", -1.0f, 1.1f, 0.02f, 0.4f, 0.129f);
        create("Tundra", -8.5f, 0.75f, 0.345f, 0.918f, 0.969f);
        create("Savanna", 23.5f, 0.9f, 0.608f, 0.878f, 0.137f);
        create("Temperate Grassland", 12.0f, 0.9f, 0.98f, 0.859f, 0.027f);
        create("Temperate Desert", 10.0f, 0.5f, 0.98f, 0.859f, 0.027f);
        create("Subtropical Desert", 15.0f, 0.25f, 0.98f, 0.58f, 0.094f);
    }

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

    public void calculateBounds() {
        final Vector2f min = new Vector2f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        final Vector2f max = new Vector2f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (Biome biome : biomes) {
            min.x = min(min.x, biome.biome.x());
            min.y = min(min.y, biome.biome.y());
            max.x = max(max.x, biome.biome.x());
            max.y = max(max.y, biome.biome.y());
        }
        bounds = new Rectf(min.x, min.y, max.x, max.y);
    }

    // Temperature in Celsius and anual precipitation in meters
    public Biome create(String name, float temperature, float precipitation, Vector3fc color) {
        Biome biome = new Biome(name, temperature, precipitation, color);
        biomes.add(biome);
        calculateBounds();
        return biome;
    }

    // Temperature in Celsius and anual precipitation in meters
    public Biome create(String name, float temperature, float precipitation, float cr, float cg, float cb) {
        return create(name, temperature, temperature, new Vector3f(cr, cg, cb));
    }

    public Rectf getBounds() {
        return bounds;
    }

    @SuppressWarnings("WeakerAccess")
    public final class Biome {

        public final String name;
        public final Vector3fc color;
        private final Vector2fc biome;

        private Biome(String name, float temperature, float precipitation, Vector3fc color) {
            this.name = name;
            biome = new Vector2f(temperature, precipitation);
            this.color = color;
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
