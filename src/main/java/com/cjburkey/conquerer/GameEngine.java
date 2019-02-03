package com.cjburkey.conquerer;

import com.artemis.ArchetypeBuilder;
import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.cjburkey.conquerer.ecs.system.engine.CameraSystem;
import com.cjburkey.conquerer.ecs.system.engine.RenderSystem;
import com.cjburkey.conquerer.ecs.system.engine.UiElementSystem;
import com.cjburkey.conquerer.glfw.Window;
import de.tomgrill.artemis.GameLoopInvocationStrat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Random;

/**
 * Created by CJ Burkey on 2019/02/03
 */
@SuppressWarnings("WeakerAccess")
public final class GameEngine {

    public static final Random RAND = new Random(System.nanoTime());
    // Keep list of things that may not be perfectly cleaned up to be automatically reclaimed on exit
    private static final ObjectArrayList<Runnable> onExit = new ObjectArrayList<>();
    // The current main camera entity
    public static int mainCamera = -1;
    private static IGame INSTANCE;
    private static Window window;
    // Game loop state
    private static boolean running = false;
    // Artemis ECS world
    private static World world;
    // This is the game loop we use
    // It allows separation of render and update/logic tasks.
    // It creates a constant world update rate and a variable render rate
    private static GameLoopInvocationStrat gameLoop;
    private static boolean isWireframe = false;

    private GameEngine() {
        throw new UnsupportedOperationException("Cannot instantiate the static game engine");
    }

    public static void start(IGame instance) {
        if (instance == null) throw new NullPointerException("Game engine implementation cannot be null");
        INSTANCE = instance;

        initWindow();
        initGameLoop(instance.getTargetUps());
        initWorld();
        startGameLoop();
    }

    private static void initWindow() {
        // Create and initialize both the window and the OpenGL context
        window = new Window("Conquerer v0.0.1", 300, 300, 4);
        window.setHalfMonitorSize();
        window.setCenter();
        window.setVsync(true);
        window.setClearColor(0.1f, 0.1f, 0.1f);
        window.show();
    }

    private static void initGameLoop(int ups) {
        gameLoop = new GameLoopInvocationStrat((int) (1000.0f / ups));
    }

    private static void initWorld() {
        world = new World(new WorldConfigurationBuilder()
                .with(new CameraSystem())
                .with(new RenderSystem())
                .with(INSTANCE().getInitialSystems())
                .with(new UiElementSystem())
                .register(gameLoop)
                .build());
    }

    private static void startGameLoop() {
        running = true;
        INSTANCE().onInit();

        while (running) {
            world.process();
            if (window.getShouldClose()) {
                exit();
            }
        }

        INSTANCE.onExit();
        onExit.forEach(Runnable::run);
        System.exit(0);
    }

    public static void exit() {
        running = false;
    }

    public static IGame INSTANCE() {
        return INSTANCE;
    }

    public static <T extends IGame> T INSTANCE(Class<T> gameEngineType) {
        return gameEngineType.cast(INSTANCE);
    }

    public static Window window() {
        return window;
    }

    public static GameLoopInvocationStrat gameLoop() {
        return gameLoop;
    }

    public static void delete(int id) {
        world.delete(id);
    }

    public static void delete(Entity entity) {
        world.deleteEntity(entity);
    }

    public static Entity getEntity(int id) {
        return world.getEntity(id);
    }

    public static Entity getMainCamera() {
        return getEntity(mainCamera);
    }

    @SafeVarargs
    public static int instantiate(Class<? extends Component>... types) {
        ArchetypeBuilder builder = new ArchetypeBuilder();
        for (Class<? extends Component> type : types) builder.add(type);
        return world.create(builder.build(world));
    }

    @SafeVarargs
    public static Entity instantiateEntity(Class<? extends Component>... types) {
        return getEntity(instantiate(types));
    }

    public static boolean isWireframe() {
        return isWireframe;
    }

    public static void toggleWireframe() {
        isWireframe = !isWireframe;
    }

    public static void onExit(Runnable cleanup) {
        onExit.add(cleanup);
    }

}
