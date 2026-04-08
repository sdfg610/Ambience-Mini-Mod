package me.molybdenum.ambience_mini.v1_21_1.network.configuration;

import me.molybdenum.ambience_mini.engine.shared.BuildConfig;
import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record ServerSupportConfigurationTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    public static final ConfigurationTask.Type TYPE =
            new ConfigurationTask.Type(AmbienceMini.rl("configure_server_support"));


    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        try {
            var packet = new ServerAmVersionPacket(BuildConfig.APP_VERSION.toString());
            sender.accept(packet);
        } catch (UnsupportedOperationException ignored) {
            listener.finishCurrentTask(TYPE); // If client does not have the mod. Just continue...
        }
    }

    @NotNull
    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}

