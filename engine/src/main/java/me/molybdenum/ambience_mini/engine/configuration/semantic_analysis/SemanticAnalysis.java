package me.molybdenum.ambience_mini.engine.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.configuration.errors.LoadError;
import me.molybdenum.ambience_mini.engine.configuration.errors.SemError;
import me.molybdenum.ambience_mini.engine.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.music.MusicPlayer;
import me.molybdenum.ambience_mini.engine.utils.Result;
import me.molybdenum.ambience_mini.engine.utils.Utils;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.config.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.playlist.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.schedule.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.configuration.pretty_printer.PrettyPrinter;
import me.molybdenum.ambience_mini.engine.core.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.core.providers.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public record SemanticAnalysis(MusicProvider musicProvider, BaseGameStateProvider gameStateProvider)
{
    public void validate(Config config, ArrayList<LoadError> errors) {
        Conf(config, new TypeEnv(), errors);
    }


    private void Conf(Config config, TypeEnv env, ArrayList<LoadError> errors) {
        if (config instanceof PlaylistDecl playlistDecl) {
            String name = playlistDecl.ident().value();
            if (!env.bind(name, new PlaylistT()))
                errors.add(new SemError(playlistDecl.ident().line(), "Multiple definition of playlist: " + name));

            PL(playlistDecl.playlist(), env, errors);
            Conf(playlistDecl.config(), env, errors);
        }
        else if (config instanceof ScheduleDecl scheduleDecl) {
            Shed(scheduleDecl.schedule(), env, errors);
        }
        else if (config == null) {
            return;
        }
        else
            throw new RuntimeException("Unhandled Conf-type: " + config.getClass().getCanonicalName());
    }

    private void PL(Playlist play, TypeEnv env, ArrayList<LoadError> errors) {
        if (play instanceof IdentP ident) {
            String name = ident.value();
            var binding = env.lookup(name);
            if (binding.isEmpty())
                errors.add(new SemError(ident.line(), "Use of undefined playlist: " + name));
            else {
                var type = binding.get();
                if (!(type instanceof PlaylistT))
                    errors.add(new SemError(ident.line(), "The ident '" + name + "' was expected to be a playlist but has type '" + PrettyPrinter.getTypeString(type) + "'"));
            }
        }
        else if (play instanceof Concat concat) {
            PL(concat.left(), env, errors);
            PL(concat.right(), env, errors);
        }
        else if (play instanceof Load load) {
            Result<String> musicPathRes = MusicProvider.validatePath(load.file().value());
            if (musicPathRes.isFailure())
                errors.add(new SemError(load.line(), musicPathRes.error));
            else {
                String musicPath = musicPathRes.value;
                if (!musicProvider.exists(musicPath))
                    errors.add(new SemError(load.line(), "Cannot find music file with name: '" + musicPath + "'"));
                if (!MusicPlayer.SUPPORTED_FILE_TYPES.contains(Utils.getFileExtension(musicPath)))
                    errors.add(new SemError(load.line(), "The file type of '" + musicPath + "' is unsupported. Ambience Mini currently only supports file types: " + String.join(", ", MusicPlayer.SUPPORTED_FILE_TYPES)));
            }
        }
        else if (play instanceof Nil || play == null) {
            return;
        }
        else
            throw new RuntimeException("Unhandled PL-type: " + play.getClass().getCanonicalName());
    }

    private void Shed(Schedule schedule, TypeEnv env, ArrayList<LoadError> errors) {
        if (schedule instanceof Play play) {
            PL(play.playlist(), env, errors);
        }
        else if (schedule instanceof Block block) {
            for (var child : block.body())
                Shed(child, env, errors);
        }
        else if (schedule instanceof When when) {
            Type type = Expr(when.condition(), env, errors);
            if (!(type instanceof BoolT) && type != null)
                errors.add(new SemError(when.line(), "The condition inside a 'when' must result in a boolean value. Got '" + PrettyPrinter.getTypeString(type) + "'"));
            if (when.body() instanceof Interrupt interrupt)
                errors.add(new SemError(interrupt.line(), "An interrupt can only be a child of a block (begin/end). Not a 'when'."));

            env.openScope();
            Shed(when.body(), env, errors);
            env.closeScope();
        }
        else if (schedule instanceof Interrupt interrupt) {
            if (env.inInterrupt())
                errors.add(new SemError(interrupt.line(), "An 'interrupt' may not occur inside the body of another 'interrupt'."));

            env.enterInterrupt();
            if (interrupt.body() instanceof When when)
                Shed(when, env, errors);
            else
                errors.add(new SemError(interrupt.line(), "The 'interrupt' keyword may only be followed by a 'when' clause."));
            env.exitInterrupt();
        }
        else if (schedule == null) {
            return;
        }
        else
            throw new RuntimeException("Unhandled Shed-type: " + schedule.getClass().getCanonicalName());
    }

    private Type Expr(Expr expr, TypeEnv env, ArrayList<LoadError> errors) {
        if (expr instanceof IdentE ident) {
            Optional<Type> type = env.lookup(ident.value());
            if (type.isEmpty()) {
                errors.add(new SemError(ident.line(), "Use of unbound ident '" + ident.value() + "'"));
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
                errors.add(new SemError(getEvent.eventName().line(), "Use of unknown event: @" + getEvent.eventName().value()));
            return new BoolT();
        }
        else if (expr instanceof GetProperty property) {
            Optional<Property> prop = gameStateProvider.tryGetProperty(property.propertyName().value());
            if (prop.isEmpty()) {
                errors.add(new SemError(property.propertyName().line(), "Use of unknown property: $" + property.propertyName().value()));
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
                            errors.add(new SemError(binOp.opLine(), "Arguments of '==' must be of same type. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    }
                    case APP_EQ -> {
                        if (!(typeLeft instanceof StringT) || !(typeRight instanceof StringT))
                            errors.add(new SemError(binOp.opLine(), "Arguments of '~~' must both be of type string. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    }
                    case AND -> {
                        if (!(typeLeft instanceof BoolT) || !(typeRight instanceof BoolT))
                            errors.add(new SemError(binOp.opLine(), "Arguments of '&&' must both be of type bool. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    }
                    case OR -> {
                        if (!(typeLeft instanceof BoolT) || !(typeRight instanceof BoolT))
                            errors.add(new SemError(binOp.opLine(), "Arguments of '||' must both be of type bool. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    }
                    case LT -> {
                        if (isNotNumber(typeLeft) || isNotNumber(typeRight))
                            errors.add(new SemError(binOp.opLine(), "Arguments of '<' must both be numbers. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    }
                }

            return new BoolT();
        }
        else if (expr instanceof QuantifierOp quanOp) {
            Type typeList = Expr(quanOp.list(), env, errors);
            if (typeList != null && !(typeList instanceof ListT))
                errors.add(new SemError(quanOp.inLine(), "The expression after 'in' in a list quantifier must be a list, but got '" + PrettyPrinter.getTypeString(typeList) + "'"));

            env.openScope();
            if (!env.bind(quanOp.identifier().value(), tryGetListElementType(typeList)))
                errors.add(new SemError(quanOp.identifier().line(), "Multiple definitions of the ident '" + quanOp.identifier() + "'"));

            Type typeCondition = Expr(quanOp.condition(), env, errors);
            if (!(typeCondition instanceof BoolT))
                errors.add(new SemError(quanOp.whereLine(), "The expression after 'where' in a list quantifier must be a boolean, but got '" + PrettyPrinter.getTypeString(typeCondition) + "'"));
            env.closeScope();

            return new BoolT();
        }
        else if (expr == null) {
            return null;
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
