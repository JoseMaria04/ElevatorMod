package xyz.vsngamer.elevatorid.init;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Mismas opciones que la ForgeConfigSpec original, guardadas en config/elevatorid.json.
 * Ojo: en Forge era config de servidor sincronizada; aquí cada lado lee su fichero (el servidor valida igualmente).
 */
public class ModConfig {
    private static final Logger LOG = LoggerFactory.getLogger("elevatorid");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final CommonGeneral GENERAL = new CommonGeneral();

    public static final class Value<T> {
        private T value;

        Value(T def) {
            value = def;
        }

        public T get() {
            return value;
        }
    }

    public static class CommonGeneral {
        /** ¿Los ascensores tienen que ser del mismo color para teletransportar? */
        public final Value<Boolean> sameColor = new Value<>(false);
        /** Alcance del ascensor (3..4064) */
        public final Value<Integer> range = new Value<>(384);
        /** ¿Recolocar al jugador en el centro del ascensor? */
        public final Value<Boolean> precisionTarget = new Value<>(true);
        /** ¿Pueden aparecer mobs encima? (requiere reiniciar) */
        public final Value<Boolean> mobSpawn = new Value<>(false);
        public final Value<Boolean> resetPitchNormal = new Value<>(false);
        public final Value<Boolean> resetPitchDirectional = new Value<>(true);
        /** ¿Gastar experiencia al teletransportarse? */
        public final Value<Boolean> useXP = new Value<>(false);
        /** Puntos de experiencia (no niveles) por uso */
        public final Value<Integer> XPPointsAmount = new Value<>(1);
    }

    public static void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("elevatorid.json");
        CommonGeneral g = GENERAL;
        try {
            if (Files.exists(file)) {
                JsonObject o = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                if (o.has("sameColor")) g.sameColor.value = o.get("sameColor").getAsBoolean();
                if (o.has("range")) g.range.value = Math.max(3, Math.min(4064, o.get("range").getAsInt()));
                if (o.has("precisionTarget")) g.precisionTarget.value = o.get("precisionTarget").getAsBoolean();
                if (o.has("mobSpawn")) g.mobSpawn.value = o.get("mobSpawn").getAsBoolean();
                if (o.has("resetPitchNormal")) g.resetPitchNormal.value = o.get("resetPitchNormal").getAsBoolean();
                if (o.has("resetPitchDirectional")) g.resetPitchDirectional.value = o.get("resetPitchDirectional").getAsBoolean();
                if (o.has("useXP")) g.useXP.value = o.get("useXP").getAsBoolean();
                if (o.has("XPPointsAmount")) g.XPPointsAmount.value = Math.max(1, o.get("XPPointsAmount").getAsInt());
            }
            JsonObject out = new JsonObject();
            out.addProperty("sameColor", g.sameColor.get());
            out.addProperty("range", g.range.get());
            out.addProperty("precisionTarget", g.precisionTarget.get());
            out.addProperty("mobSpawn", g.mobSpawn.get());
            out.addProperty("resetPitchNormal", g.resetPitchNormal.get());
            out.addProperty("resetPitchDirectional", g.resetPitchDirectional.get());
            out.addProperty("useXP", g.useXP.get());
            out.addProperty("XPPointsAmount", g.XPPointsAmount.get());
            Files.writeString(file, GSON.toJson(out), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("No se pudo leer config/elevatorid.json; uso valores por defecto", e);
        }
    }
}
