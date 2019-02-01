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
import com.cjburkey.conquerer.ecs.system.UiElementSystem;
import com.cjburkey.conquerer.gl.FontHelper;
import com.cjburkey.conquerer.gl.shader.BasicShader;
import com.cjburkey.conquerer.glfw.Input;
import com.cjburkey.conquerer.glfw.Window;
import com.cjburkey.conquerer.math.Plane;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.ui.UiSolidBox;
import com.cjburkey.conquerer.ui.UiText;
import com.cjburkey.conquerer.util.Util;
import com.cjburkey.conquerer.world.Territory;
import com.cjburkey.conquerer.world.WorldHandler;
import de.tomgrill.artemis.GameLoopInvocationStrat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static com.cjburkey.conquerer.Log.*;
import static com.cjburkey.conquerer.math.Transformation.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Created by CJ Burkey on 2019/01/10
 */
@SuppressWarnings({"WeakerAccess", "unused", "FieldCanBeLocal"})
public final class Conquerer {
    
    private Conquerer() {
        debug("Running in JVM {}", System.getProperty("java.version"));
    }
    
    /*
        This is my main area for to-do items:
        
        TODO: ADD ENTITY TRANSFORM PARENTS
        TODO: HIDE LINES BETWEEN TERRITORIES OWNED BY THE SAME EMPIRE
     */
    
    // Util
    private static final int UPS = 60;
    public static final Random RANDOM = new Random(System.nanoTime());
    
    // Keep list of things that may not be perfectly cleaned up to be automatically reclaimed on exit
    public static final ObjectArrayList<Runnable> onExit = new ObjectArrayList<>();
    
    // Fonts
    public static final FontHelper.Font roboto = FontHelper.loadFont(Util.getStreanForResource("font/Roboto/Roboto-Regular.ttf").orElse(null));
    public static final FontHelper.Font aleo = FontHelper.loadFont(Util.getStreanForResource("font/Aleo/Aleo-Regular.ttf").orElse(null));
    
    // Pre-baked font bitmaps
    private static FontHelper.FontBitmap robotoAscii256;
    private static FontHelper.FontBitmap aleoAscii256;
    
    // The current main camera entity
    public int mainCamera = -1;
    
    // Game engine
    private boolean running = false;
    private Window window;
    private BasicShader shaderColored;
    private BasicShader shaderTextured;
    private BasicShader shaderFont;
    
    // List of systems to be used with the world
    private static final BaseSystem[] ARTEMIS_SYSTEMS = new BaseSystem[] {
            new SmoothMovementSystem(),
            new CameraSystem(),
            new CameraMovementSystem(),
            new RenderSystem(),
            new UiElementSystem(),
    };
    
    // This is the game loop we use
    // It allows separation of render and update/logic tasks.
    // It creates a constant world update rate and a variable render rate
    private GameLoopInvocationStrat gameLoop = new GameLoopInvocationStrat((int) (1000.0f / UPS));
    
    // Artemis ECS world
    private World world = new World(new WorldConfigurationBuilder()
            .with(ARTEMIS_SYSTEMS)
            .register(gameLoop)
            .build());
    
    // Game world
    public final WorldHandler worldHandler = new WorldHandler(1.0f, Rectf.fromCenter(0.0f, 0.0f, 30.0f, 30.0f));
    
    // The plane to be flat with the world to allow calculating rays against the "playing field"
    public final Plane worldPlane = new Plane(new Vector3f(), new Vector3f(0.0f, 0.0f, 1.0f));
    
    // Test UI
    private UiText fpsDisplay;
    private UiText upsDisplay;
    private UiText territoryDisplay;
    private UiText territoryBiomeDisplay;
    private UiText territoryLocDisplay;
    private UiText territoryOceanDisplay;
    
