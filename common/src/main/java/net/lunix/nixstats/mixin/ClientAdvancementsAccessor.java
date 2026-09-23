package net.lunix.nixstats.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.client.multiplayer.ClientAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Read-only accessor for the client's per-advancement progress map, which is private
 * and has no public getter. Needed to read advancement progress on multiplayer servers
 * (in single-player we read the integrated server directly instead).
 *
 * <p>Also reads the advancement tree through its field: the getter was renamed
 * ({@code getTree()} before 26.3, {@code tree()} from 26.3), but the field kept its name
 * on every 26.x line, so one accessor lets a single jar run on all of them.
 */
@Mixin(ClientAdvancements.class)
public interface ClientAdvancementsAccessor {

    @Accessor("progress")
    Map<AdvancementHolder, AdvancementProgress> nixstats$getProgress();

    @Accessor("tree")
    AdvancementTree nixstats$getTree();
}
