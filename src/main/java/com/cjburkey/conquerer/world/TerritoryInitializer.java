package com.cjburkey.conquerer.world;

import org.joml.Vector2fc;

import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/19
 */
class TerritoryInitializer {
    
    private static final float tempScale = 20.0f;
    private static final float precipScale = 20.0f;
    
    static void generate(WorldHandler worldHandler, Territory.Builder territoryBuilder) {
        final BiomeHandler biomeHandler = worldHandler.terrain.biomeHandler;
        
        final Vector2fc location = territoryBuilder.getLocation();
        
        // Bound X = Temperature
        // Bound Y = Precipitation
        float temp = simplexSample2f(biomeHandler.getBounds().minX, biomeHandler.getBounds().maxX, tempScale, location, worldHandler.seed());
        float precip = simplexSample2f(biomeHandler.getBounds().minY, biomeHandler.getBounds().maxY, precipScale, location, worldHandler.seed());
        
        territoryBuilder.setBiome(biomeHandler.getBiome(temp, precip));
    }
    
}
