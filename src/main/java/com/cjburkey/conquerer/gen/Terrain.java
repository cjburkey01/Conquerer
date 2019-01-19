package com.cjburkey.conquerer.gen;

import com.cjburkey.conquerer.gen.generator.IGenerator;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.world.Territory;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.Set;
import org.joml.Vector2f;

import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class Terrain {
    
    public final IGenerator generator;
    
    private final ObjectOpenHashSet<Territory> territories = new ObjectOpenHashSet<>();
    private final Vector2f min = new Vector2f();
    private final Vector2f max = new Vector2f();
    private Rectf bounds;
    
    public Terrain(IGenerator generator) {
        this.generator = generator;
        reset();
    }
    
    public Terrain reset() {
        territories.clear();
        min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        bounds = null;
        return this;
    }
    
    public Terrain generate() {
        // [Re]generate the terrain
        reset();
        this.territories.addAll(generator.generateTerritories());
        
        // Calculate the new bounds
        for (Territory territory : territories) {
            min.x = min(min.x, territory.location.x());
            min.y = min(min.y, territory.location.y());
            max.x = max(max.x, territory.location.x());
            max.y = max(max.y, territory.location.y());
        }
        bounds = new Rectf(min.x, min.y, max.x, max.y);
        return this;
    }
    
    public Set<Territory> getTerritories() {
        return Collections.unmodifiableSet(territories);
    }
    
    public int getTerritoryCount() {
        return territories.size();
    }
    
    public Rectf bounds() {
        return bounds;
    }
    
}
