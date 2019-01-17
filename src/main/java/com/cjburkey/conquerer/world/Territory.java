package com.cjburkey.conquerer.world;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.ajwerner.voronoi.VoronoiEdge;
import org.joml.Random;
import org.joml.Vector2fc;

import static com.cjburkey.conquerer.gen.Name.*;

/**
 * Created by CJ Burkey on 2019/01/15
 */
@SuppressWarnings("WeakerAccess")
public class Territory {
    
    public String name;
    public Vector2fc location;
    public final ObjectArrayList<VoronoiEdge> voronoiEdges = new ObjectArrayList<>();
    public boolean isWater;
    
    public void randomName(Random random, int minLengthInc, int maxLengthInc) {
        name = generateName(random, minLengthInc, maxLengthInc);
    }
    
}
