package net.lunix.nixstats.neoforge;

import net.lunix.nixstats.NixStatsClient;
import net.lunix.nixstats.StatSidebar;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = NixStatsClient.MOD_ID, dist = Dist.CLIENT)
public class NixStatsNeoForge {

    private static final Identifier HUD_ID =
        Identifier.fromNamespaceAndPath(NixStatsClient.MOD_ID, "nixstats_hud");

    public NixStatsNeoForge(IEventBus modBus) {
        NixStatsClient.loadConfig(net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get());

        modBus.addListener(this::onRegisterGuiLayers);
        modBus.addListener(this::onRegisterKeyMappings);

        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
    }

    private void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(HUD_ID, (g, dt) -> StatSidebar.renderHud(g, dt));
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        KeyMapping key = NixStatsClient.createConfigKey();
        event.registerCategory(NixStatsClient.getConfigCategory());
        event.register(key);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        NixStatsClient.clientTick(Minecraft.getInstance());
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        NixStatsClient.registerClientCommand(event.getDispatcher());
    }
}
