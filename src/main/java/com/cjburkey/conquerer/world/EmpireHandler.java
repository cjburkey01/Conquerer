package com.cjburkey.conquerer.world;

import com.cjburkey.conquerer.util.NameGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.joml.Random;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
@SuppressWarnings("WeakerAccess")
public final class EmpireHandler {

    private static final Vector2fc brightnessMinMax = new Vector2f(0.7f, 0.9f);

    private final Object2ObjectOpenHashMap<UUID, Empire> empires = new Object2ObjectOpenHashMap<>();

    public Empire create(String name, Vector3fc color) {
        Empire empire = new Empire(name, color);
        empires.put(empire.uuid, empire);
        return empire;
    }

    public Empire generate(Random random) {
        return create(new NameGenerator()
                        .setCanFirstLetterBeDigraph(false)          // These four properties are the default values, I set them here only to be explicit
                        .setShouldCapitalizeFirstCharacter(true)
                        .setMinLength(3)
                        .setMaxLength(7)
                        .generate(random),
                randomColor(random, brightnessMinMax.x(), brightnessMinMax.y()));
    }

    public Set<Empire> generate(Random random, int count) {
        ObjectOpenHashSet<Empire> empires = new ObjectOpenHashSet<>();
        for (int i = 0; i < count; i++) empires.add(generate(random));
        return Collections.unmodifiableSet(empires);
    }

    public int getEmpireCount() {
        return empires.size();
    }

    public UUID[] getEmpireUuids() {
        return empires.keySet().toArray(new UUID[0]);
    }

    public Empire[] getEmpires() {
        return empires.values().toArray(new Empire[0]);
    }

    public Empire getEmpire(UUID uuid) {
        return empires.getOrDefault(uuid, null);
    }

    public static final class Empire {

        public final UUID uuid = UUID.randomUUID();
        public final String name;
        public final Vector3f color = new Vector3f();
        private final ObjectOpenHashSet<Territory> territories = new ObjectOpenHashSet<>();

        private Empire(String name, Vector3fc color) {
            if (name == null) throw new NullPointerException("Empire name may not be null");
            if (color == null) throw new NullPointerException("Empire color may not be null");

            this.name = name;
            this.color.set(color);
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Empire empire = (Empire) o;
            return name.equals(empire.name)
                    && color.equals(empire.color);
        }

        public int hashCode() {
            return Objects.hash(name, color);
        }

        public void claimTerritory(Territory territory) {
            if (!containsTerritory(territory)) {
                territories.add(territory);
                territory.setCurrentOwner(this);
            }
        }

        public void unclaimTerritory(Territory territory) {
            if (containsTerritory(territory)) {
                territory.setCurrentOwner(null);
                territories.remove(territory);
            }
        }

        public int getTerritoriesCount() {
            return territories.size();
        }

        public Territory[] getTerritories() {
            return territories.toArray(new Territory[0]);
        }

        public boolean containsTerritory(Territory territory) {
            return territories.contains(territory);
        }

    }

}
