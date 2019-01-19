package com.cjburkey.conquerer.gen.generator;

import com.cjburkey.conquerer.gen.Name;
import com.cjburkey.conquerer.gen.Poisson;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.world.Territory;
import com.cjburkey.conquerer.world.TerritoryEdge;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import org.ajwerner.voronoi.Voronoi;
import org.ajwerner.voronoi.VoronoiEdge;
import org.joml.Random;
import org.joml.Vector2fc;

import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/11
 */
public class BasicGenerator implements IGenerator {
    
    private Rectf trueBounds = Rectf.fromCenter(0.0f, 0.0f, 20.0f, 20.0f);
    private Rectf genBounds = Rectf.fromCenter(0.0f, 0.0f, 25.0f, 25.0f);
    private float minDistance = 1.0f;
    
    public Set<Territory> generateTerritories() {
        Random random = new Random(System.nanoTime());
        final Object2ObjectOpenHashMap<Vector2fc, Territory.Builder> territories = new Object2ObjectOpenHashMap<>();
        
        new Voronoi(Poisson.disc(random, minDistance, 30, genBounds)).getEdges().forEach(edge -> {
            // Check the first site of the edge
            tryEdge(random, territories, edge.getPoint1(), edge);
            
            // Check the second site of the edge
            tryEdge(random, territories, edge.getPoint2(), edge);
        });

        ObjectOpenHashSet<Territory> territoriesSet = new ObjectOpenHashSet<>();
        
        for (Territory.Builder builder : territories.values()) {
            boolean isDestroyed = false;
            for (TerritoryEdge edge : builder.getEdges()) {
                if (!trueBounds.contains(edge.pointA) || !trueBounds.contains(edge.pointB)) {
                    territories.remove(builder.getLocation());
                    isDestroyed = true;
                }
            }
            if (!isDestroyed) {
                Territory territory = builder.build();
                territoriesSet.add(territory);
            }
        }
        
        return territoriesSet;
    }
    
    private void tryEdge(Random random, Map<Vector2fc, Territory.Builder> territories, Vector2fc site, VoronoiEdge edge) {
        Territory.Builder territoryBuilder = territories.getOrDefault(site, null);
        if (territoryBuilder == null) {
            territoryBuilder = newTerritory(random, site);
            territories.put(site, territoryBuilder);
        }
        territoryBuilder.getEdges().add(new TerritoryEdge(edge));
    }
    
    public BasicGenerator setBounds(Rectf bounds) { 
        float f = 2.5f * minDistance;
        this.trueBounds = bounds;
        this.genBounds = new Rectf(bounds.minX - f, bounds.minY - f, bounds.maxX + f, bounds.maxY + f);
        return this;
    }
    
    public BasicGenerator setMinDistance(float minDistance) {
        this.minDistance = max(minDistance, 0.01f);
        return this;
    }
    
    public BasicGenerator setMaxTerritories(int maxTerritories) {
        return this;
    }
    
    public Rectf getBounds() {
        return trueBounds;
    }
    
    public float getMinDistance() {
        return minDistance;
    }
    
    public int getMaxTerritories() {
        return Integer.MAX_VALUE;
    }
    
    public boolean getUsesBounds() {
        return true;
    }
    
    private static Territory.Builder newTerritory(Random random, Vector2fc point) {
        Territory.Builder builder = Territory.builder();
        builder.setName(Name.generateName(random, 3, 7));
        builder.setLocation(point);
        return builder;
    }
    
}
