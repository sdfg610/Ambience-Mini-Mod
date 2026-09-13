package me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.shared.configuration.LoadResult;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.misc.Arg;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.misc.ArgList;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.schedule.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists.PlaylistGroup;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists.PlaylistInstance;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists.ServerPlaylists;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.kinds.AccessibleT;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.kinds.IndexableT;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.BaseInterpreter;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.VariableEnv;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.Message;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.SemError;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.AnyT;
import me.molybdenum.ambience_mini.engine.shared.configuration.messages.Warning;
import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.shared.configuration.pretty_printer.PrettyPrinter;
import me.molybdenum.ambience_mini.engine.client.core.music.decoders.AmDecoder;
import me.molybdenum.ambience_mini.engine.shared.utils.results.StrResult;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import me.molybdenum.ambience_mini.engine.client.core.providers.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SemanticAnalysis extends BaseInterpreter
{
    private final Setup setup;
    private final BaseMusicProvider musicProvider;

    private final ArrayList<String> usedMusicFiles = new ArrayList<>();
    private int nestedInterrupts = 0;


    private SemanticAnalysis(Setup setup, BaseMusicProvider musicProvider) {
        super(setup instanceof Setup.Client client ? client.gameStateProvider() : null);

        this.musicProvider = musicProvider;
        this.setup = setup;
    }


    public static LoadResult<Config> validateAndOptimize(
            Config config,
            BaseMusicProvider musicProvider,
            Setup setup,
            ArrayList<Message> messages)
    {
        var sem = new SemanticAnalysis(setup, musicProvider);
        var newConfig = sem.validateAndOptimizeConfig(config, messages);
        return newConfig == null || messages.stream().anyMatch(Message::isError)
                ? LoadResult.fail(messages)
                : LoadResult.of(newConfig, messages);
    }


    private Config validateAndOptimizeConfig(Config config, ArrayList<Message> messages) {
        var typEnv = new TypeEnv();
        var varEnv = new VariableEnv();

        validateDeclarations(config, typEnv, varEnv, messages);
        var newServerPlaylists = validateAndOptimizeServerPlaylists(config.serverPlaylists(), typEnv, varEnv, new MessageListBuilder(messages), "");
        var newSchedule = validateAndOptimizeScheduleIfNecessary(config, typEnv, varEnv, new MessageListBuilder(messages));

        makeUnusedVariableWarnings(typEnv, new MessageListBuilder(messages));
        makeUnusedMusicWarnings(messages);

        // Empty list for declarations since these are constant and are written into the rest of the config
        return messages.stream().anyMatch(Message::isError) ? null
                : new Config(List.of(), newServerPlaylists, newSchedule);
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Declarations
    private void validateDeclarations(Config config, TypeEnv typEnv, VariableEnv varEnv, ArrayList<Message> messages) {
        for (var decl : config.declarations()) {
            Type expectedType = decl.type();
            String name = decl.ident().value();
            Expr expr = decl.value();

            var eoValue = validateAndOptimizeExpression(expr, typEnv, varEnv, true, new MessageListBuilder(messages));

            var oBinding = typEnv.bind(name, expectedType, decl.ident().line());
            if (oBinding.isEmpty())
                messages.add(new SemError(decl.ident().line(), "Multiple definitions of: " + name));
            else {
                var binding = oBinding.get();
                binding.markAsConst(); // Global declarations are always constant

                eoValue.ifTypeDefined(actualType -> {
                    if (expectedType != null) {
                        if (!expectedType.equalTo(actualType))
                            messages.add(new SemError(decl.ident().line(), "Expected value of type '" + expectedType + "' for '" + name + "' but got value of type '" + actualType + "'"));
                        else if (eoValue.isWellDefined())
                            binding.markAsWellDefined();
                    }
                });

                if (eoValue.isNotConstant())
                    messages.add(new SemError(decl.ident().line(), "The value of the global identifier '" + name + "' must be a constant"));
                else if (eoValue.isConstant() && binding.getIsWellDefined())
                    varEnv.bind(name, eoValue.getValue());
            }
        }
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Server playlists
    private ServerPlaylists validateAndOptimizeServerPlaylists(
            ServerPlaylists playlists,
            TypeEnv typEnv,
            VariableEnv varEnv,
            MessageListBuilder messages,
            String groupName
    ) {
        if (playlists instanceof PlaylistInstance inst) {
            var expr = inst.expr();
            var exprLine = inst.expr().line();

            var eoValue = validateAndOptimizeExpression(expr, typEnv, varEnv, true, messages.detatch());

            eoValue.ifTypeDefined(actualType -> {
                if (!actualType.isPlaylist())
                    messages.add(new SemError(exprLine, "Expected expression of type 'playlist' for server-playlist with name '" + groupName + "', but got '" + actualType + "'"));
            });

            if (eoValue.isNotConstant())
                messages.add(new SemError(exprLine, "The value of the server-playlist '" + groupName + "' must be constant"));

            return eoValue.ifWellDefinedOrNull(newExpr -> eoValue.isConstant()
                    ? new PlaylistInstance(newExpr)
                    : null
            );
        }
        else if (playlists instanceof PlaylistGroup group) {
            var body = new HashMap<String, ServerPlaylists>();
            boolean hasErrors = false;
            for (var pl : group.body().entrySet()) {
                var newGroupName = groupName.isEmpty() ? pl.getKey() : groupName + "." + pl.getKey();
                var newPl = validateAndOptimizeServerPlaylists(pl.getValue(), typEnv, varEnv, messages, newGroupName);
                if (newPl == null)
                    hasErrors = true;
                else if (!hasErrors)
                    body.put(pl.getKey(), newPl);
            }
            return hasErrors ? null : new PlaylistGroup(body);
        }
        else if (playlists == null)
            return null;
        else
            throw new RuntimeException("Unhandled ServerPlaylists-type: " + playlists.getClass().getCanonicalName());
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Schedules
    private Schedule validateAndOptimizeScheduleIfNecessary(Config config, TypeEnv typEnv, VariableEnv varEnv, MessageListBuilder messages) {
        var schedule = config.schedule();
        if (setup instanceof Setup.DedicatedServer) {
            if (schedule != null)
                messages.add(new Warning(schedule.line(), "Music pack on dedicated server has a music schedule, but this is ignored on dedicated servers."));
        }
        else if (setup instanceof Setup.Client) {
            if (schedule == null)
                messages.add(new SemError(-1, "This music pack does not have a music schedule and cannot be loaded on the client."));
            else
                return validateAndOptimizeSchedule(schedule, typEnv, varEnv, messages);
        }

        return null;
    }

    private Schedule validateAndOptimizeSchedule(Schedule schedule, TypeEnv typEnv, VariableEnv varEnv, MessageListBuilder messages) {
        if (schedule instanceof Play play) {
            var eoResult = validateAndOptimizeExpression(play.playlist(), typEnv, varEnv, false, messages.detatch());

            eoResult.ifTypeDefined(type -> {
                if (type != null && !type.isPlaylist())
                    messages.add(new SemError(play.line(), "A play-command expected a playlist but got a value of type '" + type + "'"));
            });

            if (play.getPriorityOrElse(0) < 0)
                messages.add(new SemError(play.priority().line(), "The priority of 'play' must be non-negative (>= 0)."));

            return eoResult.ifWellDefinedOrNull(
                    newPlaylist -> messages.hasErrorOnNode() ? null : play.withPlaylistAndPriority(newPlaylist, play.computePriorityIfAbsent(nestedInterrupts))
            );
        }
        else if (schedule instanceof Block block) {
            var newBody = block.body().stream()
                    .map(child -> validateAndOptimizeSchedule(child, typEnv, varEnv, messages.detatch()))
                    .toList();

            return newBody.contains(null) ? null : block.withBody(newBody);
        }
        else if (schedule instanceof When when) {
            var eoResult = validateAndOptimizeExpression(when.condition(), typEnv, varEnv, false, messages.detatch());

            eoResult.ifTypeDefined(type -> {
                if (!type.isBool())
                    messages.add(new SemError(when.line(), "The condition inside a 'when' must result in a boolean value. Got '" + type + "'"));
            });

            var newBody = validateAndOptimizeSchedule(when.body(), typEnv, varEnv, messages.detatch());
            return newBody == null || messages.hasErrorOnNode() ? null
                    : new When(when.condition(), newBody, when.line());
        }
        else if (schedule instanceof Let let) {
            var eoResult = validateAndOptimizeExpression(let.value(), typEnv, varEnv, false, messages.detatch());

            Type expectedType = let.type();
            Type actualType = eoResult.type();
            if (expectedType != null && actualType != null && !expectedType.equalTo(actualType))
                messages.add(new SemError(let.line(), "A 'let' command expected a value of type '" + PrettyPrinter.getTypeString(expectedType) + "' but got '" + PrettyPrinter.getTypeString(actualType) + "'"));

            typEnv.openScope();
            var binding = typEnv.bind(let.ident().value(), expectedType == null ? actualType : expectedType, let.ident().line());

            var newVarEnv = varEnv.enterScope();
            if (binding.isPresent() && binding.get().isStaticallyEvaluable())
                newVarEnv.bind(let.ident().value(), eoResult.getValue());
            var newBody = validateAndOptimizeSchedule(let.body(), typEnv, newVarEnv, messages.detatch());

            makeUnusedVariableWarnings(typEnv, messages);
            typEnv.closeScope();


            return newBody == null || !eoResult.isWellDefined() || messages.hasErrorOnNode() ? null
                    : let.withValueAndBody(eoResult.expr(), newBody);
        }
        else if (schedule instanceof Interrupt interrupt) {
            ++nestedInterrupts;
            var newBody = validateAndOptimizeSchedule(interrupt.body(), typEnv, varEnv, messages.detatch());
            --nestedInterrupts;

            return newBody;
        }
        else if (schedule instanceof Vanilla || schedule == null) {
            return schedule;
        }
        else
            throw new RuntimeException("Unhandled Schedule-type: " + schedule.getClass().getCanonicalName());
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Expressions
    private ExprOptimizeResult validateAndOptimizeExpression(Expr expr, TypeEnv typEnv, VariableEnv varEnv, boolean requireConst, MessageListBuilder messages) {
        if (expr instanceof Ident ident) {
            Expr newExpr = null;
            Type type = null;
            Boolean isConst = null;

            Optional<TypeBinding> optBinding = typEnv.lookup(ident.value());
            if (optBinding.isEmpty())
                messages.add(new SemError(ident.line(), "Use of unbound identifier '" + ident.value() + "'"));
            else {
                var binding = optBinding.get();
                binding.markAsUsed();
                type = binding.type;

                isConst = binding.getIsConst();
                if (isConst)
                    newExpr = binding.getIsWellDefined() ? asExpr(evalExpr(expr, varEnv)) : null;
                else if (requireConst)
                    messages.add(new SemError(ident.line(), "Use of non-constant identifier '" + ident.value() + "' in a constant context"));
                else
                    newExpr = expr;
            }

            return mkRes(newExpr, type, isConst);
        }
        else if (expr instanceof UndefinedLit)
            return mkRes(evalExpr(expr, null), AnyT.INSTANCE);
        else if (expr instanceof BoolLit)
            return mkRes(evalExpr(expr, null), BoolT.INSTANCE);
        else if (expr instanceof IntLit)
            return mkRes(evalExpr(expr, null), IntT.INSTANCE);
        else if (expr instanceof FloatLit)
            return mkRes(evalExpr(expr, null), FloatT.INSTANCE);
        else if (expr instanceof StringLit)
            return mkRes(evalExpr(expr, null), StringT.INSTANCE);
        else if (expr instanceof PlaylistLit playlistLit) {
            var loadList = new ArrayList<PlaylistLit.Load>();
            boolean hasError = false;
            for (var load : playlistLit.music()) {
                var newLoad = validateAndOptimizeMusic(load, typEnv, varEnv, messages.detatch());
                if (newLoad == null)
                    hasError = true;
                else if (!hasError)
                    loadList.add(newLoad);
            }

            return mkRes(
                    hasError ? null : evalExpr(playlistLit.withMusic(loadList), varEnv),
                    PlaylistT.INSTANCE
            );
        }
        else if (expr instanceof GetEvent event) {
            boolean wellDefined = true;
            if (gameStateProvider.tryGetEvent(event.eventName().value()).isEmpty()) {
                messages.add(new SemError(event.eventName().line(), "Use of unknown event: @" + event.eventName().value()));
                wellDefined = false;
            }

            if (requireConst)
                messages.add(new SemError(expr.line(), "Use of (non-constant) event '@" + event.eventName().value() + "' in a constant context"));

            return mkRes(wellDefined ? expr : null, BoolT.INSTANCE, false);
        }
        else if (expr instanceof GetProperty property) {
            Optional<Property> prop = gameStateProvider.tryGetProperty(property.propertyName().value());

            boolean wellDefined = prop.isPresent();
            Type type = null;
            if (!wellDefined)
                messages.add(new SemError(property.propertyName().line(), "Use of unknown property: $" + property.propertyName().value()));
            else
                type = prop.get().type;

            if (requireConst)
                messages.add(new SemError(expr.line(), "Use of (non-constant) property '$" + property.propertyName().value() + "' in a constant context"));

            return mkRes(wellDefined ? expr : null, type, false);
        }
        else if (expr instanceof UnaryOp unOp) {
            var eoResult = validateAndOptimizeExpression(unOp.expr(), typEnv, varEnv, requireConst, messages.detatch());

            var type = switch (unOp.op()) {
                case NOT -> {
                    eoResult.ifTypeDefined(actualType -> {
                        if (!actualType.isBool())
                            messages.add(new SemError(unOp.opLine(), "Argument of '!' must be of type bool. Got '" + actualType + "'"));
                    });
                    yield BoolT.INSTANCE;
                }
                case NEG -> eoResult.ifTypeDefinedOrNull(actualType -> {
                    if (isNumber(actualType))
                        return actualType;
                    messages.add(new SemError(unOp.opLine(), "Argument of unary '-' must be a number type. Got '" + actualType + "'"));
                    return null;
                });
            };

            return tryOptimizeExprOrNull(eoResult, unOp::withExpr, varEnv, type, messages.hasErrorOnNode());
        }
        else if (expr instanceof BinaryOp binOp) {
            var eoLeft = validateAndOptimizeExpression(binOp.left(), typEnv, varEnv, requireConst, messages.detatch());
            var eoRight = validateAndOptimizeExpression(binOp.right(), typEnv, varEnv, requireConst, messages.detatch());

            Type typeLeft = eoLeft.type();
            Type typeRight = eoRight.type();

            Type resType = switch (binOp.op()) {
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

            return tryOptimizeExprOrNull(eoLeft, eoRight, binOp::withExprs, varEnv, resType, messages.hasErrorOnNode());
        }
        else if (expr instanceof QuantifierOp quanOp) {
            var eoList = validateAndOptimizeExpression(quanOp.list(), typEnv, varEnv, requireConst, messages.detatch());

            Type typeList = eoList.type();
            if (isNotList(typeList))
                messages.add(new SemError(quanOp.inLine(), "The expression after 'in' in a list quantifier must be a list, but got '" + typeList + "'"));
            Type elemType = tryGetListElementType(typeList);

            typEnv.openScope();
            typEnv.bind(quanOp.identifier().value(), elemType, quanOp.identifier().line()); // Bind in new scope cannot fail

            var eoCondition = validateAndOptimizeExpression(quanOp.condition(), typEnv, varEnv, requireConst, messages.detatch());
            eoCondition.ifTypeDefined(typeCondition -> {
                if (!typeCondition.isBool())
                    messages.add(new SemError(quanOp.whereLine(), "The expression after 'where' in a list quantifier must have type 'bool', but got '" + typeCondition + "'"));
            });

            makeUnusedVariableWarnings(typEnv, messages);
            typEnv.closeScope();

            return tryOptimizeExprOrNull(eoList, eoCondition, quanOp::withExprs, varEnv, BoolT.INSTANCE, messages.hasErrorOnNode());
        }
        else if (expr instanceof Accessor accessor) {
            var eoBase = validateAndOptimizeExpression(accessor.base(), typEnv, varEnv, requireConst, messages.detatch());
            Type baseType = eoBase.type();
            Ident field = accessor.field();

            Type fieldType;
            if (baseType instanceof AccessibleT acc) {
                fieldType = acc.getField(field.value());
                if (fieldType == null)
                    messages.add(new SemError(field.line(), "A value of type '" + baseType + "' does not contain the field '" + field.value() + "'."));
            }
            else  {
                fieldType = null;
                if (baseType != null)
                    messages.add(new SemError(accessor.line(), "A value of type '" + baseType + "' does not have any fields to access."));
            }

            return tryOptimizeExprOrNull(eoBase, accessor::withBase, varEnv, fieldType, messages.hasErrorOnNode());
        }
        else if (expr == null) {
            return new ExprOptimizeResult(null, null, null);
        }
        else
            throw new RuntimeException("Unhandled Expr-type: " + expr.getClass().getCanonicalName());
    }

    private PlaylistLit.Load validateAndOptimizeMusic(PlaylistLit.Load load, TypeEnv typEnv, VariableEnv varEnv, MessageListBuilder messages) {
        StrResult<String> musicPathRes = BaseMusicProvider.validatePath(load.file().value());
        if (musicPathRes.isFailure()) {
            messages.add(new SemError(load.line(), musicPathRes.error));
            return null;
        }
        else {
            String musicPath = musicPathRes.value;
            if (!musicProvider.existsLocally(musicPath))
                messages.add(new SemError(load.line(), "Cannot find music file with name: '" + musicPath + "'"));
            else
                usedMusicFiles.add(musicPath);

            if (!AmDecoder.isSupportedFileType(Utils.getFileExtension(musicPath)))
                messages.add(new SemError(load.line(), "The file type '" + musicPath + "' is unsupported. Ambience Mini currently only supports file types: " + String.join(", ", AmDecoder.getSupportedFileTypes())));

            int line = load.line();
            ArgList newArgs = new ArgList();
            for (var arg : load.args()) {
                MessageListBuilder argMessages = messages.detatch();

                var expr = arg.expr();
                var eoArg = validateAndOptimizeExpression(expr, typEnv, varEnv, true, argMessages.detatch());
                Type type = eoArg.type();
                String argName = arg.ident().value();

                if (eoArg.isNotConstant())
                    argMessages.add(new SemError(line, "Music arguments must be constant, but '" + argName + "' has a non-constant value"));

                switch (argName) {
                    case Music.ARG_GAIN -> {
                        if (isNotNumber(type))
                            argMessages.add(new SemError(line, "The music argument '" + Music.ARG_GAIN + "' expected a numerical value, but got a value of type '" + type + "'"));
                    }
                    case Music.ARG_LOOP -> {
                        if (isNotBool(type))
                            argMessages.add(new SemError(line, "The music argument '" + Music.ARG_LOOP + "' expected a boolean value, but got a value of type '" + type + "'"));
                    }
                    case Music.ARG_LOOPSTART,
                         Music.ARG_LOOPEND,
                         Music.ARG_LOOPLENGTH -> {
                        if (type != null) {
                            if (!type.isInt())
                                argMessages.add(new SemError(line, "The music argument '" + argName + "' expected an integer value, but got a value of type '" + type + "'"));
                            else if (eoArg.isStaticallyEvaluable()) {
                                int sample = eoArg.getValue().asInt().orElse(0);
                                if (sample < 0)
                                    argMessages.add(new SemError(line, "The value of the music argument '" + argName + "' must be non-negative. Got '" + sample + "'"));
                            }
                        }
                    }
                    default -> argMessages.add(new SemError(line, "The music argument '" + argName + "' is invalid"));
                }

                if (argMessages.hasErrorOnNode() || expr == null)
                    newArgs = null;
                else if (newArgs != null)
                    newArgs.add(new Arg(arg.ident(), eoArg.expr()));
            }

            return messages.hasErrorOnNode() || newArgs == null ? null : load.withArgs(newArgs);
        }
    }


    private static Expr asExpr(Value<?> value) {
        return new ValueLit(value);
    }

    public ExprOptimizeResult tryOptimizeExprOrNull(
            ExprOptimizeResult res,
            Function<Expr, Expr> onWellDefined,
            VariableEnv varEnv,
            Type type,
            boolean hasError
    ) {
        Expr newExpr = hasError ? null : res.ifWellDefinedOrNull(onWellDefined);
        if (newExpr != null && res.isConstant())
            newExpr = asExpr(evalExpr(newExpr, varEnv));
        return mkRes(newExpr, type, res.baseIsConst());
    }

    public ExprOptimizeResult tryOptimizeExprOrNull(
            ExprOptimizeResult res1,
            ExprOptimizeResult res2,
            BiFunction<Expr, Expr, Expr> onWellDefined,
            VariableEnv varEnv,
            Type type,
            boolean hasError
    ) {
        Boolean isConst;
        Expr newExpr = hasError ? null : res1.ifWellDefinedOrNull(expr1 -> res2.ifWellDefinedOrNull(expr2 -> onWellDefined.apply(expr1, expr2)));
        if (newExpr == null)
            isConst = null;
        else {
            isConst = res1.isConstant() && res2.isConstant();
            if (isConst)
                newExpr = asExpr(evalExpr(newExpr, varEnv));
        }
        return mkRes(newExpr, type, isConst);
    }

    private ExprOptimizeResult mkRes(Expr expr, Type type, Boolean isConst) {
        return new ExprOptimizeResult(expr, type, isConst);
    }

    private ExprOptimizeResult mkRes(Value<?> value, Type type) {
        return value == null
                ? new ExprOptimizeResult(null, type, null)
                : new ExprOptimizeResult(new ValueLit(value), type, true);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Extra warnings
    private void makeUnusedVariableWarnings(TypeEnv typEnv, MessageListBuilder messages) {
        for (var pair : typEnv.getTopScopeUnused())
            messages.add(new Warning(pair.right(), "Unused variable '" + pair.left() + "'"));
    }

    private void makeUnusedMusicWarnings(ArrayList<Message> messages) {
        int skipLen = musicProvider.getBasePath().length() + 1;  // +1 to include '/' at end of path
        List<String> unusedFiles = musicProvider.listLocalMusicFiles().stream()
                .filter(path -> AmDecoder.isSupportedFileType(Utils.getFileExtension(path.toString())) && usedMusicFiles.stream().noneMatch(path::endsWith))
                .map(path -> "-- " + path.toString().substring(skipLen))
                .toList();
        if (!unusedFiles.isEmpty())
            messages.add(new Warning(-1, "There are unused files in the music directory:\n" + String.join("\n", unusedFiles)));
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Utilities
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
