package com.cjburkey.conquerer.world;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/19
 */
@SuppressWarnings("FieldCanBeLocal")
final class TerritoryInitializer {

    private static final float tempScale = 20.0f;
    private static final float precipScale = 20.0f;
    private static final float altitudeScale = 20.0f;

    private static int precipOffset = 4835;
    private static int altitudeOffset = 2867;

    private static final float oceanLevel = 0.25f;

    static void generateTerritoryBiome(WorldHandler worldHandler, Territory.Builder territoryBuilder) {
        final BiomeHandler biomeHandler = worldHandler.terrain.biomeHandler;

        final Vector2fc location = territoryBuilder.getLocation();

        // Bound X = Temperature
        // Bound Y = Precipitation
        float temp = simplexSample2f(biomeHandler.getBounds().minX,
                biomeHandler.getBounds().maxX,
                tempScale,
                location,
                worldHandler.seed());

        float precip = simplexSample2f(biomeHandler.getBounds().minY,
                biomeHandler.getBounds().maxY,
                precipScale,
                location.add(precipOffset, precipOffset, new Vector2f()),
                worldHandler.seed());

        float altitude = simplexSample2f(0.0f,
                1.0f,
                altitudeScale,
                location.add(altitudeOffset, altitudeOffset, new Vector2f()),
                worldHandler.seed());

        territoryBuilder.setBiome(biomeHandler.getBiome(temp, precip));
        if (altitude <= oceanLevel) territoryBuilder.setWater(true);
    }

}
