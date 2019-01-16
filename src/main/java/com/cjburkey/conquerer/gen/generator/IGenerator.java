package com.cjburkey.conquerer.gen.generator;

import com.artemis.Entity;
import java.util.Set;

/**
 * Created by CJ Burkey on 2019/01/11
 */
public interface IGenerator {
    
    Set<Entity> generateTerritories(int count, float minDistance);
    
}
