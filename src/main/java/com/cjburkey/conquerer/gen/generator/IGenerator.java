package com.cjburkey.conquerer.gen.generator;

import com.cjburkey.conquerer.math.Rectf;
import com.cjburkey.conquerer.world.Territory;
import java.util.Set;

/**
 * Created by CJ Burkey on 2019/01/11
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface IGenerator {
    
    Set<Territory> generateTerritories();
    
    IGenerator setBounds(Rectf bounds);
    IGenerator setMinDistance(float minDistance);
    IGenerator setMaxTerritories(int maxTerritories);
    
    Rectf getBounds();
    float getMinDistance();
    int getMaxTerritories();
    
    boolean getUsesBounds();
    
}
