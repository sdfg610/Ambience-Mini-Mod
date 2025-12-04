package me.molybdenum.ambience_mini.engine.compatibility;

import java.util.function.Function;


public class EssentialCompat
{
    public static boolean isLoaded = false;
    private static Object fakePlayer = null;
    private static Object fakeWorld = null;


    /**
     * @param player The potentially fake player object, which also contains a reference to the fake world.
     * @param getGameProfileName Accessor to get the <code>serverBrand</code> property of the player, which is used ot detect the fake player.
     * @param getLevel Accessor to get the reference to the fake level.
     * @return <code>true</code> when the fake player and world is captured for the first time. <code>false</code> otherwise.
     */
    public static <T> boolean tryCaptureFakes(
            T player,
            Function<T, String> getGameProfileName,
            Function<T, Object> getLevel
    ) {
        if (fakePlayer == null && player != null && "EmulatedClient".equals(getGameProfileName.apply(player))) {
            fakePlayer = player;
            fakeWorld = getLevel.apply(player);
            return true;
        }
        return false;
    }

    public static boolean isNotFakePlayer(Object player) {
        return fakePlayer == null || player != fakePlayer;
    }

    public static boolean isNotFakeWorld(Object world) {
        return fakeWorld == null || world != fakeWorld;
    }
}
