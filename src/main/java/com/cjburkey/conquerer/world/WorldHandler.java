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
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static com.cjburkey.conquerer.Conquerer.*;
import static com.cjburkey.conquerer.Log.*;

/**
 * Created by CJ Burkey on 2019/01/16
 */
@SuppressWarnings("WeakerAccess")
public class WorldHandler {
    
    public final Terrain terrain = new Terrain(new BasicGenerator());
    public final float minTerritoryDistance;
    public final Rectf terrainBounds;
    public final Vector3fc edgeColor = new Vector3f(0.5f, 1.0f, 0.5f);
    private Rectf worldBounds;
    
    public WorldHandler(float minTerritoryDistance, Rectf terrainBounds) {
        this.minTerritoryDistance = minTerritoryDistance;
        this.terrainBounds = terrainBounds;
    }
    
    public void generateWorld() {
        terrain.generator.setMinDistance(minTerritoryDistance).setBounds(terrainBounds);
        terrain.generate();
        worldBounds = terrain.bounds().grow(minTerritoryDistance);
        info("Generated territories: {}", terrain.getTerritoryCount());
        
        terrain.getTerritories().forEach(this::generateTerritoryEdges);
    }
    
    private void generateTerritoryEdges(Territory territory) {
        Mesh.Builder meshBuilder = Mesh.builder();
        
        for (TerritoryEdge edge : territory.edges) {
            meshBuilder.addLine(0.05f, edge.pointA, edge.pointB);
        }
        
        Mesh mesh = meshBuilder.apply(new Mesh());
        int testObj = INSTANCE.createObject(ShaderRender.class, MeshRender.class, Pos.class, Rot.class, Scale.class);
        Entity ent = INSTANCE.world().getEntity(testObj);
        ent.getComponent(ShaderRender.class).shader = INSTANCE.shader();
        ent.getComponent(ShaderRender.class).color = edgeColor;
        ent.getComponent(MeshRender.class).mesh = mesh;
        ent.getComponent(Pos.class).position.zero();
    }
    
    public Rectf worldBounds() {
        return worldBounds;
    }
    
}
