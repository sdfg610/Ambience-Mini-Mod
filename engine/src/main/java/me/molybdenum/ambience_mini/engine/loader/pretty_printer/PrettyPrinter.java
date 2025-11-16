package me.molybdenum.ambience_mini.engine.loader.pretty_printer;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.conf.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.play.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.shed.*;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.*;

import java.util.List;
import java.util.stream.Stream;

import static me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr.Quantifiers.ANY;

public class PrettyPrinter {
    public static String printConf(Conf conf)
    {
        if (conf instanceof Playlist playlist)
            return "playlist " + playlist.ident().value() + " = " + printPL(playlist.playlist()) + ";\n\n" + printConf(playlist.conf());
        else if (conf instanceof Schedule schedule)
            return printShed(schedule.schedule());

        throw new RuntimeException("Unhandled Conf-type: " + conf.getClass().getCanonicalName());
    }


    public static String printPL(PL play) {
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

    private static Stream<PL> flatten (PL play) {
        return play instanceof Concat concat
                ? Stream.concat(flatten(concat.left()), flatten(concat.right()))
                : Stream.of(play);
    }


    public static String printShed(Shed shed) {
        return printShed(shed, 0);
    }

    private static String printShed(Shed shed, int depth) {
        if (shed instanceof Play play)
            return indent(depth) + "play " + printPL(play.playlist()) + ";\n";
        else if (shed instanceof Block block)
            return "\n" + indent(depth) + "begin\n" +
                    String.join("", block.body().stream().map(sh -> printShed(sh, depth+1)).toList()) +
                    indent(depth) + "end\n";
        else if (shed instanceof When when)
            return indent(depth) + "when (" + printExpr(when.condition()) + ") " +
                    printShed(when.body(), when.body() instanceof Block ? depth : 0);
        else if (shed instanceof Interrupt interrupt)
            return indent(depth) + "interrupt " +
                    printShed(interrupt.shed(), interrupt.shed() instanceof Block ? depth : 0);

        throw new RuntimeException("Unhandled Shed-type: " + shed.getClass().getCanonicalName());
    }

    private static String indent(int depth) {
        return "    ".repeat(depth);
    }


    public static String printExpr(Expr expr) {
        if (expr instanceof IdentE ident)
            return ident.value();
        else if (expr instanceof BoolV boolV)
            return Boolean.toString(boolV.value());
        else if (expr instanceof IntV intV)
            return Integer.toString(intV.value());
        else if (expr instanceof FloatV floatV)
            return Float.toString(floatV.value());
        else if (expr instanceof StringV stringV)
            return '"' + stringV.value() + '"';
        else if (expr instanceof Ev ev)
            return '@' + ev.eventName().value();
        else if (expr instanceof Get get)
            return '$' + get.propertyName().value();
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
