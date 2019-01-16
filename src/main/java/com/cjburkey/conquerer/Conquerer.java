package com.cjburkey.conquerer;

import com.artemis.ArchetypeBuilder;
import com.artemis.BaseSystem;
import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.cjburkey.conquerer.ecs.component.Camera;
import com.cjburkey.conquerer.ecs.component.input.CameraMovement;
import com.cjburkey.conquerer.ecs.component.input.SmoothMovement;
import com.cjburkey.conquerer.ecs.component.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.render.ShaderRender;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.ecs.component.transform.Rot;
import com.cjburkey.conquerer.ecs.component.transform.Scale;
import com.cjburkey.conquerer.ecs.component.world.Location;
import com.cjburkey.conquerer.ecs.component.world.Territory;
import com.cjburkey.conquerer.ecs.system.CameraMovementSystem;
import com.cjburkey.conquerer.ecs.system.CameraSystem;
import com.cjburkey.conquerer.ecs.system.RenderSystem;
import com.cjburkey.conquerer.ecs.system.SmoothMovementSystem;
import com.cjburkey.conquerer.gen.Terrain;
import com.cjburkey.conquerer.gen.generator.BasicGenerator;
import com.cjburkey.conquerer.gl.Mesh;
import com.cjburkey.conquerer.gl.shader.BasicShader;
import com.cjburkey.conquerer.glfw.Window;
import de.tomgrill.artemis.GameLoopInvocationStrat;
import org.ajwerner.voronoi.VoronoiEdge;
import org.joml.Vector3f;

import static com.cjburkey.conquerer.Log.*;

/**
 * Created by CJ Burkey on 2019/01/10
 */
@SuppressWarnings("WeakerAccess")
public final class Conquerer {
    
    private static final int UPS = 60;
    
    public int mainCamera = -1;
    
    // Game engine
    private boolean running = false;
    private Window window;
    private BasicShader shader;
    
    private static final BaseSystem[] ARTEMIS_SYSTEMS = new BaseSystem[] {
            new SmoothMovementSystem(),
            new CameraSystem(),
            new CameraMovementSystem(),
            new RenderSystem(),
    };
    
    // Artemis ECS
    private World world = new World(new WorldConfigurationBuilder()
            .with(ARTEMIS_SYSTEMS)
            .register(new GameLoopInvocationStrat(10000 / UPS))
            .build());
    
    // Game world
    public final Terrain terrain = new Terrain(new BasicGenerator());
    
    private void startGame() {
        terrain.generate(50, 1.05f);
        System.out.printf("Generated territories: %s\n", terrain.getTerritoryCount());
        
        window = new Window("Conquerer v0.0.1", 300, 300, 4);
        window.setHalfMonitorSize();
        window.setCenter();
        window.setVsync(true);
        window.setClearColor(0.5f, 0.5f, 0.5f);
        window.show();
        
        shader = new BasicShader("basic", true, true, true);
        
        for (Entity territory : terrain.getTerritories()) {
            Location loc = territory.getComponent(Location.class);
            
            Mesh mesh = new Mesh().circle(0.15f, 16);
            int testObj = createObject(ShaderRender.class, MeshRender.class, Pos.class, Rot.class, Scale.class);
            Entity ent = world.getEntity(testObj);
            ent.getComponent(ShaderRender.class).shader = shader;
            ent.getComponent(MeshRender.class).mesh = mesh;
            ent.getComponent(Pos.class).position.set(loc.location.x(), loc.location.y(), -3.0f);
            
            for (VoronoiEdge edge : territory.getComponent(Territory.class).voronoiEdges) {
                info("Edge: {} to {}", edge.getPointA(), edge.getPointB());
                
                mesh = new Mesh().line(0.15f, edge.getPointA(), edge.getPointB());
                testObj = createObject(ShaderRender.class, MeshRender.class, Pos.class, Rot.class, Scale.class);
                ent = world.getEntity(testObj);
                ent.getComponent(ShaderRender.class).shader = shader;
                ent.getComponent(ShaderRender.class).color = new Vector3f(1.0f, 0.0f, 0.0f);
                ent.getComponent(MeshRender.class).mesh = mesh;
                ent.getComponent(Pos.class).position.set(0.0f, 0.0f, -3.0f);
                //ent.getComponent(Scale.class).scale.set(0.1f);
            }
        }
        
        // Create starting main camera
        mainCamera = createObject(Pos.class, Rot.class, SmoothMovement.class, Camera.class, CameraMovement.class);
        
        running = true;
        info("Initialized");
        
        while (running) {
            world.process();
            if (window.getShouldClose()) {
                exit();
            }
        }
        
        info("Exited");
        System.exit(0);
    }
    
    public void exit() {
        running = false;
    }
    
    public Window window() {
        return window;
    }
    
    public World world() {
        return world;
    }
    
    public BasicShader shader() {
        return shader;
    }
    
    @SafeVarargs
    public final int createObject(Class<? extends Component>... types) {
        ArchetypeBuilder builder = new ArchetypeBuilder();
        for (Class<? extends Component> type : types) {
            builder.add(type);
        }
        return world.create(builder.build(world));
    }
    
    // -- STATIC -- //
    
    public static final Conquerer INSTANCE = new Conquerer();
    
    public static void main(String[] args) {
        INSTANCE.startGame();
    }
    
}
