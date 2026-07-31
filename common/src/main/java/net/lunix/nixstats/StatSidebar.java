package net.lunix.nixstats;

import net.lunix.nixstats.screen.StatPickerScreen;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class StatSidebar {

    private static final int BORDER    = 1;
    private static final int TITLE_H   = 11;
    private static final int ROW_H     = 16;
    private static final int PAD_L     = 3;
    private static final int ICON_SLOT = 12;   // icons scaled to 12×12
    private static final int ICON_GAP  = 3;
    public static int computeFrameWPx(NixStatsConfig cfg, Font font, Minecraft mc, float scale) {
        float trs      = scale * Math.max(0.1f, cfg.textScale);
        int b          = Math.max(1, Math.round(BORDER * scale));
        int padLPx     = Math.round(PAD_L * scale);
        int iconSlotPx = Math.round(ICON_SLOT * scale);
        int iconGapPx  = Math.round(ICON_GAP * scale);
        int colGapPx   = Math.round(4 * scale);
        int rightPadPx = Math.round(4 * scale);
        int maxLabelPx = 0;
        int maxValuePx = 0;
        if (cfg.stats != null) {
            for (StatEntry entry : cfg.stats) {
                maxLabelPx = Math.max(maxLabelPx, Math.round((font.width(entry.label) + cfg.colPad) * trs));
                int raw = readStatValue(entry, mc);
                maxValuePx = Math.max(maxValuePx, Math.round((font.width(formatValue(entry, raw)) + cfg.colPad) * trs));
            }
        }
        int col1Px = padLPx + iconSlotPx + iconGapPx + maxLabelPx + colGapPx;
        int col2Px = maxValuePx + rightPadPx;
        return Math.max(Math.round(80 * scale), 2 * b + col1Px + col2Px);
    }

    public static int frameH(NixStatsConfig cfg) {
        int n = cfg.stats != null ? cfg.stats.size() : 1;
        return 2 * BORDER + TITLE_H + BORDER + n * ROW_H + 2;
    }

    private static final int COL_BORDER   = 0xFF636363;
    private static final int COL_BG       = 0xFF1E1E1E;
    private static final int COL_TITLE_BG = 0xFF2A2A2A;
    private static final int COL_LABEL    = 0xFFCCCCCC;
    private static final int COL_VALUE    = 0xFFFFFFFF;

    public static void render(GuiGraphicsExtractor g, int x, int y, float scale, NixStatsConfig cfg) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        float trs = scale * Math.max(0.1f, cfg.textScale);
        float op  = Math.max(0f, Math.min(1f, cfg.hudOpacity));   // fades background/frame; text & icons stay opaque

        int b          = Math.max(1, Math.round(BORDER * scale));
        int th         = Math.round(TITLE_H * scale);
        int rh         = Math.round(ROW_H * scale);
        int padLPx     = Math.round(PAD_L * scale);
        int iconSlotPx = Math.round(ICON_SLOT * scale);
        int iconGapPx  = Math.round(ICON_GAP * scale);
        int colGapPx   = Math.round(4 * scale);
        int rightPadPx = Math.round(4 * scale);
        float iconScale = (ICON_SLOT / 16f) * scale;

        List<StatEntry> stats = cfg.stats;

        // Pre-pass: read all values and compute column widths
        int maxLabelPx = 0, maxValuePx = 0;
        int[] rawValues = null;
        String[] valStrs = null;
        int[] valColors = null;
        if (stats != null && !stats.isEmpty()) {
            rawValues = new int[stats.size()];
            valStrs   = new String[stats.size()];
            valColors = new int[stats.size()];
            for (int i = 0; i < stats.size(); i++) {
                StatEntry e = stats.get(i);
                rawValues[i] = readStatValue(e, mc);
                valStrs[i]   = formatValue(e, rawValues[i]);
                valColors[i] = getValueColor(e, rawValues[i], cfg);
                maxLabelPx = Math.max(maxLabelPx, Math.round((font.width(e.label) + cfg.colPad) * trs));
                maxValuePx = Math.max(maxValuePx, Math.round((font.width(valStrs[i]) + cfg.colPad) * trs));
            }
        }

        int col1Px = padLPx + iconSlotPx + iconGapPx + maxLabelPx + colGapPx;
        int col2Px = maxValuePx + rightPadPx;
        int sw = Math.max(Math.round(80 * scale), 2 * b + col1Px + col2Px);
        int sh = Math.round(frameH(cfg) * scale);

        // Border + background
        g.fill(x, y, x + sw, y + sh, withOpacity(COL_BORDER, op));
        g.fill(x + b, y + b, x + sw - b, y + sh - b, withOpacity(COL_BG, op));

        // Title bar
        g.fill(x + b, y + b, x + sw - b, y + b + th, withOpacity(COL_TITLE_BG, op));

        // Title text (centered)
        String titleStr = cfg.sidebarTitle != null ? cfg.sidebarTitle : "nixStats";
        float tfw = font.width(titleStr) * trs;
        float tfx = x + b + (sw - 2 * b - tfw) / 2f;
        float tfy = y + b + (th - 8f * trs) / 2f;
        renderScaledText(g, font, titleStr, tfx, tfy, trs, COL_VALUE);

        // Separator below title
        int sepY = y + b + th;
        g.fill(x + b, sepY, x + sw - b, sepY + b, withOpacity(COL_BORDER, op));

        if (stats == null || stats.isEmpty()) return;

        int rowsTopY = sepY + b;
        int textX    = x + b + padLPx + iconSlotPx + iconGapPx;

        for (int i = 0; i < stats.size(); i++) {
            StatEntry entry = stats.get(i);
            int rowY = rowsTopY + i * rh;

            if (i % 2 == 1) g.fill(x + b, rowY, x + sw - b, rowY + rh, withOpacity(0x0AFFFFFF, op));

            // Icon (col1)
            ItemStack icon = getIcon(entry);
            if (!icon.isEmpty()) {
                int iconX = x + b + padLPx;
                int iconY = rowY + (rh - iconSlotPx) / 2;
                g.pose().pushMatrix();
                g.pose().translate(iconX, iconY);
                g.pose().scale(iconScale, iconScale);
                g.item(icon, 0, 0);
                g.pose().popMatrix();
            }

            float textY = rowY + (rh - 8f * trs) / 2f;

            // Label (col1 — truncation is safety only)
            String labelStr = truncate(font, entry.label, maxLabelPx, trs);
            renderScaledText(g, font, labelStr, textX, textY, trs, COL_LABEL);

            // Value (col2 — right-aligned)
            int valW  = Math.round(font.width(valStrs[i]) * trs);
            float valX = x + sw - b - rightPadPx - valW;
            renderScaledText(g, font, valStrs[i], valX, textY, trs, valColors[i]);
        }
    }

    private static void renderScaledText(GuiGraphicsExtractor g, Font font, String text, float fx, float fy, float scale, int color) {
        g.pose().pushMatrix();
        g.pose().translate(fx, fy);
        g.pose().scale(scale, scale);
        g.text(font, text, 0, 0, color);
        g.pose().popMatrix();
    }

    private static String truncate(Font font, String label, int maxPx, float scale) {
        if (label == null) return "";
        int maxUnscaled = (int)(maxPx / scale);
        if (maxUnscaled <= 0) return "";
        if (font.width(label) <= maxUnscaled) return label;
        String t = label;
        while (!t.isEmpty() && font.width(t + "..") > maxUnscaled)
            t = t.substring(0, t.length() - 1);
        return t + "..";
    }

    /** Scale a color's alpha channel by {@code op} (0..1), leaving RGB intact. */
    private static int withOpacity(int argb, float op) {
        int a = Math.round(((argb >>> 24) & 0xFF) * op);
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static int readStatValue(StatEntry entry, Minecraft mc) {
        if ("phantom".equals(entry.statType)) return NixStatsClient.getLastRemaining();
        // Advancements: 1/0 for a single advancement (drives the ✓ color), done-count for a total.
        if ("advancement".equals(entry.statType)) {
            AdvancementHolder h = Advancements.byId(mc, entry.targetId);
            return Advancements.isDone(Advancements.progress(mc, h)) ? 1 : 0;
        }
        if ("advancement_total".equals(entry.statType)) {
            return Advancements.total(mc, entry.targetId)[0];
        }
        if (mc.player == null) return 0;
        // In singleplayer, read directly from the integrated server for real-time accuracy
        StatsCounter stats = mc.player.getStats();
        var srv = mc.getSingleplayerServer();
        if (srv != null) {
            ServerPlayer sp = srv.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) stats = sp.getStats();
        }
        if (entry.targetId == null) return 0;
        Identifier loc = Identifier.tryParse(entry.targetId);
        if (loc == null) return 0;
        try {
            return switch (entry.statType) {
                case "block_mined" -> {
                    var opt = BuiltInRegistries.BLOCK.getOptional(loc);
                    yield opt.isPresent() ? stats.getValue(Stats.BLOCK_MINED.get(opt.get())) : 0;
                }
                case "item_used" -> {
                    var opt = BuiltInRegistries.ITEM.getOptional(loc);
                    yield opt.isPresent() ? stats.getValue(Stats.ITEM_USED.get(opt.get())) : 0;
                }
                case "item_crafted" -> {
                    var opt = BuiltInRegistries.ITEM.getOptional(loc);
                    yield opt.isPresent() ? stats.getValue(Stats.ITEM_CRAFTED.get(opt.get())) : 0;
                }
                case "item_broken" -> {
                    var opt = BuiltInRegistries.ITEM.getOptional(loc);
                    yield opt.isPresent() ? stats.getValue(Stats.ITEM_BROKEN.get(opt.get())) : 0;
                }
                case "item_picked_up" -> {
                    var opt = BuiltInRegistries.ITEM.getOptional(loc);
                    yield opt.isPresent() ? stats.getValue(Stats.ITEM_PICKED_UP.get(opt.get())) : 0;
                }
                case "item_dropped" -> {
                    var opt = BuiltInRegistries.ITEM.getOptional(loc);
                    yield opt.isPresent() ? stats.getValue(Stats.ITEM_DROPPED.get(opt.get())) : 0;
                }
                case "entity_killed" -> {
                    var opt = BuiltInRegistries.ENTITY_TYPE.getOptional(loc);
                    yield opt.isPresent() ? stats.getValue(Stats.ENTITY_KILLED.get(opt.get())) : 0;
                }
                case "entity_killed_by" -> {
                    var opt = BuiltInRegistries.ENTITY_TYPE.getOptional(loc);
                    yield opt.isPresent() ? stats.getValue(Stats.ENTITY_KILLED_BY.get(opt.get())) : 0;
                }
                case "custom" -> {
                    var opt = BuiltInRegistries.CUSTOM_STAT.getOptional(loc);
                    yield opt.isPresent() ? stats.getValue(Stats.CUSTOM.get(opt.get())) : 0;
                }
                default -> 0;
            };
        } catch (Exception e) {
            return 0;
        }
    }

    public static String formatValue(StatEntry entry, int raw) {
        // Phantom timer is the mod's own countdown — keep the mm:ss display.
        if ("phantom".equals(entry.statType)) {
            if (raw <= 0) return "0:00";
            int secs = raw / 20;
            return String.format("%d:%02d", secs / 60, secs % 60);
        }
        // Individual advancement: use Minecraft's own progress text ("12/50"), ✓ when done —
        // matches the vanilla advancements screen exactly.
        if ("advancement".equals(entry.statType)) {
            Minecraft mc = Minecraft.getInstance();
            AdvancementProgress p = Advancements.progress(mc, Advancements.byId(mc, entry.targetId));
            if (p == null) return "—";          // em dash — not synced yet
            if (p.isDone()) return "✓";         // ✓
            Component t = p.getProgressText();
            if (t != null) {
                String s = t.getString();
                if (!s.isEmpty()) return s;          // e.g. "12/50" for multi-criteria
            }
            return "✗";                          // ✗
        }
        // Advancement total (grand or per-namespace): done/total.
        if ("advancement_total".equals(entry.statType)) {
            int[] dt = Advancements.total(Minecraft.getInstance(), entry.targetId);
            return dt[0] + "/" + dt[1];
        }
        // Everything else: delegate to Minecraft's own formatter for that stat, so counts,
        // distance, time and damage all render exactly like the vanilla stats screen.
        Stat<?> stat = resolveStat(entry);
        return stat != null ? stat.format(raw) : String.valueOf(raw);
    }

    /** Resolve the underlying Minecraft {@link Stat} for a tracked entry (null if unresolvable). */
    private static Stat<?> resolveStat(StatEntry entry) {
        if (entry.targetId == null) return null;
        Identifier loc = Identifier.tryParse(entry.targetId);
        if (loc == null) return null;
        try {
            return switch (entry.statType) {
                case "block_mined"      -> BuiltInRegistries.BLOCK.getOptional(loc).map(x -> (Stat<?>) Stats.BLOCK_MINED.get(x)).orElse(null);
                case "item_used"        -> BuiltInRegistries.ITEM.getOptional(loc).map(x -> (Stat<?>) Stats.ITEM_USED.get(x)).orElse(null);
                case "item_crafted"     -> BuiltInRegistries.ITEM.getOptional(loc).map(x -> (Stat<?>) Stats.ITEM_CRAFTED.get(x)).orElse(null);
                case "item_broken"      -> BuiltInRegistries.ITEM.getOptional(loc).map(x -> (Stat<?>) Stats.ITEM_BROKEN.get(x)).orElse(null);
                case "item_picked_up"   -> BuiltInRegistries.ITEM.getOptional(loc).map(x -> (Stat<?>) Stats.ITEM_PICKED_UP.get(x)).orElse(null);
                case "item_dropped"     -> BuiltInRegistries.ITEM.getOptional(loc).map(x -> (Stat<?>) Stats.ITEM_DROPPED.get(x)).orElse(null);
                case "entity_killed"    -> BuiltInRegistries.ENTITY_TYPE.getOptional(loc).map(x -> (Stat<?>) Stats.ENTITY_KILLED.get(x)).orElse(null);
                case "entity_killed_by" -> BuiltInRegistries.ENTITY_TYPE.getOptional(loc).map(x -> (Stat<?>) Stats.ENTITY_KILLED_BY.get(x)).orElse(null);
                case "custom"           -> BuiltInRegistries.CUSTOM_STAT.getOptional(loc).map(x -> (Stat<?>) Stats.CUSTOM.get(x)).orElse(null);
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    public static int getValueColor(StatEntry entry, int raw, NixStatsConfig cfg) {
        if ("advancement".equals(entry.statType)) return raw >= 1 ? 0xFF55FF55 : COL_VALUE;
        if (!"phantom".equals(entry.statType)) return COL_VALUE;
        float fraction = Math.min(1f, (float) raw / NixStatsClient.PHANTOM_THRESHOLD);
        if (fraction > cfg.thresholdWarning)  return cfg.colorRested;
        if (fraction > cfg.thresholdCritical) return cfg.colorWarning;
        return cfg.colorCritical;
    }

    public static ItemStack getIcon(StatEntry entry) {
        if ("phantom".equals(entry.statType)) return new ItemStack(Items.PHANTOM_MEMBRANE);
        if ("advancement".equals(entry.statType)) {
            Minecraft mc = Minecraft.getInstance();
            AdvancementHolder h = Advancements.byId(mc, entry.targetId);
            if (h != null && h.value().display().isPresent())
                return h.value().display().get().getIcon().create();
            return new ItemStack(Items.PAPER);
        }
        if ("advancement_total".equals(entry.statType)) {
            if (entry.targetId == null || entry.targetId.isEmpty())
                return new ItemStack(Items.NETHER_STAR);
            ItemStack root = Advancements.namespaceRootIcon(Minecraft.getInstance(), entry.targetId);
            return root.isEmpty() ? new ItemStack(Items.NETHER_STAR) : root;
        }
        if ("entity_killed".equals(entry.statType) || "entity_killed_by".equals(entry.statType))
            return resolveSpawnEgg(entry.targetId);
        if ("custom".equals(entry.statType)) return new ItemStack(Items.PAPER);
        return resolveItem(entry.targetId);
    }

    private static ItemStack resolveItem(String id) {
        if (id == null || id.isEmpty()) return ItemStack.EMPTY;
        Identifier loc = Identifier.tryParse(id);
        if (loc == null) return ItemStack.EMPTY;
        return BuiltInRegistries.ITEM.getOptional(loc)
                .filter(i -> i != Items.AIR)
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    private static ItemStack resolveSpawnEgg(String entityId) {
        if (entityId == null) return new ItemStack(Items.SKELETON_SKULL);
        Identifier id = Identifier.tryParse(entityId);
        if (id != null) {
            Identifier eggId = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "_spawn_egg");
            var opt = BuiltInRegistries.ITEM.getOptional(eggId);
            if (opt.isPresent() && opt.get() != Items.AIR) return new ItemStack(opt.get());
        }
        return new ItemStack(Items.SKELETON_SKULL);
    }

    public static void renderHud(GuiGraphicsExtractor ext, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        NixStatsConfig cfg = NixStatsConfig.get();
        if (cfg.hudHidden) return;
        if (cfg.stats == null || cfg.stats.isEmpty()) return;

        float scale = cfg.scale;
        int sw = computeFrameWPx(cfg, mc.font, mc, scale);
        int sh = Math.round(frameH(cfg) * scale);

        int x = cfg.posX < 0 ? ext.guiWidth() - sw - 4 : cfg.posX;
        int y = cfg.posY < 0 ? 4 : cfg.posY;

        StatSidebar.render(ext, x, y, scale, cfg);
    }
}
