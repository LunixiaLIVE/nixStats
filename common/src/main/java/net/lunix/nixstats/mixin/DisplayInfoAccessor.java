package net.lunix.nixstats.mixin;

import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reads an advancement's icon and title through their fields. 26.3 turned DisplayInfo into a
 * record, renaming {@code getIcon()}/{@code getTitle()} to {@code icon()}/{@code title()}; the
 * fields kept their names on every 26.x line, so one accessor lets a single jar run on all of them.
 */
@Mixin(DisplayInfo.class)
public interface DisplayInfoAccessor {

    @Accessor("icon")
    ItemStackTemplate nixstats$getIcon();

    @Accessor("title")
    Component nixstats$getTitle();
}
