package com.cjburkey.conquerer.gen.generator;

import com.cjburkey.conquerer.gen.Poisson;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.world.Territory;
import com.cjburkey.conquerer.world.TerritoryEdge;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import org.ajwerner.voronoi.Voronoi;
import org.joml.Random;
import org.joml.Vector2fc;

import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/11
 */
public class BasicGenerator implements IGenerator {
    
    private Rectf bounds = Rectf.fromCenter(0.0f, 0.0f, 20.0f, 20.0f);
    private float minDistance = 1.0f;
    
    public Set<Territory> generateTerritories() {
        Random random = new Random(System.nanoTime());
        final Map<Vector2fc, Territory> territories = new Object2ObjectOpenHashMap<>();
        
        new Voronoi(Poisson.disc(random, minDistance, 30, bounds)).getEdges().forEach(edge -> {
            // Check the first side of the edge
            Territory territory = territories.getOrDefault(edge.getPoint1(), null);
            if (territory == null) {
                territory = newTerritory(random, edge.getPoint1());
                territories.put(edge.getPoint1(), territory);
            }
            territory.edges.add(new TerritoryEdge(edge));
            
            // Check the second side of the edge
            territory = territories.getOrDefault(edge.getPoint2(), null);
            if (territory == null) {
                territory = newTerritory(random, edge.getPoint2());
                territories.put(edge.getPoint2(), territory);
            }
            territory.edges.add(new TerritoryEdge(edge));
        });
        
        return new ObjectOpenHashSet<>(territories.values());
    }
    
    public BasicGenerator setBounds(Rectf bounds) {
        this.bounds = bounds;
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
        return bounds;
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
    
    private static Territory newTerritory(Random random, Vector2fc point) {
        Territory territory = new Territory();
        territory.randomName(random, 3, 7);
        territory.location = point;
        return territory;
    }
    
}
