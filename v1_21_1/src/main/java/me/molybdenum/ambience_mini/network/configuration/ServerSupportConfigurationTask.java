package me.molybdenum.ambience_mini.network.configuration;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.network.to_client.HasServerSupportPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record ServerSupportConfigurationTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    private static final ResourceLocation ID = AmbienceMini.rl("configure_server_support");
    private static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new HasServerSupportPacket());
        listener.finishCurrentTask(type());
    }

    @NotNull
    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }
}
