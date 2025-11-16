package me.molybdenum.ambience_mini.engine.loader.semantic_analysis;

import me.molybdenum.ambience_mini.engine.utils.Utils;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.conf.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.play.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.shed.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.loader.pretty_printer.PrettyPrinter;
import me.molybdenum.ambience_mini.engine.state.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.state.providers.Property;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public record SemanticAnalysis(String musicDirectory, BaseGameStateProvider gameStateProvider) {
    public Stream<String> Conf(Conf conf, TypeEnv env) {
        ArrayList<String> errors = new ArrayList<>();

        if (conf instanceof Playlist playlist) {
            String name = playlist.ident().value();

            if (!env.bind(name, new PlaylistT()))
                errors.add("Multiple definition of playlist: " + name);

            Stream<String> plErr = PL(playlist.playlist(), env);
            Stream<String> confErr = Conf(playlist.conf(), env);

            return Stream.concat(errors.stream(), Stream.concat(plErr, confErr));
        }
        else if (conf instanceof Schedule schedule)
            return Shed(schedule.schedule(), env);

        throw new RuntimeException("Unhandled Conf-type: " + conf.getClass().getCanonicalName());
    }

    private Stream<String> PL(PL play, TypeEnv env) {
        if (play instanceof IdentP ident) {
            String name = ident.value();
            var binding = env.lookup(name);
            if (binding.isEmpty())
                return Stream.of("Use of undefined playlist: " + name);

            var type = binding.get();
            return type instanceof PlaylistT
                    ? Stream.empty()
                    : Stream.of("The ident '" + name + "' was expected to be a playlist but has type '" + PrettyPrinter.getTypeString(type) + "'");
        }
        else if (play instanceof Concat concat)
            return Stream.concat(
                    PL(concat.left(), env),
                    PL(concat.right(), env)
            );
        else if (play instanceof Load load) {
            Path musicPath = Path.of(musicDirectory, load.file().value());
            String ext = Utils.getFileExtension(musicPath.getFileName().toString());

            if (!Files.exists(musicPath))
                return Stream.of("Cannot find music-file with name: '" + musicPath + "'");
            else if (!Utils.SUPPORTED_FILE_TYPES.contains(ext))
                return Stream.of("Ambience mini only supports file types '" + String.join(", ", Utils.SUPPORTED_FILE_TYPES) + "' but got '" + ext + "'");
            return Stream.empty();
        }

        else if (play instanceof Nil)
            return Stream.empty();

        throw new RuntimeException("Unhandled PL-type: " + play.getClass().getCanonicalName());
    }

    private Stream<String> Shed(Shed shed, TypeEnv env) {
        if (shed instanceof Play play)
            return PL(play.playlist(), env);
        else if (shed instanceof Block block)
            return block.body().stream()
                    .map(sh -> Shed(sh, env))
                    .reduce(Stream.empty(), Stream::concat);
        else if (shed instanceof When when) {
            ArrayList<String> errors = new ArrayList<>();

            Type type = Expr(when.condition(), env, errors);
            if (!(type instanceof BoolT))
                errors.add("The condition inside a 'when' must result in a boolean value. Got '" + PrettyPrinter.getTypeString(type) + "'");
            if (when.body() instanceof Interrupt)
                errors.add("An interrupt can only be a child of a block (begin/end). Not a 'when'.");

            env.openScope();
            var errorsFromBody = Shed(when.body(), env);
            env.closeScope();

            return Stream.concat(errors.stream(), errorsFromBody);
        }
        else if (shed instanceof Interrupt interrupt) {
            if (env.inInterrupt())
                return Stream.of("An 'interrupt' may not occur inside the body of another 'interrupt'.");

            env.enterInterrupt();
            Stream<String> res = interrupt.shed() instanceof When
                    ? Shed(interrupt.shed(), env)
                    : Stream.of("The 'interrupt' keyword may only be followed by a 'when' clause.");
            env.exitInterrupt();

            return res;
        }

        throw new RuntimeException("Unhandled Shed-type: " + shed.getClass().getCanonicalName());
    }

    public Type Expr(Expr expr, TypeEnv env, ArrayList<String> errors) {
        if (expr instanceof IdentE ident) {
            Optional<Type> type = env.lookup(ident.value());
            if (type.isEmpty()) {
                errors.add("Use of unbound ident '" + ident.value() + "'");
                return null;
            }
            return type.get();
        }
        else if (expr instanceof BoolV)
            return new BoolT();
        else if (expr instanceof IntV)
            return new IntT();
        else if (expr instanceof FloatV)
            return new FloatT();
        else if (expr instanceof StringV)
            return new StringT();
        else if (expr instanceof Ev ev) {
            if (gameStateProvider.tryGetEvent(ev.eventName().value()).isEmpty())
                errors.add("Use of unknown event: @" + ev.eventName().value());
            return new BoolT();
        }
        else if (expr instanceof Get property) {
            Optional<Property> prop = gameStateProvider.tryGetProperty(property.propertyName().value());
            if (prop.isEmpty()) {
                errors.add("Use of unknown property: $" + property.propertyName().value());
                return null;
            }
            return prop.get().type;
        }
        else if (expr instanceof BinaryOp binOp) {
            Type typeLeft = Expr(binOp.left(), env, errors);
            Type typeRight = Expr(binOp.right(), env, errors);

            if (typeLeft != null && typeRight != null) // Only check if both types are known, else we get useless errors.
                switch (binOp.op()) {
                    case EQ -> {
                        if (!Objects.equals(typeLeft, typeRight))
                            errors.add("Arguments of '==' must be of same type. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'");
                    }
                    case APP_EQ -> {
                        if (!(typeLeft instanceof StringT) || !(typeRight instanceof StringT))
                            errors.add("Arguments of '~~' must both be of type string. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'");
                    }
                    case AND -> {
                        if (!(typeLeft instanceof BoolT) || !(typeRight instanceof BoolT))
                            errors.add("Arguments of '&&' must both be of type bool. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'");
                    }
                    case OR -> {
                        if (!(typeLeft instanceof BoolT) || !(typeRight instanceof BoolT))
                            errors.add("Arguments of '||' must both be of type bool. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'");
                    }
                    case LT -> {
                        if (isNotNumber(typeLeft) || isNotNumber(typeRight))
                            errors.add("Arguments of '<' must both be numbers. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'");
                    }
                }

            return new BoolT();
        }
        else if (expr instanceof QuantifierOp quanOp) {
            Type typeList = Expr(quanOp.list(), env, errors);
            if (typeList != null && !(typeList instanceof ListT))
                errors.add("The expression after 'in' in a list quantifier must be a list, but got '" + PrettyPrinter.getTypeString(typeList) + "'");

            env.openScope();
            if (!env.bind(quanOp.identifier(), tryGetListElementType(typeList)))
                errors.add("Multiple definitions of the ident '" + quanOp.identifier() + "'");

            Type typeCondition = Expr(quanOp.condition(), env, errors);
            if (!(typeCondition instanceof BoolT))
                errors.add("The expression after 'where' in a list quantifier must be a boolean, but got '" + PrettyPrinter.getTypeString(typeCondition) + "'");
            env.closeScope();

            return new BoolT();
        }
        else
            throw new RuntimeException("Unhandled Expr-type: " + expr.getClass().getCanonicalName());
    }

    private boolean isNotNumber(Type type) {
        return !(type instanceof IntT) && !(type instanceof FloatT);
    }

    private Type tryGetListElementType(Type type) {
        if (type instanceof ListT list)
            return list.elementType();
        return null;
    }
}
