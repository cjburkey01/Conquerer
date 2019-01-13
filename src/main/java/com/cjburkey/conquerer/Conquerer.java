package com.cjburkey.conquerer;

import com.artemis.ArchetypeBuilder;
import com.artemis.BaseSystem;
import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.cjburkey.conquerer.ecs.component.Camera;
import com.cjburkey.conquerer.ecs.component.render.MeshRender;
import com.cjburkey.conquerer.ecs.component.render.ShaderRender;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.ecs.component.transform.Rot;
import com.cjburkey.conquerer.ecs.component.transform.Scale;
import com.cjburkey.conquerer.ecs.system.CameraSystem;
import com.cjburkey.conquerer.ecs.system.RenderSystem;
import com.cjburkey.conquerer.gen.Terrain;
import com.cjburkey.conquerer.gen.generator.BasicGenerator;
import com.cjburkey.conquerer.gl.Mesh;
import com.cjburkey.conquerer.gl.shader.BasicShader;
import com.cjburkey.conquerer.glfw.Window;
import de.tomgrill.artemis.GameLoopInvocationStrat;

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
            new CameraSystem(),
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
//        terrain.generate(3, 100.0f);
//        System.out.printf("Generated territories: %s\n", world.getTerritoryCount());
        
        window = new Window("Conquerer v0.0.1", 300, 300, 4);
        window.setHalfMonitorSize();
        window.setCenter();
        window.setVsync(true);
        window.setClearColor(0.5f, 0.5f, 0.5f);
        window.show();
        
        shader = new BasicShader("basic", true, true, true);
        
        Mesh mesh = new Mesh().setVertices(new float[] {
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
        }).setIndices(new short[] {
                0, 1, 2,
        });
        
        int testObj = createObject(ShaderRender.class, MeshRender.class, Pos.class, Rot.class, Scale.class);
        Entity ent = world.getEntity(testObj);
        ent.getComponent(ShaderRender.class).shader = shader;
        ent.getComponent(MeshRender.class).mesh = mesh;
        ent.getComponent(Pos.class).position.set(0.0f, 0.0f, -3.0f);
        ent.getComponent(Scale.class).scale.set(150.0f);
        
        // Create starting main camera
        mainCamera = createObject(Pos.class, Rot.class, Camera.class);
        
        running = true;
        Log.info("Initialized");
        
        while (running) {
            world.process();
            if (window.getShouldClose()) {
                exit();
            }
        }
        
        Log.info("Exited");
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
