package me.molybdenum.ambience_mini.engine.shared.utils.versions;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record AmVersion(int major, int minor, int patch, int hotfix)
{
    public static AmVersion ZERO = new AmVersion(0,0,0);
    public static AmVersion V_2_5_0 = new AmVersion(2,5,0);
    public static AmVersion V_2_6_0 = new AmVersion(2,6,0);


    public AmVersion(int major, int minor, int patch) {
        this(major, minor, patch, 0);
    }


    public boolean isLessThan(AmVersion other) {
        return
            major < other.major
                || (major == other.major && (
                minor < other.minor
                    || (minor == other.minor && (
                    patch < other.patch
                        || (patch == other.patch &&
                        hotfix < other.hotfix
                    )
                ))
            ));
    }

    public boolean isGreaterThan(AmVersion other) {
        return other.isLessThan(this);
    }

    public boolean isLessThanOrEqual(AmVersion other) {
        return
            major < other.major
            || (major == other.major && (
                minor < other.minor
                || (minor == other.minor && (
                    patch < other.patch
                    || (patch == other.patch &&
                        hotfix <= other.hotfix
                    )
                ))
            ));
    }

    public boolean isGreaterThanOrEqual(AmVersion other) {
        return other.isLessThanOrEqual(this);
    }


    @Override
    public @NotNull String toString() {
        return major + "." + minor + "." + patch + (hotfix == 0 ? "" : "." + hotfix);
    }

    public static Optional<AmVersion> tryOfString(String version) {
        try {
            return Optional.of(ofString(version));
        }
        catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public static AmVersion ofString(String version) {
        String[] parts = version.split("\\.", 5);
        if (parts.length != 3 && parts.length != 4)
            throw new RuntimeException("Not a valid version string: " + version);

        int major = tryParseInt(parts[0]).orElseThrow(() -> invalid(version));
        int minor = tryParseInt(parts[1]).orElseThrow(() -> invalid(version));
        int patch = tryParseInt(parts[2]).orElseThrow(() -> invalid(version));
        int hotfix = parts.length == 4 ? tryParseInt(parts[3]).orElseThrow(() -> invalid(version)) : 0;

        return new AmVersion(major, minor, patch, hotfix);
    }

    private static Optional<Integer> tryParseInt(String str) {
        try {
            return Optional.of(Integer.parseInt(str));
        }
        catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static RuntimeException invalid(String version) {
        return new RuntimeException("Not a valid version string: '" + version + "'");
    }
}
