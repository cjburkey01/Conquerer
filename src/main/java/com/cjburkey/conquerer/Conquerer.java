package com.cjburkey.conquerer;

import com.artemis.ArchetypeBuilder;
import com.artemis.BaseSystem;
import com.artemis.Component;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.cjburkey.conquerer.ecs.component.Camera;
import com.cjburkey.conquerer.ecs.component.input.CameraMovement;
import com.cjburkey.conquerer.ecs.component.input.SmoothMovement;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.ecs.component.transform.Rot;
import com.cjburkey.conquerer.ecs.system.CameraMovementSystem;
import com.cjburkey.conquerer.ecs.system.CameraSystem;
import com.cjburkey.conquerer.ecs.system.RenderSystem;
import com.cjburkey.conquerer.ecs.system.SmoothMovementSystem;
import com.cjburkey.conquerer.gl.shader.BasicShader;
import com.cjburkey.conquerer.glfw.Window;
import com.cjburkey.conquerer.world.WorldHandler;
import de.tomgrill.artemis.GameLoopInvocationStrat;

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
    public final WorldHandler worldHandler = new WorldHandler();
    
    private void startGame() {
        window = new Window("Conquerer v0.0.1", 300, 300, 4);
        window.setHalfMonitorSize();
        window.setCenter();
        window.setVsync(true);
        window.setClearColor(0.1f, 0.1f, 0.1f);
        window.show();
        
        shader = new BasicShader("basic", true, true, true);
        
        // Generate and render the world
        worldHandler.generateWorld();
        
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
