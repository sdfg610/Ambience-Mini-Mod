package gsto.ambience_mini.music.loader.semantic_analysis;

import gsto.ambience_mini.music.loader.abstract_syntax.conf.*;
import gsto.ambience_mini.music.loader.abstract_syntax.expr.*;
import gsto.ambience_mini.music.loader.abstract_syntax.shed.*;

public class SemanticAnalysis {
    public static String Conf(Conf conf)
    {
        if (conf instanceof Playlist playlist)
            return "";
        else if (conf instanceof Schedule schedule)
            return "";

        throw new RuntimeException("Unhandled Conf-type: " + conf.getClass().getCanonicalName());
    }

    private static String PL(Shed shed) {
        throw new RuntimeException("Unhandled Shed-type: " + shed.getClass().getCanonicalName());
    }

    private static String Shed(Shed shed) {
        if (shed instanceof Play play)
            return "";
        else if (shed instanceof Block block)
            return "";
        else if (shed instanceof When when)
            return "";
        else if (shed instanceof Interrupt interrupt)
            return "";

        throw new RuntimeException("Unhandled Shed-type: " + shed.getClass().getCanonicalName());
    }

    public static String Expr(Expr expr) {
        if (expr instanceof IdentE ident)
            return "";
        else if (expr instanceof BoolV boolV)
            return "";
        else if (expr instanceof IntV intV)
            return "";
        else if (expr instanceof FloatV floatV)
            return "";
        else if (expr instanceof StringV stringV)
            return "";
        else if (expr instanceof Event ev)
            return "";
        else if (expr instanceof Get get)
            return "";
        else if (expr instanceof BinaryOp binOp)
            return "";

        throw new RuntimeException("Unhandled Expr-type: " + expr.getClass().getCanonicalName());
    }
}
