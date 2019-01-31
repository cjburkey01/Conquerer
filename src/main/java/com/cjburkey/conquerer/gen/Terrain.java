package com.cjburkey.conquerer.gen;

import com.cjburkey.conquerer.gen.generator.IGenerator;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.world.BiomeHandler;
import com.cjburkey.conquerer.world.Territory;
import com.cjburkey.conquerer.world.WorldHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import static com.cjburkey.conquerer.gen.Poisson.*;
import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public final class Terrain {
    
    public final IGenerator generator;
    public final BiomeHandler biomeHandler;
    
    private final Object2ObjectOpenHashMap<Vector2ic, Territory> territories = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<Vector2fc, Territory> territoriesLocs = new Object2ObjectOpenHashMap<>();
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
        territoriesLocs.putAll(generator.generateTerritories(worldHandler));
        for (Territory territory : territoriesLocs.values()) territories.put(getCell(territory.location, generator.getMinDistance()), territory);
        
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
    
    public Map<Vector2ic, Territory> getTerritories() {
        return Collections.unmodifiableMap(territories);
    }
    
    public Map<Vector2fc, Territory> getTerritoriesLoc() {
        return Collections.unmodifiableMap(territoriesLocs);
    }
    
    public Territory getContainingTerritory(Vector2fc point) {
        Vector2ic center = getCell(point, generator.getMinDistance());
        int ringSize = 0;
        do {
            List<Territory> inRing = getContainingTerritoryRadius(center, ringSize++);  // Ringsize is incremented afterwards
            if (inRing.size() <= 0) continue;
            
//            // TODO: CHECK POLYGON CONTAINS INSTEAD OF NEARNESS CHECK
//            float minDistSq = Float.POSITIVE_INFINITY;
//            Territory closest = null;
//            for (Territory territory : inRing) {
//                float distSq = territory.location.distanceSquared(point);
//                if (distSq < minDistSq) {
//                    minDistSq = distSq;
//                    closest = territory;
//                }
//            }
//            return closest;
            
            for (Territory territory : inRing) {
                if (containsPoint(point, territory.vertices)) return territory;
            }
        } while(ringSize < 10);
        return null;
    }
    
    private List<Territory> getContainingTerritoryRadius(Vector2ic center, int r) {
        ObjectArrayList<Territory> output = new ObjectArrayList<>();
        for (int x = -r; x <= r; x++) {
            Vector2i top = center.add(x, r, new Vector2i());
            if (territories.containsKey(top)) output.add(territories.get(top));
            Vector2i bottom = center.add(x, -r, new Vector2i());
            if (territories.containsKey(bottom)) output.add(territories.get(bottom));
        }
        for (int y = -r + 1; y <= r - 1; y++) {
            Vector2i left = center.add(r, y, new Vector2i());
            if (territories.containsKey(left)) output.add(territories.get(left));
            Vector2i right = center.add(-r, y, new Vector2i());
            if (territories.containsKey(right)) output.add(territories.get(right));
        }
        return output;
    }
    
    public int getTerritoryCount() {
        return territories.size();
    }
    
    public Rectf bounds() {
        return bounds;
    }
    
}