    private void startGame() {
        // Create and initialize both the window and the OpenGL context
        window = new Window("Conquerer v0.0.1", 300, 300, 4);
        window.setHalfMonitorSize();
        window.setCenter();
        window.setVsync(true);
        window.setClearColor(0.1f, 0.1f, 0.1f);
        window.show();
        
        // Prebuild font bitmaps
        robotoAscii256 = roboto.generateAsciiBitmap(256);
        aleoAscii256 = aleo.generateAsciiBitmap(256);
        
        // Load the shaders that we'll need
        shaderColored = new BasicShader("colored/colored", true, true, true);
        shaderTextured = new BasicShader("textured/textured", true, true, true);
        shaderFont = new BasicShader("font/font", true, true, true);
        
        // Initialize UI shader
        // This not kept in this class because it can be accessed with UiElementSystem.shader()
        UiElementSystem.initShader(new BasicShader("ui/ui", true, false, true));
        
        // Generate and render the world
        worldHandler.generateWorld(RANDOM);
        
        // Debug display
        {
            // Background
            new UiSolidBox(new Vector2f(375.0f, 6 * 24.0f + 12.0f))
                    .setColor(new Vector3f(0.0f));
            
            // FPS
            fpsDisplay = new UiText("FPS: ", robotoAscii256)
                    .setColor(new Vector3f(1.0f, 1.0f, 1.0f))
                    .setSize(24.0f);
            fpsDisplay.position().set(20.0f, 6.0f, 0.0f);
            
            // UPS
            upsDisplay = new UiText("UPS: ", robotoAscii256)
                    .setColor(new Vector3f(1.0f, 1.0f, 1.0f))
                    .setSize(24.0f);
            upsDisplay.position().set(20.0f, 30.0f, 0.0f);
            
            // Current territory
            territoryDisplay = new UiText("Territory: ", robotoAscii256)
                    .setColor(new Vector3f(1.0f, 1.0f, 1.0f))
                    .setSize(24.0f);
            territoryDisplay.position().set(20.0f, 54.0f, 0.0f);
            
            // Current territory biome
            territoryBiomeDisplay = new UiText("Biome: ", robotoAscii256)
                    .setColor(new Vector3f(1.0f, 1.0f, 1.0f))
                    .setSize(24.0f);
            territoryBiomeDisplay.position().set(20.0f, 78.0f, 0.0f);
            
            // Current territory location
            territoryLocDisplay = new UiText("Location: ", robotoAscii256)
                    .setColor(new Vector3f(1.0f, 1.0f, 1.0f))
                    .setSize(24.0f);
            territoryLocDisplay.position().set(20.0f, 102.0f, 0.0f);
            
            // Current territory ocean
            territoryOceanDisplay = new UiText("Ocean: ", robotoAscii256)
                    .setColor(new Vector3f(1.0f, 1.0f, 1.0f))
                    .setSize(24.0f);
            territoryOceanDisplay.position().set(20.0f, 126.0f, 0.0f);
        }
        
        // Create starting main camera
        mainCamera = createEntity(Pos.class, Rot.class, SmoothMovement.class, Camera.class, CameraMovement.class);
        world.getEntity(mainCamera).getComponent(CameraMovement.class).bounds = worldHandler.terrainBounds;
        world.getEntity(mainCamera).getComponent(Pos.class).position.z = 3.0f;
        world.getEntity(mainCamera).getComponent(SmoothMovement.class).goalPosition.set(0.0f, 0.0f, 3.0f);
        
        // Get ready
        running = true;
        info("Initialized");
        
        // Go!
        while (running) {
            world.process();
            if (window.getShouldClose()) {
                exit();
            }
        }
        
        // Cleanup :)
        onExit.forEach(Runnable::run);
        
        info("Exited");
        System.exit(0);
    }
    
    public void exit() {
        info("Exiting");
        running = false;
    }
    
    public Window window() {
        return window;
    }
    
    public World world() {
        return world;
    }
    
    public FontHelper.FontBitmap robotoAscii256() {
        return robotoAscii256;
    }
    
    public FontHelper.FontBitmap aleoAscii256() {
        return aleoAscii256;
    }
    
    public BasicShader shaderColored() {
        return shaderColored;
    }
    
    public BasicShader shaderTextured() {
        return shaderTextured;
    }
    
    public BasicShader shaderFont() {
        return shaderFont;
    }
    
    private boolean fill = true;
    private long lastDebugTextUpdateTime = System.nanoTime();
    public void onFrameUpdate() {
        if (Input.getKeyPressed(GLFW_KEY_C)) {
            fill = !fill;
            glPolygonMode(GL_FRONT_AND_BACK, fill ? GL_FILL : GL_LINE);
        }
        if (Input.getKeyPressed(GLFW_KEY_ESCAPE)) {
            exit();
        }
        if (Input.getKeyPressed(GLFW_KEY_R)) {
            info("Regenerating terrain");
            worldHandler.generateWorld(RANDOM);
        }
        if ((System.nanoTime() - lastDebugTextUpdateTime) >= 1000000000.0f / 10.0f) {
            fpsDisplay.setText(String.format("FPS: %.2f", 1.0f / gameLoop.lastRenderDelta()));
            upsDisplay.setText(String.format("UPS: %.2f", 1.0f / gameLoop.getUpdateDelta()));
            
            Vector3f mousePos = cameraToPlane(world.getEntity(mainCamera).getComponent(Pos.class).position,
                    world.getEntity(mainCamera).getComponent(Camera.class),
                    Input.mousePos(),
                    INSTANCE.worldPlane);
            Territory at = worldHandler.terrain.getContainingTerritory(new Vector2f(mousePos.x, mousePos.y));
            territoryDisplay.setText(String.format("Territory: %s", ((at == null) ? "None" : at.name)));
            territoryBiomeDisplay.setText(String.format("Biome: %s", ((at == null) ? "None" : at.getBiome().name)));
            territoryLocDisplay.setText(String.format("Location: %s", ((at == null) ? "None" : "(" + Util.format(at.location) + ")")));
            territoryOceanDisplay.setText(String.format("Ocean: %s", ((at == null) ? "Talse" : (at.isWater ? "True" : "False"))));
            
            lastDebugTextUpdateTime = System.nanoTime();
        }
        // TODO: TERRITORY NAME
        
    }
    
    @SafeVarargs
    public final int createEntity(Class<? extends Component>... types) {
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
