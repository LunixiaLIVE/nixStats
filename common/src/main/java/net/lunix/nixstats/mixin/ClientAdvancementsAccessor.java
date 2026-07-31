package net.lunix.nixstats.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Read-only accessor for the client's per-advancement progress map, which is private
 * and has no public getter. Needed to read advancement progress on multiplayer servers
 * (in single-player we read the integrated server directly instead). This is the mod's
 * only mixin — a plain field accessor, no bytecode injection.
 */
@Mixin(ClientAdvancements.class)
public interface ClientAdvancementsAccessor {

    @Accessor("progress")
    Map<AdvancementHolder, AdvancementProgress> nixstats$getProgress();
}
