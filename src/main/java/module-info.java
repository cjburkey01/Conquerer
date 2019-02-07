/**
 * Created by CJ Burkey on 2019/02/06
 */
module com.cjburkey.conquerer {

    // Core Java
    requires java.base;

    // LWJGL3 and its dependencies
    requires org.joml;
    requires org.lwjgl.opengl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.stb;
    requires org.lwjgl.natives;
    requires org.lwjgl.opengl.natives;
    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.stb.natives;

    // Game engine
    requires artemis.odb;
    opens com.cjburkey.conquerer.ecs.system.engine;
    opens com.cjburkey.conquerer.ecs.system;
    exports com.cjburkey.conquerer.ecs.component.engine.render.ui;
    exports com.cjburkey.conquerer.ecs.component.engine.render;
    exports com.cjburkey.conquerer.ecs.component.engine;
    exports com.cjburkey.conquerer.ecs.component.input;
    exports com.cjburkey.conquerer.ecs.system.engine;
    exports com.cjburkey.conquerer.ecs.system;

    // Faster collections
    requires it.unimi.dsi.fastutil;

    // Debug
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

}
