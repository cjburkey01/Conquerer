package com.cjburkey.conquerer.ecs.component.input;

import com.artemis.Component;
import com.cjburkey.conquerer.math.Rectf;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by CJ Burkey on 2019/01/15
 */
public class CameraMovement extends Component {
    
    public float goalZoom = 0.0f;
    public float[] zoomVelocity = new float[] { 0.0f };
    
    public float speed = 50.0f;
    public float zoomSpeed = 1.0f;
    public float zoomMoveRatio = 0.5f;
    public float zoomClose = 5.0f;
    public float zoomFar = 50.0f;
    
    public Rectf bounds = Rectf.infinite();
    
    public final Vector2f previousMousePos = new Vector2f();
    
    // Input
    public final IntOpenHashSet activatingMouseForDrag = new IntOpenHashSet();
    public final IntOpenHashSet leftKeys = new IntOpenHashSet();
    public final IntOpenHashSet rightKeys = new IntOpenHashSet();
    public final IntOpenHashSet upKeys = new IntOpenHashSet();
    public final IntOpenHashSet downKeys = new IntOpenHashSet();
    
    public CameraMovement() {
        activatingMouseForDrag.add(GLFW_MOUSE_BUTTON_MIDDLE);
        
        leftKeys.add(GLFW_KEY_LEFT);
        leftKeys.add(GLFW_KEY_A);
        
        rightKeys.add(GLFW_KEY_RIGHT);
        rightKeys.add(GLFW_KEY_D);
        
        upKeys.add(GLFW_KEY_UP);
        upKeys.add(GLFW_KEY_W);
        
        downKeys.add(GLFW_KEY_DOWN);
        downKeys.add(GLFW_KEY_S);
    }
    
}
