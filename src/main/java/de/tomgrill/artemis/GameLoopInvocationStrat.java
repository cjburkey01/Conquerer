/*
    Copyright 2015 See AUTHORS file.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package de.tomgrill.artemis;

import com.artemis.BaseSystem;
import com.artemis.SystemInvocationStrategy;
import com.artemis.utils.BitVector;
import com.cjburkey.conquerer.GameEngine;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Implements a game loop based on this excellent blog post:
 * http://gafferongames.com/game-physics/fix-your-timestep/
 * <p>
 * To avoid floating point rounding errors we only use fixed point numbers for calculations.
 */
public class GameLoopInvocationStrat extends SystemInvocationStrategy {

    private final ObjectArrayList<BaseSystem> logicMarkedSystems;
    private final ObjectArrayList<BaseSystem> otherSystems;

    private final long nanosPerLogicTick; // ~ dt
    private final BitVector disabledlogicMarkedSystems = new BitVector();
    private final BitVector disabledOtherSystems = new BitVector();
    private long currentTime = System.nanoTime();
    private float lastRenderDelta = 0.0f;
    private long accumulator;
    private boolean systemsSorted = false;

    public GameLoopInvocationStrat(int millisPerLogicTick) {
        this.nanosPerLogicTick = TimeUnit.MILLISECONDS.toNanos(millisPerLogicTick);
        logicMarkedSystems = new ObjectArrayList<>();
        otherSystems = new ObjectArrayList<>();
    }

    @Override
    protected void initialize() {
        // Sort Sytems here in case setEnabled() is called prior to first process()
        if (!systemsSorted) {
            sortSystems();
        }
    }

    private void sortSystems() {
        if (!systemsSorted) {
            Object[] systemsData = systems.getData();
            for (int i = 0, s = systems.size(); s > i; i++) {
                BaseSystem system = (BaseSystem) systemsData[i];
                if (system instanceof ILogic) {
                    logicMarkedSystems.add(system);
                } else {
                    otherSystems.add(system);
                }
            }
            systemsSorted = true;
        }
    }

    @Override
    protected void process() {
        if (!systemsSorted) {
            sortSystems();
        }

        long newTime = System.nanoTime();
        long frameTime = newTime - currentTime;

        if (frameTime > 250000000) {
            frameTime = 250000000;    // Note: Avoid spiral of death
        }

        currentTime = newTime;
        accumulator += frameTime;

        // required since artemis-odb-2.0.0-RC4, updateEntityStates() must be called
        // before processing the first system - in case any entities are
        // added outside the main process loop
        updateEntityStates();

        world.setDelta(nanosPerLogicTick / 1000000000.0f);

        /* LOGIC */

        GameEngine.window().prepareUpdate();
        while (accumulator >= nanosPerLogicTick) {
            // Process all entity systems inheriting from ILogic
            for (int i = 0; i < logicMarkedSystems.size(); i++) {
                // Make sure your systems keep the current state before calculating the new state
                // else you cannot interpolate later on when rendering
                if (disabledlogicMarkedSystems.get(i)) {
                    continue;
                }
                logicMarkedSystems.get(i).process();
                updateEntityStates();
            }

            accumulator -= nanosPerLogicTick;
        }

        /* RENDER */

        lastRenderDelta = frameTime / 1000000000.0f;
        world.setDelta(lastRenderDelta);

        GameEngine.window().prepareFrame();
        // Process all NON ILogic inheriting entity systems
        for (int i = 0; i < otherSystems.size(); i++) {
            if (disabledOtherSystems.get(i)) {
                continue;
            }
            otherSystems.get(i).process();
            updateEntityStates();
        }
        GameEngine.INSTANCE().onUpdate();
        GameEngine.window().finishFrame();
    }

    @Override
    public boolean isEnabled(BaseSystem target) {
        ObjectArrayList<BaseSystem> systems = (target instanceof ILogic) ? logicMarkedSystems : otherSystems;
        BitVector disabledSystems = (target instanceof ILogic) ? disabledlogicMarkedSystems : disabledOtherSystems;
        Class<?> targetClass = target.getClass();
        for (int i = 0; i < systems.size(); i++) {
            if (targetClass == systems.get(i).getClass())
                return !disabledSystems.get(i);
        }
        throw new RuntimeException("System not found in this world");
    }

    @Override
    public void setEnabled(BaseSystem target, boolean value) {
        ObjectArrayList<BaseSystem> systems = (target instanceof ILogic) ? logicMarkedSystems : otherSystems;
        BitVector disabledSystems = (target instanceof ILogic) ? disabledlogicMarkedSystems : disabledOtherSystems;
        Class<?> targetClass = target.getClass();
        for (int i = 0; i < systems.size(); i++) {
            if (targetClass == systems.get(i).getClass()) {
                disabledSystems.set(i, !value);
                break;
            }
        }
    }

    public float getUpdateDelta() {
        return nanosPerLogicTick / 1000000000.0f;
    }

    public float lastRenderDelta() {
        return lastRenderDelta;
    }

}

