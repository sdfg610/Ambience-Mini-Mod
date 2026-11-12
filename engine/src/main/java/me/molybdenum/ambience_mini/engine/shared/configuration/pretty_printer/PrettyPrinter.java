package me.molybdenum.ambience_mini.engine.shared.configuration.pretty_printer;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.misc.ArgList;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.*;

import java.util.ArrayList;

public class PrettyPrinter {
    public static String printConfig(Config config)
    {
        var items = new ArrayList<String>();

        for (var decl : config.declarations())
            items.add(printDeclaration(decl));

        // TODO: Server playlists

        for (var preset : config.presets())
            items.add(printPreset(preset));

        return String.join("\n\n", items);
    }


    public static String printDeclaration(GlobalDecl decl) {
        return getTypeString(decl.type()) + " " + decl.ident().value() + " = " + printExpr(decl.value()) + ";";
    }


    public static String printPreset(Preset preset) {
        var cmdStr = printCommand(preset.body());

        return preset.name().value().isEmpty() ? cmdStr : "preset " + preset.name().value() + "\n" + cmdStr + "\nend";
    }

    public static String printCommand(Command command) {
        return printCommand(command, 0);
    }

    private static String printCommand(Command command, int depth) {
        if (command instanceof Skip)
            return indent(depth) + "skip;\n";
        else if (command instanceof Play play)
            return indent(depth) + "play "
                    + (play.isInstant() ? "instant " : "")
                    + (play.ifdef() ? "ifdef " : "")
                    + printExpr(play.playlist())
                    + play.getPriorityOpt().map(p -> " priority " + p).orElse("") + ";\n";
        else if (command instanceof Vanilla)
            return indent(depth) + "use_vanilla_player;\n";
        else if (command instanceof Interrupt interrupt)
            return indent(depth) + "interrupt " +
                    printCommand(interrupt.body(), interrupt.body() instanceof Block ? depth : 0);
        else if (command instanceof Block block)
            return "\n" + indent(depth) + "begin\n" +
                    String.join("", block.body().stream().map(sh -> printCommand(sh, depth+1)).toList()) +
                    indent(depth) + "end\n";
        else if (command instanceof When when)
            return indent(depth) + "when (" + printExpr(when.condition()) + ") " +
                    printCommand(when.body(), when.body() instanceof Block ? depth : 0);
        else if (command instanceof Let let)
            return indent(depth) + "let " + let.ident().value() + (let.type() == null ? "" : ": " + let.type()) + " = " + printExpr(let.value()) + " in\n" +
                    printCommand(let.body(), let.body() instanceof Block ? depth : 0);
        // TODO: Print modifier

        throw new RuntimeException("Unhandled Shed-type: " + command.getClass().getCanonicalName());
    }

    private static String indent(int depth) {
        return "    ".repeat(depth);
    }


    public static String printExpr(Expr expr) {
        if (expr instanceof Ident ident)
            return ident.value();
        else if (expr instanceof BoolLit boolLit)
            return Boolean.toString(boolLit.value());
        else if (expr instanceof IntLit intLit)
            return Integer.toString(intLit.value());
        else if (expr instanceof FloatLit floatLit)
            return Float.toString(floatLit.value());
        else if (expr instanceof StringLit stringLit)
            return '"' + stringLit.value() + '"';
        else if (expr instanceof PlaylistLit playlistLit)
            return printPlaylist(playlistLit);
        else if (expr instanceof ValueLit valueLit)
            return "ValueLit(" + valueLit.value() + ")";
        else if (expr instanceof GetEvent getEvent)
            return '@' + getEvent.eventName().value();
        else if (expr instanceof GetProperty getProperty)
            return '$' + getProperty.propertyName().value();
        // TODO: Unary operators
        else if (expr instanceof BinaryOp binOp)
            return printBinaryOp(binOp.op(), binOp.left(), binOp.right());
        else if (expr instanceof TerneryOp ternOp)
            return printTerneryOp(ternOp.expr1(), ternOp.expr2(), ternOp.expr3());
        else if (expr instanceof Accessor acc)
            return printAccessor(acc);
        else if (expr instanceof QuantifierOp quanOp)
            return String.format(
                    "%s %s in %s has %s end",
                    getQuantifierString(quanOp.quantifier()),
                    quanOp.identifier(),
                    printExpr(quanOp.list()),
                    printExpr(quanOp.condition())
            );
        else
            throw new RuntimeException("Unhandled Expr-type: " + expr.getClass().getCanonicalName());
    }

