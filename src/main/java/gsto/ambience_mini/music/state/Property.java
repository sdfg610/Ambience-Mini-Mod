package gsto.ambience_mini.music.state;

import gsto.ambience_mini.music.loader.abstract_syntax.type.*;

import java.util.ArrayList;
import java.util.Optional;

public class Property {
    public static final ArrayList<Property> PROPERTIES = new ArrayList<>();


    public final String name;
    public final Type type;

    public Property(String name, Type type) {
        this.name = name;
        this.type = type;
    }


    public static Optional<Property> get(String name) {
        return PROPERTIES.stream().filter(property -> property.name.equals(name)).findFirst();
    }

    public static boolean exists(String name) {
        return PROPERTIES.stream().anyMatch(property -> property.name.equals(name));
    }

    public static Property register(String name, Type type) {
        Property ev = new Property(name, type);
        PROPERTIES.add(ev);
        return ev;
    }

    public static final Property DIMENSION = register("dimension", new StringT());
    public static final Property ELEVATION = register("elevation", new FloatT());
    public static final Property BOSS = register("boss", new StringT());
}
