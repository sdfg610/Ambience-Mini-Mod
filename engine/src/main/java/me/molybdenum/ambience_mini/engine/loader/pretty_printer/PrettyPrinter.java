package me.molybdenum.ambience_mini.engine.loader.pretty_printer;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.conf.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.playlist.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.schedule.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.*;

import java.util.List;
import java.util.stream.Stream;

public class PrettyPrinter {
    public static String printConf(Config config)
    {
        if (config instanceof PlaylistDecl playlistDecl)
            return "playlist " + playlistDecl.ident().value() + " = " + printPL(playlistDecl.playlist()) + ";\n\n" + printConf(playlistDecl.config());
        else if (config instanceof ScheduleDecl scheduleDecl)
            return printShed(scheduleDecl.schedule());

        throw new RuntimeException("Unhandled Conf-type: " + config.getClass().getCanonicalName());
    }


    public static String printPL(Playlist play) {
        if (play instanceof Nil)
            return "NIL";

        var list = flatten(play)
                .filter(pl -> !(pl instanceof Nil))
                .toList();

        List<String> vars = list.stream()
                .filter(pl -> pl instanceof IdentP)
                .map(pl -> ((IdentP)pl).value())
                .toList();

        List<String> loads = list.stream()
                .filter(pl -> pl instanceof Load)
                .map(pl -> (Load)pl)
                .map(load -> '"' + load.file().value() + '"' + (load.gain() == null ? "" : " <" + load.gain().value() + ">"))
                .toList();

        String varString = String.join(" ++ ", vars);
        String loadString;
        if (loads.isEmpty())
            loadString = "";
        else if (loads.size() == 1)
            loadString = "[ " + loads.get(0) + " ]";
        else
            loadString = "[\n" + String.join(",\n", loads.stream().map(load -> indent(1) + load).toList()) + "\n]";

        if (!varString.isEmpty() && !loadString.isEmpty())
            return varString + " ++ " + loadString;
        else if (varString.isEmpty() && loadString.isEmpty())
            return "NIL";
        else if (!varString.isEmpty())
            return varString;
        else
            return loadString;
    }

    private static Stream<Playlist> flatten (Playlist play) {
        return play instanceof Concat concat
                ? Stream.concat(flatten(concat.left()), flatten(concat.right()))
                : Stream.of(play);
    }


    public static String printShed(Schedule schedule) {
        return printShed(schedule, 0);
    }

    private static String printShed(Schedule schedule, int depth) {
        if (schedule instanceof Play play)
            return indent(depth) + "play " + printPL(play.playlist()) + ";\n";
        else if (schedule instanceof Block block)
            return "\n" + indent(depth) + "begin\n" +
                    String.join("", block.body().stream().map(sh -> printShed(sh, depth+1)).toList()) +
                    indent(depth) + "end\n";
        else if (schedule instanceof When when)
            return indent(depth) + "when (" + printExpr(when.condition()) + ") " +
                    printShed(when.body(), when.body() instanceof Block ? depth : 0);
        else if (schedule instanceof Interrupt interrupt)
            return indent(depth) + "interrupt " +
                    printShed(interrupt.body(), interrupt.body() instanceof Block ? depth : 0);

        throw new RuntimeException("Unhandled Shed-type: " + schedule.getClass().getCanonicalName());
    }

    private static String indent(int depth) {
        return "    ".repeat(depth);
    }


    public static String printExpr(Expr expr) {
        if (expr instanceof IdentE ident)
            return ident.value();
        else if (expr instanceof BoolLit boolLit)
            return Boolean.toString(boolLit.value());
        else if (expr instanceof IntLit intLit)
            return Integer.toString(intLit.value());
        else if (expr instanceof FloatLit floatLit)
            return Float.toString(floatLit.value());
        else if (expr instanceof StringLit stringLit)
            return '"' + stringLit.value() + '"';
        else if (expr instanceof GetEvent getEvent)
            return '@' + getEvent.eventName().value();
        else if (expr instanceof GetProperty getProperty)
            return '$' + getProperty.propertyName().value();
        else if (expr instanceof BinaryOp binOp)
            return surround(binOp.left()) + binaryOpString(binOp.op()) + surround(binOp.right());
        else if (expr instanceof QuantifierOp quanOp)
            return String.format(
                    "%s %s in %s has %s end",
                    getQuantifierString(quanOp.quantifier()),
                    quanOp.identifier(),
                    printExpr(quanOp.list()),
                    printExpr(quanOp.condition())
            );

        throw new RuntimeException("Unhandled Expr-type: " + expr.getClass().getCanonicalName());
    }

    private static String surround(Expr expr)
    {
        if (expr instanceof BinaryOp)
            return "(" + printExpr(expr) + ")";
        return printExpr(expr);
    }


    public static String binaryOpString(BinaryOperators op)
    {
        return switch (op) {
            case EQ -> " == ";
            case APP_EQ -> " ~~ ";
            case AND -> " && ";
            case OR -> " || ";
            case LT -> " < ";
        };
    }

    public static String getQuantifierString(Quantifiers op)
    {
        return switch (op) {
            case ANY -> "any";
            case ALL -> "all";
        };
    }

    public static String getTypeString(Type type) {
        if (type instanceof BoolT)
            return "bool";
        else if (type instanceof IntT)
            return "int";
        else if (type instanceof FloatT)
            return "float";
        else if (type instanceof StringT)
            return "string";
        else if (type instanceof ListT listT)
            return "list<" + getTypeString(listT.elementType()) + ">";
        else if (type instanceof PlaylistT)
            return "playlist";
        else if (type == null)
            return "null";

        throw new RuntimeException("Unhandled Type-type: " + type.getClass().getCanonicalName());
    }
}
