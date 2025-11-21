package me.molybdenum.ambience_mini.engine.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.music.MusicPlayer;
import me.molybdenum.ambience_mini.engine.utils.Utils;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.config.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.playlist.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.schedule.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.configuration.pretty_printer.PrettyPrinter;
import me.molybdenum.ambience_mini.engine.core.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.core.providers.Property;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public record SemanticAnalysis(String musicDirectory, BaseGameStateProvider gameStateProvider) {
    public Stream<String> Conf(Config config, TypeEnv env) {
        ArrayList<String> errors = new ArrayList<>();

        if (config instanceof PlaylistDecl playlistDecl) {
            String name = playlistDecl.ident().value();

            if (!env.bind(name, new PlaylistT()))
                errors.add("Multiple definition of playlist: " + name);

            Stream<String> plErr = PL(playlistDecl.playlist(), env);
            Stream<String> confErr = Conf(playlistDecl.config(), env);

            return Stream.concat(errors.stream(), Stream.concat(plErr, confErr));
        }
        else if (config instanceof ScheduleDecl scheduleDecl)
            return Shed(scheduleDecl.schedule(), env);

        throw new RuntimeException("Unhandled Conf-type: " + config.getClass().getCanonicalName());
    }

    private Stream<String> PL(Playlist play, TypeEnv env) {
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
            else if (!MusicPlayer.SUPPORTED_FILE_TYPES.contains(ext))
                return Stream.of("Ambience mini only supports file types '" + String.join(", ", MusicPlayer.SUPPORTED_FILE_TYPES) + "' but got '" + ext + "'");
            return Stream.empty();
        }

        else if (play instanceof Nil)
            return Stream.empty();

        throw new RuntimeException("Unhandled PL-type: " + play.getClass().getCanonicalName());
    }

    private Stream<String> Shed(Schedule schedule, TypeEnv env) {
        if (schedule instanceof Play play)
            return PL(play.playlist(), env);
        else if (schedule instanceof Block block)
            return block.body().stream()
                    .map(sh -> Shed(sh, env))
                    .reduce(Stream.empty(), Stream::concat);
        else if (schedule instanceof When when) {
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
        else if (schedule instanceof Interrupt interrupt) {
            if (env.inInterrupt())
                return Stream.of("An 'interrupt' may not occur inside the body of another 'interrupt'.");

            env.enterInterrupt();
            Stream<String> res = interrupt.body() instanceof When
                    ? Shed(interrupt.body(), env)
                    : Stream.of("The 'interrupt' keyword may only be followed by a 'when' clause.");
            env.exitInterrupt();

            return res;
        }

        throw new RuntimeException("Unhandled Shed-type: " + schedule.getClass().getCanonicalName());
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
        else if (expr instanceof BoolLit)
            return new BoolT();
        else if (expr instanceof IntLit)
            return new IntT();
        else if (expr instanceof FloatLit)
            return new FloatT();
        else if (expr instanceof StringLit)
            return new StringT();
        else if (expr instanceof GetEvent getEvent) {
            if (gameStateProvider.tryGetEvent(getEvent.eventName().value()).isEmpty())
                errors.add("Use of unknown event: @" + getEvent.eventName().value());
            return new BoolT();
        }
        else if (expr instanceof GetProperty property) {
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
