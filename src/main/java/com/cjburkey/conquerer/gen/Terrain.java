package com.cjburkey.conquerer.gen;

import com.artemis.Entity;
import com.cjburkey.conquerer.ecs.component.world.Location;
import com.cjburkey.conquerer.gen.generator.IGenerator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.Set;
import org.joml.Vector2f;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class Terrain {
    
    public final IGenerator generator;
    private final ObjectOpenHashSet<Entity> territories = new ObjectOpenHashSet<>();
    private final Vector2f min = new Vector2f();
    private final Vector2f max = new Vector2f();
    private final Vector2f center = new Vector2f();
    private final Vector2f size = new Vector2f();
    
    public Terrain(IGenerator generator) {
        this.generator = generator;
        reset();
    }
    
    public Terrain reset() {
        territories.clear();
        min.set(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        max.set(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        center.zero();
        size.zero();
        return this;
    }
    
    public Terrain generate(int territoryCount, float minDistanceBetweenTerritories) {
        reset();
        this.territories.addAll(generator.generateTerritories(territoryCount, minDistanceBetweenTerritories));
        for (Entity territory : territories) {
            Location loc = territory.getComponent(Location.class);
            min.x = Math.min(min.x, loc.location.x());
            min.y = Math.min(min.y, loc.location.y());
            max.x = Math.max(max.x, loc.location.x());
            max.y = Math.max(max.y, loc.location.y());
        }
        center.set((min.x + max.x) / 2.0f, (min.y + max.y) / 2.0f);
        size.set(max.x - min.x, max.y - min.y);
        return this;
    }
    
    public Set<Entity> getTerritories() {
        return Collections.unmodifiableSet(territories);
    }
    
    public int getTerritoryCount() {
        return territories.size();
    }
    
    public Vector2fc getMin() {
        return min;
    }
    
    public Vector2fc getMax() {
        return max;
    }
    
    public Vector2fc getCenter() {
        return center;
    }
    
    public Vector2fc getSize() {
        return size;
    }
    
}
