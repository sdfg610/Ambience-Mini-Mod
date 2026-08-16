package me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.schedule.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists.PlaylistGroup;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists.PlaylistInstance;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists.ServerPlaylists;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.kinds.AccessibleT;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.kinds.IndexableT;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.Message;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.SemError;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.AnyT;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.Warning;
import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.configuration.pretty_printer.PrettyPrinter;
import me.molybdenum.ambience_mini.engine.client.core.music.decoders.AmDecoder;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.client.core.providers.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SemanticAnalysis
{
    private final ArrayList<String> usedMusicFiles = new ArrayList<>();
    private final BaseMusicProvider musicProvider;
    private final Setup setup;
    private final BaseGameStateProvider gameStateProvider;


    public SemanticAnalysis(BaseMusicProvider musicProvider, Setup setup) {
        this.musicProvider = musicProvider;
        this.setup = setup;

        gameStateProvider = setup instanceof Setup.Client client ? client.gameStateProvider() : null;
    }

    public void validate(Config config, ArrayList<Message> messages) {
        usedMusicFiles.clear();

        validateConfig(config, new TypeEnv(), messages);

        int skipLen = musicProvider.getBasePath().length() + 1;  // +1 to include '/' at end of path
        List<String> unusedFiles = musicProvider.listLocalMusicFiles().stream()
                .filter(path -> AmDecoder.isSupportedFileType(Utils.getFileExtension(path.toString())) && usedMusicFiles.stream().noneMatch(path::endsWith))
                .map(path -> "-- " + path.toString().substring(skipLen))
                .toList();
        if (!unusedFiles.isEmpty())
            messages.add(new Warning(-1, "There are unused files in the music directory:\n" + String.join("\n", unusedFiles)));
    }


    private void validateConfig(Config config, TypeEnv env, ArrayList<Message> messages) {
        for (var decl : config.declarations()) {
            Type expectedType = decl.type();
            String name = decl.ident().value();
            if (!env.bind(name, expectedType, decl.ident().line()))
                messages.add(new SemError(decl.ident().line(), "Multiple definitions of: " + name));

            Type actualType = validateExpression(decl.value(), env, false, messages);
            if (actualType != null && expectedType != null && !expectedType.equalTo(actualType))
                messages.add(new SemError(decl.ident().line(), "Expected value of type '" + expectedType + "' for '" + name + "' but got value of type '" + actualType + "'"));
        }

        validateServerPlaylists(config.serverPlaylists(), env, messages, "");

        var schedule = config.schedule();
        if (setup instanceof Setup.DedicatedServer) {
            if (schedule != null)
                messages.add(new Warning(schedule.line(), "Music pack on dedicated server has a music schedule, but this is ignored on dedicated servers."));
        }
        else if (setup instanceof Setup.Client) {
            if (schedule == null)
                messages.add(new SemError(-1, "This music pack does not have a music schedule and cannot be loaded on the client."));
            else
                validateSchedule(schedule, env, messages);
        }

        makeUnusedVariableWarnings(env, messages);
    }

    private void validateServerPlaylists(ServerPlaylists playlists, TypeEnv env, ArrayList<Message> messages, String groupName) {
        if (playlists instanceof PlaylistInstance inst) {
            var type = validateExpression(inst.expr(), env, false, messages);
            if (type != null && !type.isPlaylist())
                messages.add(new SemError(inst.expr().line(), "Expected expression of type 'playlist' for server-playlist with name '" + groupName + "', but got '" + type + "'"));
        }
        else if (playlists instanceof PlaylistGroup group) {
            for (var pl : group.body().entrySet())
                validateServerPlaylists(pl.getValue(), env, messages, groupName.isEmpty() ? pl.getKey() : groupName + "." + pl.getKey());
        }
        else if (playlists == null)
            return;
        else
            throw new RuntimeException("Unhandled ServerPlaylists-type: " + playlists.getClass().getCanonicalName());
    }

    private void validateSchedule(Schedule schedule, TypeEnv env, ArrayList<Message> messages) {
        if (schedule instanceof Play play) {
            Type type = validateExpression(play.playlist(), env, true, messages);
            if (type != null && !type.isPlaylist())
                messages.add(new SemError(play.line(), "A play-command expected a playlist but got a value of type '" + type + "'"));

            if (play.getPriorityOrElse(0) < 0)
                messages.add(new SemError(play.priority().line(), "The priority of 'play' must be non-negative (>= 0)."));
        }
        else if (schedule instanceof Block block) {
            for (var child : block.body())
                validateSchedule(child, env, messages);
        }
        else if (schedule instanceof When when) {
            Type type = validateExpression(when.condition(), env, true, messages);
            if (type != null && !type.isBool())
                messages.add(new SemError(when.line(), "The condition inside a 'when' must result in a boolean value. Got '" + type + "'"));

            env.openScope();
            validateSchedule(when.body(), env, messages);
            env.closeScope();
        }
        else if (schedule instanceof Let let) {
            Type expectedType = let.type();
            Type actualType = validateExpression(let.value(), env, true, messages);
            if (expectedType != null && !expectedType.equalTo(actualType))
                messages.add(new SemError(let.line(), "A 'let' command expected a value of type '" + PrettyPrinter.getTypeString(expectedType) + "' but got '" + PrettyPrinter.getTypeString(actualType) + "'"));

            env.openScope();
            env.bind(let.ident().value(), expectedType == null ? actualType : expectedType, let.ident().line());
            validateSchedule(let.body(), env, messages);
            makeUnusedVariableWarnings(env, messages);
            env.closeScope();
        }
        else if (schedule instanceof Interrupt interrupt) {
            validateSchedule(interrupt.body(), env, messages);
        }
        else if (schedule instanceof Vanilla || schedule == null) {
            return;
        }
        else
            throw new RuntimeException("Unhandled Schedule-type: " + schedule.getClass().getCanonicalName());
    }

    private Type validateExpression(Expr expr, TypeEnv env, boolean allowGameState, ArrayList<Message> messages) {
        if (expr instanceof Ident ident) {
            Optional<TypeBinding> optBinding = env.lookup(ident.value());
            if (optBinding.isEmpty()) {
                messages.add(new SemError(ident.line(), "Use of unbound identifier '" + ident.value() + "'"));
                return null;
            } else {
                var binding = optBinding.get();
                binding.markIsUsed();
                return binding.type;
            }
        }
        else if (expr instanceof UndefinedLit)
            return new AnyT();
        else if (expr instanceof BoolLit)
            return BoolT.INSTANCE;
        else if (expr instanceof IntLit)
            return new IntT();
        else if (expr instanceof FloatLit)
            return new FloatT();
        else if (expr instanceof StringLit)
            return new StringT();
        else if (expr instanceof Playlist playlist) {
            for (var load : playlist.music())
                validateMusic(load, messages);
            return PlaylistT.INSTANCE;
        }
        else if (expr instanceof GetEvent event) {
            if (!allowGameState) {
                messages.add(new SemError(event.eventName().line(), "The use of events is not allowed at this point in the config"));
                return null;
            }

            if (gameStateProvider.tryGetEvent(event.eventName().value()).isEmpty())
                messages.add(new SemError(event.eventName().line(), "Use of unknown event: @" + event.eventName().value()));
            return BoolT.INSTANCE;
        }
        else if (expr instanceof GetProperty property) {
            if (!allowGameState) {
                messages.add(new SemError(property.propertyName().line(), "The use of properties is not allowed at this point in the config"));
                return null;
            }

            Optional<Property> prop = gameStateProvider.tryGetProperty(property.propertyName().value());
            if (prop.isEmpty()) {
                messages.add(new SemError(property.propertyName().line(), "Use of unknown property: $" + property.propertyName().value()));
                return null;
            }
            return prop.get().type;
        }
        else if (expr instanceof UnaryOp unOp) {
            Type type = validateExpression(unOp.expr(), env, allowGameState, messages);
            return switch (unOp.op()) {
                case NOT -> {
                    if (type != null && !type.isBool())
                        messages.add(new SemError(unOp.opLine(), "Argument of '!' must be of type bool. Got '" + type + "'"));
                    yield BoolT.INSTANCE;
                }
                case NEG -> {
                    if (isNumber(type))
                        yield type;
                    else if (isNotNumber(type))
                        messages.add(new SemError(unOp.opLine(), "Argument of unary '-' must be a number type. Got '" + type + "'"));
                    yield null;
                }
            };
        }
        else if (expr instanceof BinaryOp binOp) {
            Type typeLeft = validateExpression(binOp.left(), env, allowGameState, messages);
            Type typeRight = validateExpression(binOp.right(), env, allowGameState, messages);

            return switch (binOp.op()) {
                case EQ, NULL_CHECK -> {
                    if (isNotEqual(typeLeft, typeRight))
                        messages.add(new SemError(binOp.line(), "Arguments of '" + PrettyPrinter.getBinaryOpInfix(binOp.op()) + "' must be of same type. Got '" + typeLeft + "' and '" + typeRight + "'"));
                    yield BoolT.INSTANCE;
                }
                case APP_EQ -> {
                    if (isNotString(typeLeft) || isNotString(typeRight))
                        messages.add(new SemError(binOp.line(), "Arguments of '~~' must both be of type string. Got '" + typeLeft + "' and '" + typeRight + "'"));
                    yield BoolT.INSTANCE;
                }
                case MATCH -> {
                    if (isNotString(typeLeft) || isNotString(typeRight))
                        messages.add(new SemError(binOp.line(), "Arguments of '*~' must both be of type string. Got '" + typeLeft + "' and '" + typeRight + "'"));
                    yield BoolT.INSTANCE;
                }
                case AND -> {
                    if (isNotBool(typeLeft) || isNotBool(typeRight))
                        messages.add(new SemError(binOp.line(), "Arguments of '&&' must both be of type bool. Got '" + typeLeft + "' and '" + typeRight + "'"));
                    yield BoolT.INSTANCE;
                }
                case OR -> {
                    if (isNotBool(typeLeft) || isNotBool(typeRight))
                        messages.add(new SemError(binOp.line(), "Arguments of '||' must both be of type bool. Got '" + typeLeft + "' and '" + typeRight + "'"));
                    yield BoolT.INSTANCE;
                }
                case LT -> {
                    if (isNotNumber(typeLeft) || isNotNumber(typeRight))
                        messages.add(new SemError(binOp.line(), "Arguments of '<' and '>' must both be numbers. Got '" + typeLeft + "' and '" + typeRight + "'"));
                    yield BoolT.INSTANCE;
                }
                case LE -> {
                    if (isNotNumber(typeLeft) || isNotNumber(typeRight))
                        messages.add(new SemError(binOp.line(), "Arguments of '<=' and '>=' must both be numbers. Got '" + typeLeft + "' and '" + typeRight + "'"));
                    yield BoolT.INSTANCE;
                }
                case INDEXER -> {
                    if (typeLeft instanceof IndexableT indexer) {
                        if (isNotEqual(typeRight, indexer.indexerType()))
                            messages.add(new SemError(binOp.line(), "A value of type '" + typeLeft + "' requires an indexer of type '" + indexer.indexerType() + "'. Got '" + typeRight + "'"));
                        yield indexer.outputType();
                    }
                    else if (typeLeft != null)
                        messages.add(new SemError(binOp.line(), "A value of type '" + typeLeft + "' cannot be indexed using '[]'."));
                    yield null;
                }
                case ADD -> {
                    boolean leftInvalid = isNotNumber(typeLeft) && isNotString(typeLeft);
                    boolean rightInvalid = isNotNumber(typeRight) && isNotString(typeRight);
                    boolean notCompatible = typeLeft != null && typeRight != null
                            && !(isNumber(typeLeft) && isNumber(typeRight))
                            && !(typeLeft.isString() && typeRight.isString());

                    if (leftInvalid || rightInvalid || notCompatible)
                        messages.add(new SemError(binOp.line(), "Arguments of '+' must both be either numbers or strings. Got '" + typeLeft + "' and '" + typeRight + "'"));

                    if (isNumber(typeLeft) && isNumber(typeRight))
                        yield tryGetGetMaximalNumberType(typeLeft, typeRight);
                    else if (typeLeft.isString() && typeRight.isString())
                        yield StringT.INSTANCE;
                    else
                        yield null;
                }
                case SUB, MUL, DIV -> {
                    if (isNotNumber(typeLeft) || isNotNumber(typeRight))
                        messages.add(new SemError(binOp.line(), "Arguments of '" + PrettyPrinter.getBinaryOpInfix(binOp.op()) + "' must both be numbers. Got '" + typeLeft + "' and '" + typeRight + "'"));
                    yield tryGetGetMaximalNumberType(typeLeft, typeRight);
                }
                case APPEND -> {
                    if (isNotPlaylist(typeLeft) || isNotPlaylist(typeRight))
                        messages.add(new SemError(binOp.line(), "Arguments of '++' must both be playlists. Got '" + typeLeft + "' and '" + typeRight + "'."));
                    yield PlaylistT.INSTANCE;
                }
            };
        }
        else if (expr instanceof QuantifierOp quanOp) {
            Type typeList = validateExpression(quanOp.list(), env, allowGameState, messages);
            if (isNotList(typeList))
                messages.add(new SemError(quanOp.inLine(), "The expression after 'in' in a list quantifier must be a list, but got '" + typeList + "'"));

            env.openScope();
            if (!env.bind(quanOp.identifier().value(), tryGetListElementType(typeList), quanOp.identifier().line()))
                messages.add(new SemError(quanOp.identifier().line(), "Multiple definitions of the ident '" + quanOp.identifier() + "'"));

            Type typeCondition = validateExpression(quanOp.condition(), env, allowGameState, messages);
            if (typeCondition != null && !typeCondition.isBool())
                messages.add(new SemError(quanOp.whereLine(), "The expression after 'where' in a list quantifier must have type 'bool', but got '" + typeCondition + "'"));

            makeUnusedVariableWarnings(env, messages);
            env.closeScope();

            return BoolT.INSTANCE;
        }
        else if (expr instanceof Accessor accessor) {
            Type type = validateExpression(accessor.base(), env, allowGameState, messages);
            Ident field = accessor.field();

            if (type instanceof AccessibleT acc) {
                Type fieldT = acc.getField(field.value());
                if (fieldT != null)
                    return fieldT;
                messages.add(new SemError(field.line(), "A value of type '" + type + "' does not contain the field '" + field.value() + "'."));
            }
            else if (type != null)
                messages.add(new SemError(accessor.line(), "A value of type '" + type + "' does not have any fields to access."));
            return null;
        }
        else if (expr == null) {
            return null;
        }
        else
            throw new RuntimeException("Unhandled Expr-type: " + expr.getClass().getCanonicalName());
    }


    private void validateMusic(Playlist.Load load, ArrayList<Message> messages) {
        Result<String> musicPathRes = BaseMusicProvider.validatePath(load.file().value());
        if (musicPathRes.isFailure())
            messages.add(new SemError(load.line(), musicPathRes.error));
        else {
            String musicPath = musicPathRes.value;
            if (!musicProvider.existsLocally(musicPath))
                messages.add(new SemError(load.line(), "Cannot find music file with name: '" + musicPath + "'"));
            else
                usedMusicFiles.add(musicPath);

            if (!AmDecoder.isSupportedFileType(Utils.getFileExtension(musicPath)))
                messages.add(new SemError(load.line(), "The file type '" + musicPath + "' is unsupported. Ambience Mini currently only supports file types: " + String.join(", ", AmDecoder.getSupportedFileTypes())));

            for (var arg : load.args()) {
                Type type = validateExpression(arg.expr(), null, false, messages);
                switch (arg.ident().value()) {
                    case Music.ARG_GAIN -> {
                        if (type != null && !type.isFloat() && !type.isInt())
                            messages.add(new SemError(arg.ident().line(), "The music argument '" + Music.ARG_GAIN + "' expected a numerical value, but got a value of type '" + type + "'"));
                    }
                    case Music.ARG_LOOP -> {
                        if (type != null && !type.isBool())
                            messages.add(new SemError(arg.ident().line(), "The music argument '" + Music.ARG_LOOP + "' expected a boolean value, but got a value of type '" + type + "'"));
                    }
                    case Music.ARG_LOOPSTART,
                         Music.ARG_LOOPEND,
                         Music.ARG_LOOPLENGTH -> {
                        if (type != null && !type.isInt())
                            messages.add(new SemError(arg.ident().line(), "The music argument '" + arg.ident().value() + "' expected an integer value, but got a value of type '" + type + "'"));
                    }
                    default -> messages.add(new SemError(arg.ident().line(), "The music argument '" + arg.ident().value() + "' is invalid"));
                }
            }
        }
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

    private boolean isNotPlaylist(Type type) {
        return type != null && !type.isPlaylist();
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

    private void makeUnusedVariableWarnings(TypeEnv env, ArrayList<Message> messages) {
        for (var pair : env.getTopScopeUnused())
            messages.add(new Warning(pair.right(), "Unused variable '" + pair.left() + "'"));
    }

    private Type tryGetGetMaximalNumberType(Type left, Type right) {
        if (left == null || right == null)
            return null;

        if (left.isFloat() || right.isFloat())
            return FloatT.INSTANCE;
        if (left.isInt() && right.isInt())
            return IntT.INSTANCE;
        return null;
    }
}
