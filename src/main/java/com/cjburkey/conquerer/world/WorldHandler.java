package com.cjburkey.conquerer.world;

import com.artemis.Entity;
import com.cjburkey.conquerer.Conquerer;
import com.cjburkey.conquerer.ecs.component.engine.Camera;
import com.cjburkey.conquerer.ecs.component.engine.Transform;
import com.cjburkey.conquerer.ecs.component.engine.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.engine.render.ShaderRender;
import com.cjburkey.conquerer.ecs.component.engine.render.Textured;
import com.cjburkey.conquerer.engine.GameEngine;
import com.cjburkey.conquerer.gen.generator.BasicGenerator;
import com.cjburkey.conquerer.gl.FontHelper;
import com.cjburkey.conquerer.gl.Mesh;
import com.cjburkey.conquerer.glfw.Input;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static com.cjburkey.conquerer.gen.Poisson.*;
import static com.cjburkey.conquerer.math.Transformation.*;
import static com.cjburkey.conquerer.util.Log.*;
import static com.cjburkey.conquerer.util.Util.*;

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

    private List<Territory> getContainingTerritoryRadius(Vector2ic center, int r) {
        ObjectArrayList<Territory> output = new ObjectArrayList<>();
        for (int x = -r; x <= r; x++) {
            Vector2i top = center.add(x, r, new Vector2i());
            if (terrain.getTerritories().containsKey(top)) output.add(terrain.getTerritories().get(top));
            Vector2i bottom = center.add(x, -r, new Vector2i());
            if (terrain.getTerritories().containsKey(bottom)) output.add(terrain.getTerritories().get(bottom));
        }
        for (int y = -r + 1; y <= r - 1; y++) {
            Vector2i left = center.add(r, y, new Vector2i());
            if (terrain.getTerritories().containsKey(left)) output.add(terrain.getTerritories().get(left));
            Vector2i right = center.add(-r, y, new Vector2i());
            if (terrain.getTerritories().containsKey(right)) output.add(terrain.getTerritories().get(right));
        }
        return output;
    }

    public Territory getContainingTerritory(Vector2fc point) {
        Vector2ic center = getCell(point, terrain.generator.getMinDistance());
        int ringSize = 0;
        do {
            List<Territory> inRing = getContainingTerritoryRadius(center, ringSize++);  // Ringsize is postcremented
            if (inRing.size() > 0) {
                for (Territory territory : inRing) {
                    if (containsPoint(point, territory.vertices)) return territory;
                }
            }
        } while (ringSize < 10);
        return null;
    }

    public Territory getTerritoryUnderMouse() {
        Entity mainCamera = GameEngine.getMainCamera();
        Vector3f mousePos = cameraToPlane(mainCamera.getComponent(Transform.class).position,
            mainCamera.getComponent(Camera.class),
            Input.mousePos(),
            Conquerer.Q.worldPlane);
        return getContainingTerritory(new Vector2f(mousePos.x, mousePos.y));
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
            final int worldTerritoryEntity = GameEngine.instantiate(ShaderRender.class, MeshRender.class, Transform.class);
            territory.entities.add(worldTerritoryEntity);
            Entity ent = GameEngine.getEntity(worldTerritoryEntity);
            ent.getComponent(ShaderRender.class).shader = Conquerer.Q.shaderColored();
            ent.getComponent(MeshRender.class).mesh = meshBuilder.apply(new Mesh());
            ent.getComponent(Transform.class).position.zero();
        }

        // Generate territory name mesh
        {
            if (!territory.isWater) {
                FontHelper.FontBitmap font = Conquerer.Q.aleoAscii256();

                final Mesh.Builder meshBuilder = Mesh.builder();
                Vector2f textSize = new Vector2f();
                meshBuilder.addText(font, territory.name, 0.2f, textSize);
                textSize.mul(0.5f);
                final int worldTerritoryEntity = GameEngine.instantiate(ShaderRender.class, MeshRender.class, Transform.class, Textured.class);
                territory.entities.add(worldTerritoryEntity);
                final Entity ent = GameEngine.getEntity(worldTerritoryEntity);
                ent.getComponent(ShaderRender.class).shader = Conquerer.Q.shaderFont();
                ent.getComponent(ShaderRender.class).uniformCallbacks.put("color", s -> s.setUniform("color", new Vector4f(1.0f)));
                Mesh m = new Mesh();
                m.canBeWireframe = false;
                ent.getComponent(MeshRender.class).mesh = meshBuilder.apply(m);
                ent.getComponent(Transform.class).position.set(territory.location.sub(textSize.x, -textSize.y, new Vector2f()), 1.0f);
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
