package me.molybdenum.ambience_mini.engine.shared.core.areas;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;

import java.util.function.Function;

public class Owner implements AmSerializable
{
    public Ownership ownership;


    public Owner(AmReader reader) {
        ownership = reader.readBoolean()
                ? Owned.fromReader(reader)
                : Ownerless.fromReader(reader);
    }

    public Owner(Ownership ownership) {
        if (ownership == null)
            throw new NullPointerException("'ownership' cannot be null");

        this.ownership = ownership;
    }


    public String getOwnerIdIfOwned() {
        return ownership instanceof Owned owned ? owned.playerUUID : null;
    }

    public boolean isPrivate() {
        return ownership instanceof Owned owned && !owned.isShared;
    }

    public boolean isShared() {
        return ownership instanceof Owned owned && owned.isShared;
    }

    public boolean isPublic() {
        return ownership instanceof Ownerless ownerless && !ownerless.isLocal;
    }

    public boolean isLocal() {
        return ownership instanceof Ownerless ownerless && ownerless.isLocal;
    }


    public <T> T match(Function<Owned, T> onOwned, Function<Ownerless, T> onOwnerless) {
        return ownership instanceof Owned
                ? onOwned.apply((Owned)ownership)
                : onOwnerless.apply((Ownerless)ownership);
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeBoolean(ownership instanceof Owned);
        ownership.writeTo(writer);
    }


    public static boolean validateJson(JsonElement elem) {
        if (!Utils.isJsonObjectWith(elem, "isOwned", "ownership"))
            return false;

        JsonObject obj = elem.getAsJsonObject();
        return Utils.isJsonBoolean(obj.get("isOwned"))
                && (
                        Owned.validateJson(obj.get("ownership")) || Ownerless.validateJson(obj.get("ownership"))
                );
    }

    public static Owner fromJson(JsonObject obj) {
        return new Owner(
                obj.get("isOwned").getAsBoolean()
                        ? Owned.fromJson(obj.getAsJsonObject("ownership"))
                        : Ownerless.fromJson(obj.getAsJsonObject("ownership"))
        );
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.add("isOwned", new JsonPrimitive(ownership instanceof Owned));
        obj.add("ownership", ownership.toJson());
        return obj;
    }


    public Owner copy() {
        return new Owner(ownership.copy());
    }

    public static Owner ofPlayerUUID(String playerUUID) {
        return new Owner(new Owned(playerUUID, false));
    }


    public interface Ownership {
        void writeTo(AmWriter writer);
        JsonObject toJson();

        Ownership copy();
    }

    public static class Owned implements Ownership {
        public String playerUUID;
        public boolean isShared;


        public Owned(String playerUUID, boolean isShared) {
            this.playerUUID = playerUUID;
            this.isShared = isShared;
        }


        public void writeTo(AmWriter writer) {
            writer.writeString(playerUUID);
            writer.writeBoolean(isShared);
        }

        public static Owned fromReader(AmReader reader) {
            return new Owned(reader.readString(), reader.readBoolean());
        }


        public static boolean validateJson(JsonElement elem) {
            if (!Utils.isJsonObjectWith(elem, "playerUUID", "isShared"))
                return false;

            JsonObject obj = elem.getAsJsonObject();
            return Utils.isJsonString(obj.get("playerUUID"))
                    && Utils.isJsonBoolean(obj.get("isShared"));
        }

        public static Owned fromJson(JsonObject obj) {
            return new Owned(
                    obj.get("playerUUID").getAsString(),
                    obj.get("isShared").getAsBoolean()
            );
        }

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.add("playerUUID", new JsonPrimitive(playerUUID));
            obj.add("isShared", new JsonPrimitive(isShared));
            return obj;
        }


        @Override
        public Ownership copy() {
            return new Owned(
                    playerUUID,
                    isShared
            );
        }
    }

    public static class Ownerless implements Ownership {
        public boolean isLocal;


        public Ownerless(boolean isLocal) {
            this.isLocal = isLocal;
        }


        public void writeTo(AmWriter writer) {
            writer.writeBoolean(isLocal);
        }

        public static Ownerless fromReader(AmReader reader) {
            return new Ownerless(reader.readBoolean());
        }


        public static boolean validateJson(JsonElement elem) {
            if (!Utils.isJsonObjectWith(elem, "isLocal"))
                return false;

            JsonObject obj = elem.getAsJsonObject();
            return Utils.isJsonBoolean(obj.get("isLocal"));
        }

        public static Ownerless fromJson(JsonObject obj) {
            return new Ownerless(
                    obj.get("isLocal").getAsBoolean()
            );
        }

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.add("isLocal", new JsonPrimitive(isLocal));
            return obj;
        }



        @Override
        public Ownership copy() {
            return new Ownerless(isLocal);
        }
    }
}
