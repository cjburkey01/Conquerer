package com.cjburkey.conquerer.gen.generator;

import com.artemis.Entity;
import com.cjburkey.conquerer.Conquerer;
import com.cjburkey.conquerer.ecs.component.world.Location;
import com.cjburkey.conquerer.ecs.component.world.Named;
import com.cjburkey.conquerer.ecs.component.world.Territory;
import com.cjburkey.conquerer.gen.Poisson;
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
    
    public Set<Entity> generateTerritories(int maxCount, float minDistance) {
        Random random = new Random(System.nanoTime());
        final Map<Vector2fc, Entity> territories = new Object2ObjectOpenHashMap<>();
        
        new Voronoi(Poisson.getPoints(random, minDistance, 30, maxCount)).getEdges().forEach(edge -> {
            Entity builderA = territories.get(edge.getPoint1());
            if (builderA == null) {
                builderA = newTerritory(random, edge.getPoint1());
                territories.put(edge.getPoint1(), builderA);
            }
            builderA.getComponent(Territory.class).voronoiEdges.add(edge);
            
            Entity builderB = territories.get(edge.getPoint2());
            if (builderB == null) {
                builderB = newTerritory(random, edge.getPoint2());
                territories.put(edge.getPoint2(), builderB);
            }
            builderB.getComponent(Territory.class).voronoiEdges.add(edge);
        });
        
        return new HashSet<>(territories.values());
    }
    
    private static Entity newTerritory(Random random, Vector2fc point) {
        Entity entity = Territory.create(Conquerer.INSTANCE.world());
        entity.getComponent(Named.class).randomName(random, 3, 7);
        entity.getComponent(Location.class).location = point;
        return entity;
    }
    
}
