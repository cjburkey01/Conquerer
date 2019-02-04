package com.cjburkey.conquerer.gen.generator;

import com.cjburkey.conquerer.gen.Poisson;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.util.NameBuilder;
import com.cjburkey.conquerer.world.Territory;
import com.cjburkey.conquerer.world.TerritoryEdge;
import com.cjburkey.conquerer.world.WorldHandler;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import org.ajwerner.voronoi.Voronoi;
import org.ajwerner.voronoi.VoronoiEdge;
import org.joml.Random;
import org.joml.Vector2fc;

import static com.cjburkey.conquerer.Log.*;
import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/11
 */
public class BasicGenerator implements IGenerator {

    private Rectf trueBounds = Rectf.fromCenter(0.0f, 0.0f, 20.0f, 20.0f);
    private Rectf genBounds = Rectf.fromCenter(0.0f, 0.0f, 25.0f, 25.0f);
    private float minDistance = 1.0f;

    private static Territory.Builder newTerritory(Random random, Vector2fc point) {
        Territory.Builder builder = Territory.builder();
        builder.setName(new NameBuilder()
            .setCanFirstLetterBeDigraph(false)
            .setShouldCapitalizeFirstCharacter(true)
            .setMinLength(3)
            .setMaxLength(7)
            .build(random));
        builder.setLocation(point);
        return builder;
    }

    public Map<Vector2fc, Territory> generateTerritories(WorldHandler worldHandler) {
        // DEBUG
        debug("Generating (seed: {}) territories within {}", worldHandler.seed(), genBounds);
        debug("Discarding excess territories outside {}", trueBounds);
        long start = System.nanoTime();

        Random random = new Random(System.nanoTime());
        final Object2ObjectOpenHashMap<Vector2fc, Territory.Builder> territories = new Object2ObjectOpenHashMap<>();

        // Generate a poisson disc (random but evenly distributed) of points,
        //      then build edges between them using Fortune's algorithm for a Voronoi graph
        new Voronoi(new ObjectArrayList<>(Poisson.disc(random, minDistance, 30, genBounds).values())).getEdges().forEach(edge -> {
            // Check the first site of the edge
            tryEdge(random, territories, edge.getPoint1(), edge.getPoint2(), edge);

            // Check the second site of the edge
            tryEdge(random, territories, edge.getPoint2(), edge.getPoint1(), edge);
        });

        // The key is the location of the territory to allow for fast searching
        // The location is used when finding neighbors later on
        Object2ObjectOpenHashMap<Vector2fc, Territory> territoriesMap = new Object2ObjectOpenHashMap<>();

        int destroyedTerritories = 0;
        // Build all the territories (except ones that go outside the bounds)
        ObjectOpenHashSet<Territory.Builder> tmpSet = new ObjectOpenHashSet<>(territories.values());
        for (Territory.Builder builder : tmpSet) {
            boolean isDestroyed = false;
            for (TerritoryEdge edge : builder.getEdges()) {
                if (!trueBounds.contains(edge.pointA) || !trueBounds.contains(edge.pointB)) {
                    territories.remove(builder.getLocation());
                    isDestroyed = true;
                    destroyedTerritories++;
                    break;
                }
            }
            if (!isDestroyed) {
                Territory territory = builder.build(worldHandler);
                territoriesMap.put(territory.location, territory);
            }
        }

        // Link all the territories to their neighbors
        for (Territory territory : territoriesMap.values()) {
            for (TerritoryEdge edgeA : territory.edges) {
                if (territoriesMap.containsKey(edgeA.territoryLocB)) {
                    edgeA.territoryB = territoriesMap.get(edgeA.territoryLocB);
                } else {
                    edgeA.territoryLocB = null;
                }
            }
        }

        // DEBUG
        long end = System.nanoTime();
        debug("Discarded {} territories during generation", destroyedTerritories);
        debug("Finished generating a final count of {} territories and took {}ms", territoriesMap.size(), (end - start) / 1000000);

        return territoriesMap;
    }

    private void tryEdge(Random random, Map<Vector2fc, Territory.Builder> territories, Vector2fc site, Vector2fc otherSite, VoronoiEdge edge) {
        Territory.Builder territoryBuilder = territories.getOrDefault(site, null);
        if (territoryBuilder == null) {
            territoryBuilder = newTerritory(random, site);
            territories.put(site, territoryBuilder);
        }
        territoryBuilder.getEdges().add(new TerritoryEdge(edge.getPointA(), edge.getPointB(), site, otherSite));
    }

    public Rectf getBounds() {
        return trueBounds;
    }

    public BasicGenerator setBounds(Rectf bounds) {
        float f = 2.5f * minDistance;
        this.trueBounds = bounds;
        this.genBounds = new Rectf(bounds.minX - f, bounds.minY - f, bounds.maxX + f, bounds.maxY + f);
        return this;
    }

    public float getMinDistance() {
        return minDistance;
    }

    public BasicGenerator setMinDistance(float minDistance) {
        this.minDistance = max(minDistance, 0.01f);
        return this;
    }

    public int getMaxTerritories() {
        return Integer.MAX_VALUE;
    }

    public BasicGenerator setMaxTerritories(int maxTerritories) {
        return this;
    }

    public boolean getUsesBounds() {
        return true;
    }

}
