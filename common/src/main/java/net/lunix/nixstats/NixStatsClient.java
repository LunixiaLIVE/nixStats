package net.lunix.nixstats;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.lunix.nixstats.screen.NixStatsConfigScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import org.lwjgl.glfw.GLFW;

/**
 * Common (loader-agnostic) client logic for nixStats. No mod-loader API here —
 * the Fabric and NeoForge entrypoints wire their native client events and call
 * these handlers. No Architectury runtime dependency.
 */
public final class NixStatsClient {

    public static final String MOD_ID = "nixstats";
    public static final int PHANTOM_THRESHOLD = 72000;

    private static int lastRemaining = PHANTOM_THRESHOLD;
    private static int syncTick      = 0;
    private static KeyMapping openConfigKey;
    private static KeyMapping.Category configCategory;

    private NixStatsClient() {}

    public static int getLastRemaining() {
        return lastRemaining;
    }

    public static KeyMapping.Category getConfigCategory() {
        return configCategory;
    }

    public static void loadConfig(java.nio.file.Path configDir) {
        NixStatsConfig.init(configDir);
        NixStatsConfig.load();
    }

    /** Build the (unbound) config keybind + its category. The platform registers it. */
    public static KeyMapping createConfigKey() {
        configCategory = KeyMapping.Category.register(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "config")
        );
        openConfigKey = new KeyMapping(
            "key.nixstats.config",
            GLFW.GLFW_KEY_UNKNOWN,
            configCategory
        );
        return openConfigKey;
    }

    /** Per-client-tick logic. Call from the loader's end-client-tick event. */
    public static void clientTick(Minecraft client) {
        if (client.player == null) return;

        // Phantom timer: read server-side in singleplayer for tick-accurate value
        MinecraftServer srv = client.getSingleplayerServer();
        if (srv != null) {
            ServerPlayer sp = srv.getPlayerList().getPlayer(client.player.getUUID());
            int ticksSinceRest = sp != null
                ? sp.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST))
                : client.player.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            lastRemaining = Math.max(0, PHANTOM_THRESHOLD - ticksSinceRest);
        } else {
            int ticksSinceRest = client.player.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
            lastRemaining = Math.max(0, PHANTOM_THRESHOLD - ticksSinceRest);
        }

        // Periodically push REQUEST_STATS to keep all client-side stats fresh.
        if (client.getConnection() != null &&
            ++syncTick >= NixStatsConfig.get().syncInterval * 20) {
            syncTick = 0;
            client.getConnection().send(
                new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS)
            );
        }

        if (openConfigKey != null && openConfigKey.consumeClick()) {
            client.execute(NixStatsClient::openConfig);
        }
    }

    /** Open the config screen. */
    public static void openConfig() {
        Minecraft.getInstance().setScreen(new NixStatsConfigScreen(null));
    }

    /**
     * Register the {@code /nixstats config} client command. Source-type agnostic so
     * it works on both Fabric's client dispatcher and NeoForge's CommandSourceStack.
     */
    public static <S> void registerClientCommand(CommandDispatcher<S> dispatcher) {
        dispatcher.register(
            LiteralArgumentBuilder.<S>literal("nixstats")
                .then(LiteralArgumentBuilder.<S>literal("config")
                    .executes(ctx -> {
                        Minecraft.getInstance().execute(NixStatsClient::openConfig);
                        return 1;
                    }))
        );
    }
}
