package com.cjburkey.conquerer.world;

import com.artemis.Entity;
import com.cjburkey.conquerer.ecs.component.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.render.ShaderRender;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.ecs.component.transform.Rot;
import com.cjburkey.conquerer.ecs.component.transform.Scale;
import com.cjburkey.conquerer.gen.Terrain;
import com.cjburkey.conquerer.gen.generator.BasicGenerator;
import com.cjburkey.conquerer.gl.Mesh;
import com.cjburkey.conquerer.math.Rectf;
import org.joml.Random;

import static com.cjburkey.conquerer.Conquerer.*;
import static com.cjburkey.conquerer.Log.*;

/**
 * Created by CJ Burkey on 2019/01/16
 */
@SuppressWarnings("WeakerAccess")
public class WorldHandler {
    
    public final long seed;
    public final Terrain terrain = new Terrain(new BiomeHandler(), new BasicGenerator());
    public final float minTerritoryDistance;
    public final Rectf terrainBounds;
    public final float borderThickness = 0.05f;
    private Rectf worldBounds;
    
    public WorldHandler(long seed, float minTerritoryDistance, Rectf terrainBounds) {
        this.seed = seed;
        this.minTerritoryDistance = minTerritoryDistance;
        this.terrainBounds = terrainBounds;
    }
    
    public WorldHandler(Random random, float minTerritoryDistance, Rectf terrainBounds) {
        this(random.nextInt(Integer.MAX_VALUE - 1) - (Integer.MAX_VALUE - 1) / 2, minTerritoryDistance, terrainBounds);
    }
    
    public void generateWorld() {
        terrain.generator.setMinDistance(minTerritoryDistance).setBounds(terrainBounds);
        terrain.generate();
        worldBounds = terrain.bounds().grow(minTerritoryDistance);
        info("Generated territories: {}", terrain.getTerritoryCount());
        
        terrain.getTerritories().values().forEach(this::generateTerritoryEdges);
    }
    
    private void generateTerritoryEdges(Territory territory) {
        Mesh.Builder meshBuilder = Mesh.builder();
        
        territory.updateGraphics(meshBuilder);
        
        Mesh mesh = meshBuilder.apply(new Mesh());
        int worldTerritoryEntity = INSTANCE.createObject(ShaderRender.class, MeshRender.class, Pos.class, Rot.class, Scale.class);
        territory.entity = worldTerritoryEntity;
        Entity ent = INSTANCE.world().getEntity(worldTerritoryEntity);
        ent.getComponent(ShaderRender.class).shader = INSTANCE.shader();
        ent.getComponent(MeshRender.class).mesh = mesh;
        ent.getComponent(Pos.class).position.zero();
    }
    
    public Rectf worldBounds() {
        return worldBounds;
    }
    
}
