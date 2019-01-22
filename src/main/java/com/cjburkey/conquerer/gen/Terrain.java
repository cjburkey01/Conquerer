package com.cjburkey.conquerer.gen;

import com.cjburkey.conquerer.gen.generator.IGenerator;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.world.BiomeHandler;
import com.cjburkey.conquerer.world.Territory;
import com.cjburkey.conquerer.world.WorldHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class Terrain {
    
    public final IGenerator generator;
    public final BiomeHandler biomeHandler;
    
    private final Object2ObjectOpenHashMap<Vector2fc, Territory> territories = new Object2ObjectOpenHashMap<>();
    private final Vector2f min = new Vector2f();
    private final Vector2f max = new Vector2f();
    private Rectf bounds;
    
    public Terrain(BiomeHandler biomeHandler, IGenerator generator) {
        this.biomeHandler = biomeHandler;
        this.generator = generator;
        reset();
    }
    
    public Terrain reset() {
        territories.values().forEach(Territory::cleanupEntity);
        territories.clear();
        min.set(Float.POSITIVE_INFINITY);
        max.set(Float.NEGATIVE_INFINITY);
        bounds = null;
        return this;
    }
    
    public Terrain generate(WorldHandler worldHandler) {
        // [Re]generate the terrain
        reset();
        this.territories.putAll(generator.generateTerritories(worldHandler));
        
        // Calculate the new bounds
        for (Territory territory : territories.values()) {
            min.x = min(min.x, territory.location.x());
            min.y = min(min.y, territory.location.y());
            max.x = max(max.x, territory.location.x());
            max.y = max(max.y, territory.location.y());
        }
        bounds = new Rectf(min.x, min.y, max.x, max.y);
        return this;
    }
    
    public Map<Vector2fc, Territory> getTerritories() {
        return Collections.unmodifiableMap(territories);
    }
    
    public int getTerritoryCount() {
        return territories.size();
    }
    
    public Rectf bounds() {
        return bounds;
    }
    
}
