package com.cjburkey.conquerer;

import com.artemis.BaseSystem;
import com.artemis.Entity;
import com.cjburkey.conquerer.ecs.component.Camera;
import com.cjburkey.conquerer.ecs.component.input.CameraMovement;
import com.cjburkey.conquerer.ecs.component.input.SmoothMovement;
import com.cjburkey.conquerer.ecs.component.transform.Pos;
import com.cjburkey.conquerer.ecs.component.transform.Rot;
import com.cjburkey.conquerer.ecs.system.CameraMovementSystem;
import com.cjburkey.conquerer.ecs.system.SmoothMovementSystem;
import com.cjburkey.conquerer.ecs.system.engine.UiElementSystem;
import com.cjburkey.conquerer.game.ConquererHandler;
import com.cjburkey.conquerer.game.DebugDisplay;
import com.cjburkey.conquerer.gl.FontHelper;
import com.cjburkey.conquerer.gl.shader.BasicShader;
import com.cjburkey.conquerer.glfw.Input;
import com.cjburkey.conquerer.math.Plane;
import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.util.Util;
import com.cjburkey.conquerer.world.WorldHandler;
import org.joml.Vector3f;

import static com.cjburkey.conquerer.Log.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by CJ Burkey on 2019/01/10
 */
@SuppressWarnings({"WeakerAccess", "unused", "FieldCanBeLocal"})
public final class Conquerer implements IGame {

    public static final Conquerer SELF = new Conquerer();

    /*
        This is my main area for to-do items:
        
        TODO: ADD ENTITY TRANSFORM PARENTS
        TODO: HIDE LINES BETWEEN TERRITORIES OWNED BY THE SAME EMPIRE
     */

    // Fonts
    public static final FontHelper.Font roboto = FontHelper.loadFont(Util.getStreanForResource("font/Roboto/Roboto-Regular.ttf").orElse(null));
    public static final FontHelper.Font aleo = FontHelper.loadFont(Util.getStreanForResource("font/Aleo/Aleo-Regular.ttf").orElse(null));
    // Pre-baked font bitmaps
    private static FontHelper.FontBitmap robotoAscii256;
    private static FontHelper.FontBitmap aleoAscii256;
    // Game world
    public final WorldHandler worldHandler = new WorldHandler(1.0f, Rectf.fromCenter(0.0f, 0.0f, 30.0f, 30.0f));
    // The plane to be flat with the terrain and allow calculating mouse positions via rays with the terrain
    public final Plane worldPlane = new Plane(new Vector3f(), new Vector3f(0.0f, 0.0f, 1.0f));
    // Shaders
    private BasicShader shaderColored;
    private BasicShader shaderTextured;
    private BasicShader shaderFont;
    // Test UI
    private DebugDisplay debugDisplay;
    private long lastDebugTextUpdateTime = System.nanoTime();

    private Conquerer() {
        debug("Running in JVM {}", System.getProperty("java.version"));
    }

    public static void main(String[] args) {
        GameEngine.start(SELF);
    }

    public BaseSystem[] getInitialSystems() {
        return new BaseSystem[] {
                new SmoothMovementSystem(),
                new CameraMovementSystem(),
        };
    }

    public int getTargetUpdateRate() {
        return 60;
    }

    private void initFonts() {
        // Prebuild font bitmaps
        robotoAscii256 = roboto.generateAsciiBitmap(256);
        aleoAscii256 = aleo.generateAsciiBitmap(256);
    }

    private void initShaders() {
        // Load the shaders that we'll need
        shaderColored = new BasicShader("colored/colored", true, true, true);
        shaderTextured = new BasicShader("textured/textured", true, true, true);
        shaderFont = new BasicShader("font/font", true, true, true);

        // Initialize UI shader
        // This not kept in this class because it can be accessed with UiElementSystem.shader()
        UiElementSystem.initShader(new BasicShader("ui/ui", true, false, true));
    }

    private void initTerrain() {
        // Generate and render the terrain
        worldHandler.generateWorld(GameEngine.RAND);
        worldHandler.generateTerrainGraphics();
    }

    private void initDebugText() {
        debugDisplay = new DebugDisplay();
    }

    private void initMainCamera() {
        // Create starting main camera
        GameEngine.mainCamera = GameEngine.instantiate(Pos.class, Rot.class, SmoothMovement.class, Camera.class, CameraMovement.class);
        Entity mainCameraEntity = GameEngine.getMainCamera();
        mainCameraEntity.getComponent(CameraMovement.class).bounds = worldHandler.terrainBounds;
        mainCameraEntity.getComponent(Pos.class).position.z = 3.0f;
        mainCameraEntity.getComponent(SmoothMovement.class).goalPosition.set(0.0f, 0.0f, 3.0f);
    }

    private void initPlayerEmpire() {
        ConquererHandler.playerEmpire.set(worldHandler.empireHandler.generate(GameEngine.RAND));
    }

    public void onInit() {
        initFonts();
        initShaders();
        initTerrain();
        initMainCamera();
        initDebugText();
        initPlayerEmpire();
    }

    public void onExit() {
    }

    public void onUpdate() {
        Entity mainCamera = GameEngine.getMainCamera();

        if (Input.getKeyPressed(GLFW_KEY_C)) {
            GameEngine.toggleWireframe();
        }
        if (Input.getKeyPressed(GLFW_KEY_ESCAPE)) {
            GameEngine.exit();
        }
        if (Input.getKeyPressed(GLFW_KEY_R)) {
            info("Regenerating terrain");
            worldHandler.generateWorld(GameEngine.RAND);
            worldHandler.generateTerrainGraphics();
        }
        if (Input.getKeyPressed(GLFW_KEY_F1)) {
            debugDisplay.enabled.toggle();
        }
        if ((System.nanoTime() - lastDebugTextUpdateTime) >= 1000000000.0f / 10.0f) {
            debugDisplay.updateDisplay();
            lastDebugTextUpdateTime = System.nanoTime();
        }
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

}
