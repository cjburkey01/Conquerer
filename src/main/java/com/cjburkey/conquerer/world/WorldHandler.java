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
import org.ajwerner.voronoi.VoronoiEdge;
import org.joml.Vector3f;

import static com.cjburkey.conquerer.Conquerer.*;
import static com.cjburkey.conquerer.Log.*;

/**
 * Created by CJ Burkey on 2019/01/16
 */
@SuppressWarnings("WeakerAccess")
public class WorldHandler {
    
    public final Terrain terrain = new Terrain(new BasicGenerator());
    
    public void generateWorld() {
        terrain.generate(100, 1.05f);
        info("Generated territories: {}", terrain.getTerritoryCount());
        
        terrain.getTerritories().forEach(this::generateTerritoryEdges);
    }
    
    private void generateTerritoryEdges(Territory territory) {
        Mesh mesh;
        int testObj;
        Entity ent;
        
        for (VoronoiEdge edge : territory.voronoiEdges) {
            mesh = new Mesh().line(0.05f, edge.getPointA(), edge.getPointB());
            testObj = INSTANCE.createObject(ShaderRender.class, MeshRender.class, Pos.class, Rot.class, Scale.class);
            ent = INSTANCE.world().getEntity(testObj);
            ent.getComponent(ShaderRender.class).shader = INSTANCE.shader();
            ent.getComponent(ShaderRender.class).color = new Vector3f(0.3f, 0.3f, 1.0f);
            ent.getComponent(MeshRender.class).mesh = mesh;
            ent.getComponent(Pos.class).position.set(0.0f, 0.0f, -3.0f);
        }
    }
    
}
