package com.cjburkey.conquerer.gen.generator;

import com.cjburkey.conquerer.world.Territory;
import java.util.Set;

/**
 * Created by CJ Burkey on 2019/01/11
 */
public interface IGenerator {
    
    Set<Territory> generateTerritories(int count, float minDistance);
    
}
