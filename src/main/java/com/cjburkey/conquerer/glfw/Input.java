package com.cjburkey.conquerer.glfw;

import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.function.Function;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by CJ Burkey on 2018/12/11
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class Input {
    
    private static final Int2BooleanOpenHashMap keyboardButtons = new Int2BooleanOpenHashMap();
    private static final IntOpenHashSet keyboardButtonsUp = new IntOpenHashSet();
    private static final Int2BooleanOpenHashMap mouseButtons = new Int2BooleanOpenHashMap();
    private static final IntOpenHashSet mouseButtonsUp = new IntOpenHashSet();
    
    private static final Vector2f previousMousePos = new Vector2f();
    private static final Vector2f mousePos = new Vector2f();
    private static final Vector2f deltaMousePos = new Vector2f();
    private static final Vector2f deltaScroll = new Vector2f();
    
    static void init(long window) {
        keyboardButtons.keySet().forEach((int key) -> keyboardButtons.put(key, false));
        keyboardButtonsUp.clear();
        mouseButtons.keySet().forEach((int button) -> mouseButtons.put(button, false));
        mouseButtonsUp.clear();
        
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                keyboardButtons.put(key, true);
            } else if (action == GLFW_RELEASE) {
                keyboardButtons.remove(key);
                keyboardButtonsUp.add(key);
            }
        });
        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (action == GLFW_PRESS) {
                mouseButtons.put(button, true);
            } else if (action == GLFW_RELEASE) {
                mouseButtons.remove(button);
                mouseButtonsUp.add(button);
            }
        });
        glfwSetCursorPosCallback(window, (win, x, y) -> {
            previousMousePos.set(mousePos);
            mousePos.set((float) x, (float) y);
            mousePos.sub(previousMousePos, deltaMousePos);
        });
        glfwSetScrollCallback(window, (win, x, y) -> deltaScroll.set((float) x, (float) y));
    }
    
    static void update() {
        deltaMousePos.zero();
        deltaScroll.zero();
    }
    
    public static boolean getKeyDown(int key) {
        return keyboardButtons.containsKey(key);
    }
    
    public static boolean getAnyKeysDown(int... keys) {
        return getAny(Input::getKeyDown, keys);
    }
    
    public static boolean getAnyKeysDown(IntCollection keys) {
        return getAny(Input::getKeyDown, keys);
    }
    
    public static boolean getAllKeysDown(int... keys) {
        return getAll(Input::getKeyDown, keys);
    }
    
    public static boolean getAllKeysDown(IntCollection keys) {
        return getAll(Input::getKeyDown, keys);
    }
    
    public static boolean getKeyPressed(int key) {
        return getKeyDown(key) && keyboardButtons.get(key);
    }
    
    public static boolean getAnyKeysPressed(int... keys) {
        return getAny(Input::getKeyPressed, keys);
    }
    
    public static boolean getAnyKeysPressed(IntCollection keys) {
        return getAny(Input::getKeyPressed, keys);
    }
    
    public static boolean getAllKeysPressed(int... keys) {
        return getAll(Input::getKeyPressed, keys);
    }
    
    public static boolean getAllKeysPressed(IntCollection keys) {
        return getAll(Input::getKeyPressed, keys);
    }
    
    public static boolean getKeyUp(int key) {
        return keyboardButtonsUp.contains(key);
    }
    
    public static boolean getAnyKeysUp(int... keys) {
        return getAny(Input::getKeyUp, keys);
    }
    
    public static boolean getAnyKeysUp(IntCollection keys) {
        return getAny(Input::getKeyUp, keys);
    }
    
    public static boolean getAllKeysUp(int... keys) {
        return getAll(Input::getKeyUp, keys);
    }
    
    public static boolean getAllKeysUp(IntCollection keys) {
        return getAll(Input::getKeyUp, keys);
    }
    
    public static boolean getMouseDown(int mouseButton) {
        return mouseButtons.containsKey(mouseButton);
    }
    
    public static boolean getAnyMouseDown(int... buttons) {
        return getAny(Input::getMouseDown, buttons);
    }
    
    public static boolean getAnyMouseDown(IntCollection buttons) {
        return getAny(Input::getMouseDown, buttons);
    }
    
    public static boolean getAllMouseDown(int... buttons) {
        return getAll(Input::getMouseDown, buttons);
    }
    
    public static boolean getAllMouseDown(IntCollection buttons) {
        return getAll(Input::getMouseDown, buttons);
    }
    
    public static boolean getMousePressed(int mouseButton) {
        return getMouseDown(mouseButton) && mouseButtons.get(mouseButton);
    }
    
    public static boolean getAnyMousePressed(int... buttons) {
        return getAny(Input::getMousePressed, buttons);
    }
    
    public static boolean getAnyMousePressed(IntCollection buttons) {
        return getAny(Input::getMousePressed, buttons);
    }
    
    public static boolean getAllMousePressed(int... buttons) {
        return getAll(Input::getMousePressed, buttons);
    }
    
    public static boolean getAllMousePressed(IntCollection buttons) {
        return getAll(Input::getMousePressed, buttons);
    }
    
    public static boolean getMouseUp(int mouseButton) {
        return mouseButtonsUp.contains(mouseButton);
    }
    
    public static boolean getAnyMouseUp(int... buttons) {
        return getAny(Input::getMouseUp, buttons);
    }
    
    public static boolean getAnyMouseUp(IntCollection buttons) {
        return getAny(Input::getMouseUp, buttons);
    }
    
    public static boolean getAllMouseUp(int... buttons) {
        return getAll(Input::getMouseUp, buttons);
    }
    
    public static boolean getAllMouseUp(IntCollection buttons) {
        return getAll(Input::getMouseUp, buttons);
    }
    
    private static boolean getAny(Function<Integer, Boolean> function, int... codes) {
        for (int code : codes) {
            if (function.apply(code)) return true;
        }
        return false;
    }
    
    private static boolean getAny(Function<Integer, Boolean> function, IntCollection codes) {
        for (int code : codes) {
            if (function.apply(code)) return true;
        }
        return false;
    }
    
    private static boolean getAll(Function<Integer, Boolean> function, int... codes) {
        for (int code : codes) {
            if (!function.apply(code)) return false;
        }
        return true;
    }
    
    private static boolean getAll(Function<Integer, Boolean> function, IntCollection codes) {
        for (int code : codes) {
            if (!function.apply(code)) return false;
        }
        return true;
    }
    
    public static Vector2fc mousePos() {
        return mousePos;
    }
    
    public static Vector2fc mouseDelta() {
        return deltaMousePos;
    }
    
    public static Vector2fc getDeltaScrollVector() {
        return deltaScroll;
    }
    
    public static float scrollX() {
        return deltaScroll.x();
    }
    
    public static float scrollY() {
        return deltaScroll.y();
    }
    
}
