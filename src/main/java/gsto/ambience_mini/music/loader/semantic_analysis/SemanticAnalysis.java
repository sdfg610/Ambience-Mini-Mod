package gsto.ambience_mini.music.loader.semantic_analysis;

import gsto.ambience_mini.music.loader.abstract_syntax.conf.*;
import gsto.ambience_mini.music.loader.abstract_syntax.expr.*;
import gsto.ambience_mini.music.loader.abstract_syntax.play.*;
import gsto.ambience_mini.music.loader.abstract_syntax.shed.*;
import gsto.ambience_mini.music.loader.abstract_syntax.type.*;
import gsto.ambience_mini.music.loader.pretty_printer.PrettyPrinter;
import gsto.ambience_mini.music.state.Event;
import gsto.ambience_mini.music.state.Property;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class SemanticAnalysis {
    public static Stream<String> Conf(Conf conf, Env env) {
        ArrayList<String> errors = new ArrayList<>();

        if (conf instanceof Playlist playlist) {
            String name = playlist.ident().value();

            if (env.hasPlaylist(name))
                errors.add("Multiple definition of playlist: " + name);
            else
                env.addPlaylist(name);

            Stream<String> plErr = PL(playlist.playlist(), env);
            Stream<String> confErr = Conf(playlist.conf(), env);

            return Stream.concat(errors.stream(), Stream.concat(plErr, confErr));
        }
        else if (conf instanceof Schedule schedule)
            return Shed(schedule.schedule(), env);

        throw new RuntimeException("Unhandled Conf-type: " + conf.getClass().getCanonicalName());
    }

    private static Stream<String> PL(PL play, Env env) {
        if (play instanceof IdentP ident) {
            String name = ident.value();
            return env.hasPlaylist(name)
                    ? Stream.empty()
                    : Stream.of("Use of undefined playlist: " + name);
        }
        else if (play instanceof Concat concat)
            return Stream.concat(
                    PL(concat.left(), env),
                    PL(concat.right(), env)
            );
        else if (play instanceof Load || play instanceof Nil)
            return Stream.empty();

        throw new RuntimeException("Unhandled PL-type: " + play.getClass().getCanonicalName());
    }

    private static Stream<String> Shed(Shed shed, Env env) {
        if (shed instanceof Play play) {
            if (play.playlist() instanceof IdentP indent)
                return env.hasPlaylist(indent.value())
                        ? Stream.empty()
                        : Stream.of("Use of undefined playlist after 'play' keyword: " + indent.value());
            else if (play.playlist() instanceof Nil)
                return Stream.empty();
            return Stream.of("The 'play' keyword may only be followed by a single playlist-name or NIL.");
        }
        else if (shed instanceof Block block)
            return block.body().stream().map(sh -> Shed(sh, env)).reduce(Stream.empty(), Stream::concat);
        else if (shed instanceof When when) {
            ArrayList<String> errors = new ArrayList<>();

            Type type = Expr(when.condition(), env, errors);
            if (!(type instanceof BoolT))
                errors.add("The condition inside a 'when' must result in a boolean value");

            return Stream.concat(errors.stream(), Shed(when.body(), env));
        }
        else if (shed instanceof Interrupt interrupt) {
            if (env.inInterrupt)
                return Stream.of("An 'interrupt' may not occur inside the body of another 'interrupt'.");

            env.inInterrupt = true;
            Stream<String> res = interrupt.shed() instanceof When
                    ? Shed(interrupt.shed(), env)
                    : Stream.of("The 'interrupt' keyword may only be followed by a 'when' clause.");
            env.inInterrupt = false;

            return res;
        }

        throw new RuntimeException("Unhandled Shed-type: " + shed.getClass().getCanonicalName());
    }

    public static Type Expr(Expr expr, Env env, ArrayList<String> errors) {
        if (expr instanceof IdentE ident) {
            errors.add("Lone identifiers currently do not have a use in expressions. Unknown identifier: " + ident.value());
            return null;
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
            if (!Event.exists(ev.eventName().value()))
                errors.add("Unknown event: @" + ev.eventName().value());
            return new BoolT();
        }
        else if (expr instanceof Get property) {
            Optional<Property> prop = Property.get(property.propertyName().value());
            if (prop.isEmpty()) {
                errors.add("Unknown property: $" + property.propertyName().value());
                return null;
            }
            return prop.get().type;
        }
        else if (expr instanceof BinaryOp binOp) {
            Type typeLeft = Expr(binOp.left(), env, errors);
            Type typeRight = Expr(binOp.right(), env, errors);

            switch (binOp.op()) {
                case EQ -> {
                    if (!Objects.equals(typeLeft, typeRight))
                        errors.add("Arguments of '==' must be of same type. Got '" + PrettyPrinter.printType(typeLeft) + "' and '" + PrettyPrinter.printType(typeRight) + "'");
                }
                case APP_EQ -> {
                    if (!(typeLeft instanceof StringT) || !(typeRight instanceof StringT))
                        errors.add("Arguments of '~~' must both be of type string. Got '" + PrettyPrinter.printType(typeLeft) + "' and '" + PrettyPrinter.printType(typeRight) + "'");
                }
                case AND -> {
                    if (!(typeLeft instanceof BoolT) || !(typeRight instanceof BoolT))
                        errors.add("Arguments of '&&' must both be of type bool. Got '" + PrettyPrinter.printType(typeLeft) + "' and '" + PrettyPrinter.printType(typeRight) + "'");
                }
                case OR -> {
                    if (!(typeLeft instanceof BoolT) || !(typeRight instanceof BoolT))
                        errors.add("Arguments of '||' must both be of type bool. Got '" + PrettyPrinter.printType(typeLeft) + "' and '" + PrettyPrinter.printType(typeRight) + "'");
                }
            }

            return new BoolT();
        }

        throw new RuntimeException("Unhandled Expr-type: " + expr.getClass().getCanonicalName());
    }
}
