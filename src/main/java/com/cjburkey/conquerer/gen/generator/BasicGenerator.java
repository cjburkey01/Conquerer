package com.cjburkey.conquerer.gen.generator;

import com.cjburkey.conquerer.gen.Poisson;
import com.cjburkey.conquerer.world.Territory;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.ajwerner.voronoi.Voronoi;
import org.joml.Random;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/11
 */
public class BasicGenerator implements IGenerator {
    
    public Set<Territory> generateTerritories(int maxCount, float minDistance) {
        Random random = new Random(System.nanoTime());
        final Map<Vector2fc, Territory.Builder> territories = new Object2ObjectOpenHashMap<>();
        
        new Voronoi(Poisson.getPoints(random, minDistance, 30, maxCount)).getEdges().forEach(edge -> {
            Territory.Builder builderA = territories.get(edge.getPoint1());
            if (builderA == null) {
                builderA = newTerritory(random, edge.getPoint1());
                territories.put(edge.getPoint1(), builderA);
            }
            builderA.addEdge(edge);
            
            Territory.Builder builderB = territories.get(edge.getPoint2());
            if (builderB == null) {
                builderB = newTerritory(random, edge.getPoint2());
                territories.put(edge.getPoint2(), builderB);
            }
            builderB.addEdge(edge);
        });
        
        return territories.values()
                .stream()
                .map(Territory.Builder::build)
                .collect(Collectors.toSet());
    }
    
    private static Territory.Builder newTerritory(Random random, Vector2fc point) {
        return Territory.builder().setRandomName(random, 3, 7).setLocation(point);
    }
    
}
