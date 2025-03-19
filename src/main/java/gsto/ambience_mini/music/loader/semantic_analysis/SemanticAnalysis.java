package gsto.ambience_mini.music.loader.semantic_analysis;

import gsto.ambience_mini.music.loader.abstract_syntax.conf.*;
import gsto.ambience_mini.music.loader.abstract_syntax.expr.*;
import gsto.ambience_mini.music.loader.abstract_syntax.play.*;
import gsto.ambience_mini.music.loader.abstract_syntax.shed.*;
import gsto.ambience_mini.music.loader.abstract_syntax.type.*;
import gsto.ambience_mini.music.loader.pretty_printer.PrettyPrinter;
import gsto.ambience_mini.music.state.Event;
import gsto.ambience_mini.music.state.Property;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public record SemanticAnalysis(String musicDirectory) {
    public Stream<String> Conf(Conf conf, Env env) {
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

    private Stream<String> PL(PL play, Env env) {
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
        else if (play instanceof Load load)
            return Files.exists(getMusicPath(load.file().value()))
                    ? Stream.empty()
                    : Stream.of("Cannot find music-file with name: '" + load.file().value() + "'");

        else if (play instanceof Nil)
            return Stream.empty();

        throw new RuntimeException("Unhandled PL-type: " + play.getClass().getCanonicalName());
    }

    private Stream<String> Shed(Shed shed, Env env) {
        if (shed instanceof Play play)
            return PL(play.playlist(), env);
        else if (shed instanceof Block block)
            return block.body().stream().map(sh -> Shed(sh, env)).reduce(Stream.empty(), Stream::concat);
        else if (shed instanceof When when) {
            ArrayList<String> errors = new ArrayList<>();

            Type type = Expr(when.condition(), env, errors);
            if (!(type instanceof BoolT))
                errors.add("The condition inside a 'when' must result in a boolean value.");

            if (when.body() instanceof Interrupt)
                errors.add("An interrupt can only be a child of a block (begin/end). Not a 'when'.");

            return Stream.concat(errors.stream(), Shed(when.body(), env));
        }
        else if (shed instanceof Interrupt interrupt) {
            if (env.inInterrupt > 0)
                return Stream.of("An 'interrupt' may not occur inside the body of another 'interrupt'.");

            env.inInterrupt++; // Track multiple nested interrupts to allow finding other errors within.
            Stream<String> res = interrupt.shed() instanceof When
                    ? Shed(interrupt.shed(), env)
                    : Stream.of("The 'interrupt' keyword may only be followed by a 'when' clause.");
            env.inInterrupt--;

            return res;
        }

        throw new RuntimeException("Unhandled Shed-type: " + shed.getClass().getCanonicalName());
    }

    public Type Expr(Expr expr, Env env, ArrayList<String> errors) {
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
            Optional<Property> prop = Property.tryGet(property.propertyName().value());
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

    private Path getMusicPath(String musicName) {
        if (!musicName.endsWith(".mp3"))
            return Path.of(musicDirectory, musicName + ".mp3");
        return Path.of(musicDirectory, musicName);
    }
}
