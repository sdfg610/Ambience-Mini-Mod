package me.molybdenum.ambience_mini.engine.client.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.config.Config;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.config.PlaylistDecl;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.config.ScheduleDecl;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.playlist.*;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.schedule.*;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.kinds.AccessibleT;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.kinds.IndexableT;
import me.molybdenum.ambience_mini.engine.client.configuration.errors.LoadError;
import me.molybdenum.ambience_mini.engine.client.configuration.errors.SemError;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.AnyT;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.client.configuration.pretty_printer.PrettyPrinter;
import me.molybdenum.ambience_mini.engine.client.music.MusicPlayer;
import me.molybdenum.ambience_mini.engine.client.music.decoders.AmDecoder;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.client.core.providers.Property;

import java.util.ArrayList;
import java.util.Optional;

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
            if (!musicPathRes.isSuccess())
                errors.add(new SemError(load.line(), musicPathRes.error));
            else {
                String musicPath = musicPathRes.value;
                if (!musicProvider.exists(musicPath))
                    errors.add(new SemError(load.line(), "Cannot find music file with name: '" + musicPath + "'"));
                if (!AmDecoder.getSupportedFileTypes().contains(Utils.getFileExtension(musicPath)))
                    errors.add(new SemError(load.line(), "The file type of '" + musicPath + "' is unsupported. Ambience Mini currently only supports file types: " + String.join(", ", AmDecoder.getSupportedFileTypes())));
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
            if (play.getPriorityOrElse(0) < 0)
                errors.add(new SemError(play.priority().line(), "The priority must be non-negative (>= 0)."));
        }
        else if (schedule instanceof Block block) {
            for (var child : block.body())
                Shed(child, env, errors);
        }
        else if (schedule instanceof When when) {
            Type type = Expr(when.condition(), env, errors);
            if (!(type instanceof BoolT) && type != null)
                errors.add(new SemError(when.line(), "The condition inside a 'when' must result in a boolean value. Got '" + PrettyPrinter.getTypeString(type) + "'"));

            env.openScope();
            Shed(when.body(), env, errors);
            env.closeScope();
        }
        else if (schedule instanceof Let let) {
            Type expectedType = let.type();
            Type actualType = Expr(let.value(), env, errors);
            if (expectedType != null && !expectedType.equalTo(actualType))
                errors.add(new SemError(let.line(), "A 'let' command expected a value of type '" + PrettyPrinter.getTypeString(expectedType) + "' but got '" + PrettyPrinter.getTypeString(actualType) + "'"));

            env.openScope();
            env.bind(let.ident().value(), expectedType == null ? actualType : expectedType);
            Shed(let.body(), env, errors);
            env.closeScope();
        }
        else if (schedule instanceof Interrupt interrupt) {
            Shed(interrupt.body(), env, errors);
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
        else if (expr instanceof UndefinedLit)
            return new AnyT();
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
        else if (expr instanceof UnaryOp unOp) {
            Type type = Expr(unOp.expr(), env, errors);
            return switch (unOp.op()) {
                case NOT -> {
                    if (type != null && !(type instanceof BoolT))
                        errors.add(new SemError(unOp.opLine(), "Argument of '!' must be of type bool. Got '" + PrettyPrinter.getTypeString(type) + "'"));
                    yield new BoolT();
                }
                case NEG -> {
                    if (isNumber(type))
                        yield type;
                    else if (isNotNumber(type))
                        errors.add(new SemError(unOp.opLine(), "Argument of unary '-' must be a number type. Got '" + PrettyPrinter.getTypeString(type) + "'"));
                    yield null;
                }
            };
        }
        else if (expr instanceof BinaryOp binOp) {
            Type typeLeft = Expr(binOp.left(), env, errors);
            Type typeRight = Expr(binOp.right(), env, errors);

            return switch (binOp.op()) {
                case EQ -> {
                    if (isNotEqual(typeLeft, typeRight))
                        errors.add(new SemError(binOp.line(), "Arguments of '==' must be of same type. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    yield new BoolT();
                }
                case APP_EQ -> {
                    if (isNotString(typeLeft) || isNotString(typeRight))
                        errors.add(new SemError(binOp.line(), "Arguments of '~~' must both be of type string. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    yield new BoolT();
                }
                case MATCH -> {
                    if (isNotString(typeLeft) || isNotString(typeRight))
                        errors.add(new SemError(binOp.line(), "Arguments of '*~' must both be of type string. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    yield new BoolT();
                }
                case AND -> {
                    if (isNotBool(typeLeft) || isNotBool(typeRight))
                        errors.add(new SemError(binOp.line(), "Arguments of '&&' must both be of type bool. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    yield new BoolT();
                }
                case OR -> {
                    if (isNotBool(typeLeft) || isNotBool(typeRight))
                        errors.add(new SemError(binOp.line(), "Arguments of '||' must both be of type bool. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    yield new BoolT();
                }
                case LT -> {
                    if (isNotNumber(typeLeft) || isNotNumber(typeRight))
                        errors.add(new SemError(binOp.line(), "Arguments of '<' and '>' must both be numbers. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    yield new BoolT();
                }
                case LE -> {
                    if (isNotNumber(typeLeft) || isNotNumber(typeRight))
                        errors.add(new SemError(binOp.line(), "Arguments of '<=' and '>=' must both be numbers. Got '" + PrettyPrinter.getTypeString(typeLeft) + "' and '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                    yield new BoolT();
                }
                case INDEXER -> {
                    if (typeLeft instanceof IndexableT indexer) {
                        if (isNotEqual(typeRight, indexer.indexerType()))
                            errors.add(new SemError(binOp.line(), "A value of type '" + PrettyPrinter.getTypeString(typeLeft) + "' requires an indexer of type '" + PrettyPrinter.getTypeString(indexer.indexerType()) + "'. Got '" + PrettyPrinter.getTypeString(typeRight) + "'"));
                        yield indexer.outputType();
                    }
                    else if (typeLeft != null)
                        errors.add(new SemError(binOp.line(), "A value of type '" + PrettyPrinter.getTypeString(typeLeft) + "' cannot be indexed using '[]'."));
                    yield null;
                }
            };
        }
        else if (expr instanceof QuantifierOp quanOp) {
            Type typeList = Expr(quanOp.list(), env, errors);
            if (isNotList(typeList))
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
        else if (expr instanceof Accessor accessor) {
            Type type = Expr(accessor.base(), env, errors);
            IdentE field = accessor.field();

            if (type instanceof AccessibleT acc) {
                Type fieldT = acc.getField(field.value());
                if (fieldT != null)
                    return fieldT;
                errors.add(new SemError(field.line(), "A value of type '" + PrettyPrinter.getTypeString(type) + "' does not contain the field '" + field.value() + "'."));
            }
            else if (type != null)
                errors.add(new SemError(accessor.line(), "A value of type '" + PrettyPrinter.getTypeString(type) + "' does not have any fields to access."));
            return null;
        }
        else if (expr == null) {
            return null;
        }
        else
            throw new RuntimeException("Unhandled Expr-type: " + expr.getClass().getCanonicalName());
    }


    private boolean isNotEqual(Type t1, Type t2) {
        return t1 != null && t2 != null && !t1.equalTo(t2);
    }

    private boolean isNotBool(Type type) {
        return type != null && !type.isBool();
    }

    private boolean isNotString(Type type) {
        return type != null && !type.isString();
    }

    private boolean isNotList(Type type) {
        return type != null && !type.isList();
    }


    private boolean isNumber(Type type) {
        return type instanceof IntT || type instanceof FloatT;
    }

    private boolean isNotNumber(Type type) {
        return type != null && !isNumber(type);
    }


    private Type tryGetListElementType(Type type) {
        if (type instanceof ListT list)
            return list.elementType;
        return null;
    }
}
