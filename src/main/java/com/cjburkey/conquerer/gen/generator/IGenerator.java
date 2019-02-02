package com.cjburkey.conquerer.gen.generator;

import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.world.Territory;
import com.cjburkey.conquerer.world.WorldHandler;
import java.util.Map;
import org.joml.Vector2fc;

/**
 * Created by CJ Burkey on 2019/01/11
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface IGenerator {

    Map<Vector2fc, Territory> generateTerritories(WorldHandler worldHandler);

    IGenerator setBounds(Rectf bounds);

    IGenerator setMinDistance(float minDistance);

    IGenerator setMaxTerritories(int maxTerritories);

    Rectf getBounds();

    float getMinDistance();

    int getMaxTerritories();

    boolean getUsesBounds();

}
