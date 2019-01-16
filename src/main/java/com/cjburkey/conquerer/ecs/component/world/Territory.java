package com.cjburkey.conquerer.ecs.component.world;

import com.artemis.ArchetypeBuilder;
import com.artemis.Entity;
import com.artemis.PooledComponent;
import com.artemis.World;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.ajwerner.voronoi.VoronoiEdge;

/**
 * Created by CJ Burkey on 2019/01/15
 */
public class Territory extends PooledComponent {
    
    public final ObjectArrayList<VoronoiEdge> voronoiEdges = new ObjectArrayList<>();
    public boolean isWater;
    
    public Territory() {
    }
    
    protected void reset() {
        voronoiEdges.clear();
        isWater = false;
    }
    
    public static Entity create(World world) {
        return world.createEntity(new ArchetypeBuilder()
                    .add(Named.class)
                    .add(Location.class)
                    .add(Territory.class)
                    .build(world));
    }
    
}
