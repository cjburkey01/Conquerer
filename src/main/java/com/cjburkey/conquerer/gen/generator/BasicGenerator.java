package com.cjburkey.conquerer.gen.generator;

import com.cjburkey.conquerer.gen.Poisson;
import com.cjburkey.conquerer.world.Territory;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.ajwerner.voronoi.Voronoi;
import org.joml.Random;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/11
 */
public class BasicGenerator implements IGenerator {
    
    public Set<Territory> generateTerritories(int maxCount, float minDistance) {
        Random random = new Random(System.nanoTime());
        final Map<Vector2fc, Territory> territories = new Object2ObjectOpenHashMap<>();
        
        new Voronoi(Poisson.getPoints(random, minDistance, 30, maxCount)).getEdges().forEach(edge -> {
            Territory territoryA = territories.get(edge.getPoint1());
            if (territoryA == null) {
                territoryA = newTerritory(random, edge.getPoint1());
                territories.put(edge.getPoint1(), territoryA);
            }
            territoryA.voronoiEdges.add(edge);
            
            Territory territoryB = territories.get(edge.getPoint2());
            if (territoryB == null) {
                territoryB = newTerritory(random, edge.getPoint2());
                territories.put(edge.getPoint2(), territoryB);
            }
            territoryB.voronoiEdges.add(edge);
        });
        
        return new HashSet<>(territories.values());
    }
    
    private static Territory newTerritory(Random random, Vector2fc point) {
        Territory territory = new Territory();
        territory.randomName(random, 3, 7);
        territory.location = point;
        return territory;
    }
    
}
