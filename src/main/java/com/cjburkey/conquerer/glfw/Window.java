package com.cjburkey.conquerer.glfw;

import java.util.Objects;
import org.joml.Vector3f;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Created by CJ Burkey on 2018/11/25
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Window {
    
    private static boolean init;
    
    private long window;
    private String title;
    private int width;
    private int height;
    private final Vector3f clearColor = new Vector3f();
    private boolean shouldClose = false;
    private boolean vsync = false;
    
    public Window(String title, int width, int height, int multisample) {
        this.title = title;
        this.width = width;
        this.height = height;
        
        // Initialize GLFW
        if (!init) {
            GLFWErrorCallback.createPrint(System.err).set();
            if (!glfwInit()) throw new IllegalStateException("Unable to initiailize GLFW");
            init = true;
        }
        
        // Prepare window settings
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, multisample);
        
        // Create window
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) throw new IllegalStateException("Failed to initialize GLFW window");
        
        // Add size callback
        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            this.width = w;
            this.height = h;
            glViewport(0, 0, w, h);
        });
        
        Input.init(window);
        
        // Initialize OpenGL in window
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        
        // Allow MSAA
        glEnable(GL_MULTISAMPLE);
        
        // Enable transparency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        // Ignore the back of faces on the meshes to reduce drawing
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }
    
    public void prepareUpdate() {
        Input.update();
        glfwPollEvents();
        shouldClose = glfwWindowShouldClose(window);
    }
    
    public void prepareFrame(int clearBits) {
        glClear(clearBits);
    }
    
    public void prepareFrame() {
        prepareFrame(GL_COLOR_BUFFER_BIT);
    }
    
    public void finishFrame() {
        glfwSwapBuffers(window);
    }
    
    public void show() {
        glfwShowWindow(window);
        glfwRequestWindowAttention(window);
    }
    
    public void hide() {
        glfwHideWindow(window);
    }
    
    public void destroy(boolean cleanupGlfw) {
        hide();
        Callbacks.glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        if (cleanupGlfw) {
            glfwTerminate();
            Objects.requireNonNull(glfwSetErrorCallback(null)).free();
        }
    }
    
    public void setTitle(String title) {
        this.title = title;
        glfwSetWindowTitle(window, title);
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setWidth(int width) {
        glfwSetWindowSize(window, width, height);
        this.width = width;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setHeight(int height) {
        glfwSetWindowSize(window, width, height);
        this.height = height;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHalfMonitorSize() {
        GLFWVidMode monitor = getMonitor();
        setWidth(monitor.width() / 2);
        setHeight(monitor.height() / 2);
    }
    
    public void setPosition(int x, int y) {
        glfwSetWindowPos(window, x, y);
    }
    
    public void setCenter() {
        GLFWVidMode monitor = getMonitor();
        setPosition((monitor.width() - width) / 2, (monitor.height() - height) / 2);
    }
    
    public Vector3f getClearColor() {
        return new Vector3f(clearColor);
    }
    
    public void setClearColor(float r, float g, float b) {
        glClearColor(r, g, b, 1.0f);
        clearColor.set(r, g, b);
    }
    
    public boolean getShouldClose() {
        return shouldClose;
    }
    
    public boolean getVsync() {
        return vsync;
    }
    
    public void setVsync(boolean vsync) {
        if (this.vsync == vsync) return;
        glfwSwapInterval(vsync ? 1 : 0);
        this.vsync = vsync;
    }
    
    public GLFWVidMode getMonitor(long monitor) {
        return glfwGetVideoMode(monitor);
    }
    
    public GLFWVidMode getMonitor() {
        return getMonitor(glfwGetPrimaryMonitor());
    }
    
}
