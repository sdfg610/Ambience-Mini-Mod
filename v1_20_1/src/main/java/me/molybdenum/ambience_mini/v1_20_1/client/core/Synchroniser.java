package me.molybdenum.ambience_mini.v1_20_1.client.core;

import me.molybdenum.ambience_mini.engine.client.core.BaseSynchroniser;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class Synchroniser extends BaseSynchroniser {
    private static final Minecraft mc = Minecraft.getInstance();


    @Override
    @Nullable
    public <T> T syncToMain(Supplier<T> task) {
        try {
            return mc.submit(task).get(MAIN_THREAD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ignored) {
            return null;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
