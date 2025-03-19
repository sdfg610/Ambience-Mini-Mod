package gsto.ambience_mini.music.state;

import gsto.ambience_mini.music.loader.abstract_syntax.type.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;

public class Property {
    public static final ArrayList<Property> PROPERTIES = new ArrayList<>();


    public final String name;
    public final Type type;
    private final Supplier<Object> getter;

    public Property(String name, Type type, Supplier<Object> getter) {
        this.name = name;
        this.type = type;
        this.getter = getter;
    }

    public Object getValue() {
        return getter.get();
    }


    public static Property get(String name) {
        return PROPERTIES.stream()
                .filter(property -> property.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not get property: " + name));
    }

    public static Optional<Property> tryGet(String name) {
        return PROPERTIES.stream().filter(property -> property.name.equals(name)).findFirst();
    }

    public static Property register(String name, Type type, Supplier<Object> getter) {
        Property ev = new Property(name, type, getter);
        PROPERTIES.add(ev);
        return ev;
    }

    public static final Property DIMENSION = register("dimension", new StringT(), GameStateMonitor::getDimensionId);
    public static final Property BIOME = register("biome", new StringT(), GameStateMonitor::getBiomeId);
    public static final Property TIME = register("time", new IntT(), GameStateMonitor::getTime);

    public static final Property VEHICLE = register("vehicle", new StringT(), GameStateMonitor::getVehicle);

    public static final Property ELEVATION = register("elevation", new FloatT(), GameStateMonitor::getPlayerElevation);

    public static final Property BOSS = register("boss", new StringT(), GameStateMonitor::getBossId);

    //TODO: biometag, structure ?
}
