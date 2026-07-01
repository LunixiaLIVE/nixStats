package net.lunix.nixstats.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.lunix.nixstats.NixStatsClient;
import net.lunix.nixstats.StatSidebar;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class NixStatsFabric implements ClientModInitializer {

    private static final Identifier HUD_ID =
        Identifier.fromNamespaceAndPath(NixStatsClient.MOD_ID, "nixstats_hud");

    @Override
    public void onInitializeClient() {
        NixStatsClient.loadConfig(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir());

        // Keybind — unbound by default, configurable in Controls
        KeyMappingHelper.registerKeyMapping(NixStatsClient.createConfigKey());

        // Client tick
        ClientTickEvents.END_CLIENT_TICK.register(NixStatsClient::clientTick);

        // Sidebar HUD
        HudElementRegistry.addLast(HUD_ID, new HudElement() {
            @Override
            public void extractRenderState(GuiGraphicsExtractor ext, DeltaTracker dt) {
                StatSidebar.renderHud(ext, dt);
            }
        });

        // /nixstats config
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            NixStatsClient.registerClientCommand(dispatcher));
    }
}