    private static String surround(Expr expr)
    {
        if (expr instanceof BinaryOp || expr instanceof TerneryOp)
            return "(" + printExpr(expr) + ")";
        return printExpr(expr);
    }


    public static String printPlaylist(PlaylistLit playlistLit) {
        var loads = playlistLit.music().stream()
                .map(load -> '"' + load.file().value() + '"' + getArgsString(load.args()))
                .toList();

        if (loads.isEmpty())
            return "[ ]";
        else if (loads.size() == 1)
            return "[ " + loads.get(0) + " ]";
        else
            return "[\n" + String.join(",\n", loads.stream().map(load -> indent(1) + load).toList()) + "\n]";
    }

    private static String getArgsString(ArgList args) {
        return args.isEmpty()
                ? ""
                : "<" + String.join(", ", args.stream().map(arg -> arg.ident().value() + "=" + printExpr(arg.expr())).toList()) + ">";
    }


    public static String printBinaryOp(BinaryOperators op, Expr left, Expr right) {
        String l = surround(left);
        String r = surround(right);
        return switch (op) {
            case INDEXER -> l + "[" + r + "]";
            case EQ, APP_EQ, MATCH, AND, OR, LT, LE, ADD, SUB, MUL, DIV, APPEND, NULL_CHECK
                    -> l + " " + getBinaryOpInfix(op) + " " + r;
        };
    }

    public static String getBinaryOpInfix(BinaryOperators op) {
        return switch (op) {
            case EQ -> "==";
            case APP_EQ -> "~~";
            case MATCH -> "*~";
            case AND -> "&&";
            case OR -> "||";
            case LT -> "<";
            case LE -> "<=";
            case INDEXER -> "[]";
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "/";
            case APPEND -> "++";
            case NULL_CHECK -> "??";
        };
    }

    public static String printTerneryOp(Expr expr1, Expr expr2, Expr expr3) {
        String e1 = surround(expr1);
        String e2 = surround(expr2);
        String e3 = surround(expr3);
        return e1 + " ? " + e2 + " : " + e3;
    }

    public static String getQuantifierString(Quantifiers op) {
        return switch (op) {
            case ANY -> "any";
            case ALL -> "all";
            case GET -> "get";
            case COUNT -> "count";
        };
    }

    public static String getTypeString(Type type) {
        if (type instanceof AnyT)
            return "any";
        else if (type instanceof BoolT)
            return "bool";
        else if (type instanceof IntT)
            return "int";
        else if (type instanceof FloatT)
            return "float";
        else if (type instanceof StringT)
            return "string";
        else if (type instanceof AreaT)
            return "area";
        else if (type instanceof CombatantT)
            return "combatant";
        else if (type instanceof PlaylistT)
            return "playlist";
        else if (type instanceof ListT listT)
            return "list<" + getTypeString(listT.elementType) + ">";
        else if (type instanceof MapT mapT)
            return "map<" + getTypeString(mapT.keyType) + ", " + getTypeString(mapT.valueType) + ">";
        else if (type == null)
            return "null";

        throw new RuntimeException("Unhandled Type-type: " + type.getClass().getCanonicalName());
    }

    private static String printAccessor(Accessor acc) {
        return printExpr(acc.base()) + "." + acc.field().value();
    }
}
