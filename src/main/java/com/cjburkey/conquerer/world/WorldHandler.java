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
import com.cjburkey.conquerer.util.Util;
import org.joml.Random;

import static com.cjburkey.conquerer.Conquerer.*;
import static com.cjburkey.conquerer.Log.*;

/**
 * Created by CJ Burkey on 2019/01/16
 */
@SuppressWarnings("WeakerAccess")
public class WorldHandler {
    
    private int seed = 0;
    public final float minTerritoryDistance;
    public final Rectf terrainBounds;
    public final float borderThickness = 0.05f;
    private Rectf worldBounds;
    
    // Handlers
    public final EmpireHandler empireHandler = new EmpireHandler();
    public final BiomeHandler biomeHandler = new BiomeHandler();
    public final Terrain terrain = new Terrain(biomeHandler, new BasicGenerator());
    
    public WorldHandler(float minTerritoryDistance, Rectf terrainBounds) {
        this.minTerritoryDistance = minTerritoryDistance;
        this.terrainBounds = terrainBounds;
    }
    
    public void generateWorld(int seed) {
        this.seed = seed;
        terrain.generator.setMinDistance(minTerritoryDistance).setBounds(terrainBounds);
        terrain.generate(this);
        worldBounds = terrain.bounds().grow(minTerritoryDistance);
        info("Generated territories: {}", terrain.getTerritoryCount());
        
        terrain.getTerritories().values().forEach(this::generateTerritoryEdges);
    }
    
    public void generateWorld(Random random) {
        // Terrain generation stops working for seeds above 2000000 or so.
        // The simplex noise implementation we use is fast, but not perfect with higher inputs (it becomes very grid-like).
        // 2000000 around zero allows for a total possible number of 4000000 worlds, which should be plenty.
        generateWorld(Util.nextInt(random, -2000000, 2000000));
    }
    
    private void generateTerritoryEdges(Territory territory) {
        Mesh.Builder meshBuilder = Mesh.builder();
        
        territory.updateGraphics(meshBuilder);
        
        Mesh mesh = meshBuilder.apply(new Mesh());
        int worldTerritoryEntity = INSTANCE.createObject(ShaderRender.class, MeshRender.class, Pos.class, Rot.class, Scale.class);
        territory.entity = worldTerritoryEntity;
        Entity ent = INSTANCE.world().getEntity(worldTerritoryEntity);
        ent.getComponent(ShaderRender.class).shader = INSTANCE.shaderColored();
        ent.getComponent(MeshRender.class).mesh = mesh;
        ent.getComponent(Pos.class).position.zero();
    }
    
    public int seed() {
        return seed;
    }
    
    public Rectf worldBounds() {
        return worldBounds;
    }
    
}
