package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type;

import me.molybdenum.ambience_mini.engine.shared.configuration.pretty_printer.PrettyPrinter;
import me.molybdenum.ambience_mini.engine.shared.utils.results.StrResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public sealed abstract class Type permits AnyT, AreaT, BoolT, BossT, CombatantT, FloatT, IntT, ListT, LivingT, MapT, PlaylistT, StringT
{
    public boolean equalTo(Type other) {
        return this instanceof AnyT
                || other instanceof AnyT
                || (other != null && equalToInternal(other));
    }

    protected abstract boolean equalToInternal(@NotNull Type other);


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isBool() {
        return this instanceof BoolT || this instanceof AnyT;
    }

    public boolean isInt() {
        return this instanceof IntT || this instanceof AnyT;
    }

    public boolean isFloat() {
        return this instanceof FloatT || this instanceof AnyT;
    }

    public boolean isString() {
        return this instanceof StringT || this instanceof AnyT;
    }

    public boolean isPlaylist() {
        return this instanceof PlaylistT || this instanceof AnyT;
    }

    public boolean isList() {
        return this instanceof ListT || this instanceof AnyT;
    }


    @Override
    public String toString() {
        return PrettyPrinter.getTypeString(this);
    }


    public static StrResult<Type> tryOf(String name, List<Type> args) {
        return switch (name) {
            case "area" -> tryOf(name, AreaT.INSTANCE, args);
            case "bool" -> tryOf(name, BoolT.INSTANCE, args);
            case "boss" -> tryOf(name, BossT.INSTANCE, args);
            case "combatant" -> tryOf(name, CombatantT.INSTANCE, args);
            case "float" -> tryOf(name, FloatT.INSTANCE, args);
            case "int" -> tryOf(name, IntT.INSTANCE, args);
            case "living" -> tryOf(name, LivingT.INSTANCE, args);
            case "playlist" -> tryOf(name, PlaylistT.INSTANCE, args);
            case "string" -> tryOf(name, StringT.INSTANCE, args);

            case "list" -> tryOf(name, ListT::new, args);

            case "map" -> tryOf(name, MapT::new, args);

            default -> StrResult.fail("No such type with name '" + name + "'");
        };
    }

    private static StrResult<Type> tryOf(String name, Type type, List<Type> args) {
        if (!args.isEmpty())
            return StrResult.fail("The type '" + name + "' takes no type-arguments, but got '" + args.size() + "'");
        return StrResult.of(type);
    }

    private static StrResult<Type> tryOf(String name, Function<Type, Type> typeCtor, List<Type> args) {
        if (args.size() != 1)
            return StrResult.fail("The type '" + name + "' requires 1 type-argument, but got '" + args.size() + "'");
        return StrResult.of(typeCtor.apply(args.get(0)));
    }

    private static StrResult<Type> tryOf(String name, BiFunction<Type, Type, Type> typeCtor, List<Type> args) {
        if (args.size() != 2)
            return StrResult.fail("The type '" + name + "' requires 2 type-arguments, but got '" + args.size() + "'");
        return StrResult.of(typeCtor.apply(args.get(0), args.get(1)));
    }
}
