package com.cjburkey.conquerer;

import com.artemis.BaseSystem;

/**
 * Created by CJ Burkey on 2019/02/03
 */
public interface IGame {

    void onInit();

    void onUpdate();

    void onExit();

    BaseSystem[] getInitialSystems();

    int getTargetUps();

}
