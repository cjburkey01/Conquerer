package com.cjburkey.conquerer.world;

import com.artemis.Entity;
import com.cjburkey.conquerer.Conquerer;
import com.cjburkey.conquerer.GameEngine;
import com.cjburkey.conquerer.ecs.component.Camera;
import com.cjburkey.conquerer.ecs.component.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.render.ShaderRender;
import com.cjburkey.conquerer.ecs.component.render.Textured;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.ecs.component.transform.Rot;
import com.cjburkey.conquerer.ecs.component.transform.Scale;
import com.cjburkey.conquerer.gen.generator.BasicGenerator;
import com.cjburkey.conquerer.gl.FontHelper;
import com.cjburkey.conquerer.gl.Mesh;
import com.cjburkey.conquerer.glfw.Input;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.util.Util;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static com.cjburkey.conquerer.Log.*;
import static com.cjburkey.conquerer.math.Transformation.*;

/**
 * Created by CJ Burkey on 2019/01/16
 */
@SuppressWarnings("WeakerAccess")
public final class WorldHandler {

    public final float minTerritoryDistance;
    public final Rectf terrainBounds;
    public final float borderThickness = 0.05f;
    // Handlers
    public final EmpireHandler empireHandler = new EmpireHandler();
    public final BiomeHandler biomeHandler = new BiomeHandler();
    public final Terrain terrain = new Terrain(biomeHandler, new BasicGenerator());
    // Properties
    private int seed = 0;
    private Rectf worldBounds;

    public WorldHandler(float minTerritoryDistance, Rectf terrainBounds) {
        this.minTerritoryDistance = minTerritoryDistance;
        this.terrainBounds = terrainBounds;
    }

    public void generateWorld(int seed) {
        this.seed = seed;
        terrain.generator
                .setMinDistance(minTerritoryDistance)
                .setBounds(terrainBounds);
        deleteTerrainGraphics();
        for (EmpireHandler.Empire empire : empireHandler.getEmpires()) empire.unclaimAllTerritories();
        terrain.generate(this);
        worldBounds = terrain.bounds().grow(minTerritoryDistance);
        info("Generated territories: {}", terrain.getTerritoryCount());
    }

    public Territory getTerritoryUnderMouse() {
        Entity mainCamera = GameEngine.getMainCamera();
        Vector3f mousePos = cameraToPlane(mainCamera.getComponent(Pos.class).position,
                mainCamera.getComponent(Camera.class),
                Input.mousePos(),
                Conquerer.Q.worldPlane);
        return terrain.getContainingTerritory(new Vector2f(mousePos.x, mousePos.y));
    }

    public void generateWorld(Random random) {
        // Terrain generation stops working for seeds above 2000000 or so.
        // The simplex noise implementation we use is fast, but not perfect with higher inputs (it becomes very grid-like).
        // 2000000 around zero allows for a total possible number of 4000000 worlds, which should be plenty.
        generateWorld(Util.nextInt(random, -2000000, 2000000));
    }

    public void deleteTerrainGraphics() {
        terrain.getTerritories().values().forEach(Territory::cleanupEntity);
    }

    public void generateTerrainGraphics() {
        terrain.getTerritories().values().forEach(this::generateTerritoryEdges);
    }

    private void generateTerritoryEdges(Territory territory) {
        // Generate terrain graphics and borders
        {
            final Mesh.Builder meshBuilder = Mesh.builder();
            territory.updateGraphics(meshBuilder);
            final int worldTerritoryEntity = GameEngine.instantiate(ShaderRender.class, MeshRender.class, Pos.class, Rot.class, Scale.class);
            territory.entities.add(worldTerritoryEntity);
            Entity ent = GameEngine.getEntity(worldTerritoryEntity);
            ent.getComponent(ShaderRender.class).shader = Conquerer.Q.shaderColored();
            ent.getComponent(MeshRender.class).mesh = meshBuilder.apply(new Mesh());
            ent.getComponent(Pos.class).position.zero();
        }

        // Generate territory name mesh
        {
            if (!territory.isWater) {
                FontHelper.FontBitmap font = Conquerer.Q.aleoAscii256();

                final Mesh.Builder meshBuilder = Mesh.builder();
                Vector2f textSize = new Vector2f();
                meshBuilder.addText(font, territory.name, 0.2f, textSize);
                textSize.mul(0.5f);
                final int worldTerritoryEntity = GameEngine.instantiate(ShaderRender.class, MeshRender.class, Pos.class, Rot.class, Scale.class, Textured.class);
                territory.entities.add(worldTerritoryEntity);
                final Entity ent = GameEngine.getEntity(worldTerritoryEntity);
                ent.getComponent(ShaderRender.class).shader = Conquerer.Q.shaderFont();
                ent.getComponent(ShaderRender.class).uniformCallbacks.put("color", s -> s.setUniform("color", new Vector4f(1.0f)));
                Mesh m = new Mesh();
                m.canBeWireframe = false;
                ent.getComponent(MeshRender.class).mesh = meshBuilder.apply(m);
                ent.getComponent(Pos.class).position.set(territory.location.sub(textSize.x, -textSize.y, new Vector2f()), 1.0f);
                ent.getComponent(Textured.class).texture = font.texture;
            }
        }
    }

    public int seed() {
        return seed;
    }

    public Rectf worldBounds() {
        return worldBounds;
    }

}
