package net.lunix.nixstats.screen;

import net.lunix.nixstats.NixStatsConfig;
import net.lunix.nixstats.StatEntry;
import net.lunix.nixstats.StatNameMode;
import net.lunix.nixstats.StatSidebar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class NixStatsConfigScreen extends Screen {

    private static final int[] PRESET_COLORS = {
        0xFF55FF55, 0xFF00AA00, 0xFF55FFFF, 0xFF00AAAA,
        0xFF5555FF, 0xFF0000AA, 0xFFFF55FF, 0xFFAA00AA,
        0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFFFFFFFF
    };

    // Natural design height the layout needs; the whole window scales down to fit
    // when the GUI canvas is shorter than this (i.e. at high GUI scale).
    private static final int DESIGN_MIN_H = 320;

    private static boolean tempLoaded        = false;
    private static String  tempTitle;
    private static float   tempScale;
    private static float   tempTextScale;
    private static int     tempIconGap;
    private static int     tempLabelPad;
    private static int     tempValuePad;
    private static int     tempEmptyLabelWidth;
    private static int     tempSyncInterval;
    private static int     tempColorRested;
    private static int     tempColorWarning;
    private static int     tempColorCritical;
    private static float   tempThresholdWarning;
    private static float   tempThresholdCritical;
    private static float   tempHudOpacity;
    private static StatNameMode tempStatNameMode = StatNameMode.NAMES;
    private static List<StatEntry> tempStats;
    private static int     swatchSelRested;
    private static int     swatchSelWarning;
    private static int     swatchSelCritical;

    private static final String[] COLOR_ROW_LABELS = { "Rested:", "Warning:", "Critical:" };

    private final Screen parent;

    // Layout â€” set each init
    private int panelX, panelW;
    private int boxTop, boxBottom;
    private int titleEditY, topSpinnersY;
    private int colorHeaderY, colorRowsBaseY, botSpinnersY;
    private int statsHeaderY, statsListBaseY, addBtnY, buttonsY;
    private int innerX, innerW;

    // Scale-to-fit: everything is laid out in a virtual canvas (layoutW x layoutH)
    // then rendered through a uniform scale + offset so it always fits the screen.
    private float uiScale = 1f;
    private float uiOffX = 0f, uiOffY = 0f;
    private int layoutW, layoutH;

    // Scroll state â€” instance fields so they survive rebuildWidgets()
    private int statsScrollOffset = 0;
    private int statsScrollTop, statsScrollBottom;
    private int visibleStatRows;

    public NixStatsConfigScreen(Screen parent) {
        super(Component.literal("nixStats Config"));
        this.parent = parent;
    }

    // Real screen coords -> virtual (layout) coords
    private double vx(double rx) { return (rx - uiOffX) / uiScale; }
    private double vy(double ry) { return (ry - uiOffY) / uiScale; }

    @Override
    protected void init() {
        if (!tempLoaded) {
            NixStatsConfig cfg = NixStatsConfig.get();
            tempTitle             = cfg.sidebarTitle != null ? cfg.sidebarTitle : "nixStats";
            tempScale             = cfg.scale;
            tempTextScale         = cfg.textScale > 0 ? cfg.textScale : 1.0f;
            tempIconGap           = Math.max(0, Math.min(20, cfg.iconGap));
            tempLabelPad          = Math.max(0, Math.min(20, cfg.labelPad));
            tempValuePad          = Math.max(0, Math.min(20, cfg.valuePad));
            tempEmptyLabelWidth   = Math.max(0, Math.min(20, cfg.emptyLabelWidth));
            tempSyncInterval      = Math.max(1, Math.min(60, cfg.syncInterval));
            tempColorRested       = cfg.colorRested;
            tempColorWarning      = cfg.colorWarning;
            tempColorCritical     = cfg.colorCritical;
            tempThresholdWarning  = cfg.thresholdWarning;
            tempThresholdCritical = cfg.thresholdCritical;
            tempHudOpacity        = clampF(cfg.hudOpacity, 0f, 1f);
            tempStatNameMode      = cfg.statNameMode != null ? cfg.statNameMode : StatNameMode.NAMES;
            tempStats             = deepCopy(cfg.stats);
            swatchSelRested       = findSwatch(tempColorRested);
            swatchSelWarning      = findSwatch(tempColorWarning);
            swatchSelCritical     = findSwatch(tempColorCritical);
            tempLoaded = true;
        }

        // Compute scale-to-fit. Lay out in a virtual canvas; shrink (never enlarge)
        // so the fixed top section fits even at high GUI scale.
        uiScale = Math.min(1f, (float) this.height / DESIGN_MIN_H);
        layoutW = this.width;
        layoutH = uiScale < 1f ? DESIGN_MIN_H : this.height;
        uiOffX = (this.width - this.width * uiScale) / 2f;
        uiOffY = 0f;

        panelW = Math.min(layoutW - 16, Math.max(240, layoutW / 3));
        panelX = Math.max(4, (layoutW - panelW) / 2);
        innerX = panelX + 6;
        innerW = panelW - 12;

        // Panel anchored near top; bottom buttons anchored to canvas bottom
        int panelY = 8;
        boxTop  = panelY;
        buttonsY = layoutH - 24;

        int y = panelY + 5;

        titleEditY       = y; y += 22;
        topSpinnersY     = y; y += 26;
        colorHeaderY     = y; y += 14;
        colorRowsBaseY   = y; y += 18;
        botSpinnersY     = y; y += 26;
        y += 10;                      // blank line above the Names toggle
        statsHeaderY     = y; y += 14;
        y += 10;                      // blank line below it, before the stats list
        statsListBaseY   = y;

        // Stats scroll area fills remaining space above the bottom buttons.
        // Layout from bottom: buttonsY -> 4px gap -> boxBottom -> 3px gap -> addBtn(16px) -> 2px gap -> scrollBottom
        statsScrollTop    = statsListBaseY;
        int desiredBottom = buttonsY - 4;
        // addBtnArea = 2(gap) + 16(btn) + 3(gap) = 21
        int rawScrollH    = desiredBottom - 21 - statsScrollTop;
        visibleStatRows   = Math.max(1, rawScrollH / 16);
        statsScrollBottom = statsScrollTop + visibleStatRows * 16;
        addBtnY           = statsScrollBottom + 2;
        boxBottom         = addBtnY + 16 + 3;
        // Push buttons down if panel ended up taller than expected (tiny screens)
        buttonsY          = Math.max(boxBottom + 4, layoutH - 24);

        // Clamp scroll offset after a stat removal or screen resize
        int n = tempStats != null ? tempStats.size() : 0;
        statsScrollOffset = Math.max(0, Math.min(Math.max(0, n - visibleStatRows), statsScrollOffset));

        // â”€â”€ Widgets (fixed-position sections only; stat rows handled manually) â”€â”€

        EditBox titleBox = new EditBox(font, innerX, titleEditY, innerW, 16,
                Component.literal("Sidebar Title"));
        titleBox.setMaxLength(32);
        titleBox.setHint(Component.literal("Sidebar Title..."));
        titleBox.setValue(tempTitle);
        titleBox.setResponder(t -> tempTitle = t);
        addRenderableWidget(titleBox);

        // Scale/Text/Pad/Sync (top) and Warning/Critical (threshold) render as custom
        // up/down spinners â€” see drawSpinnerRow() / spinnerRowClick().

        // Name mode - left-aligned, with a blank line above for breathing room. Cycles
        // Names -> Abbrev -> None, and the caption is always the mode now in effect.
        // Affects the HUD (and the live preview) only; the list below always shows full
        // labels so rows stay tellable apart while configuring.
        addRenderableWidget(Button.builder(namesButtonLabel(), b -> {
            tempStatNameMode = tempStatNameMode.next();
            b.setMessage(namesButtonLabel());
        }).bounds(innerX, statsHeaderY - 1, 84, 14).build());

        // + Add Stat (below scroll area, above bottom buttons)
        addRenderableWidget(Button.builder(Component.literal("+ Add Stat"), btn ->
            minecraft.setScreen(new StatPickerScreen(this, entry -> {
                if (tempStats == null) tempStats = new ArrayList<>();
                tempStats.add(entry);
            }))
        ).bounds(innerX, addBtnY, innerW, 16).build());

        // Bottom buttons
        int bx = panelX;
        addRenderableWidget(Button.builder(Component.literal("Set Position"), btn -> {
            applyToConfig();
            NixStatsConfig.save();
            minecraft.setScreen(new PositionScreen(this));
        }).bounds(bx, buttonsY, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Save"), btn -> {
            applyToConfig();
            NixStatsConfig.save();
            tempLoaded = false;
            minecraft.setScreen(parent);
        }).bounds(bx + 110, buttonsY, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> {
            tempLoaded = false;
            minecraft.setScreen(parent);
        }).bounds(bx + 180, buttonsY, 60, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        // Dim backdrop in real screen space (covers the whole screen, unscaled)
        g.fill(0, 0, this.width, this.height, 0xC0101010);

        // Virtual mouse coords for hover/hit-testing inside the scaled panel
        int vMouseX = (int) vx(mouseX);
        int vMouseY = (int) vy(mouseY);

        g.pose().pushMatrix();
        g.pose().translate(uiOffX, uiOffY);
        g.pose().scale(uiScale, uiScale);

        g.centeredText(font, title, layoutW / 2, 4, 0xFFFFFF);

        // Main box
        g.fill(panelX - 1, boxTop - 1, panelX + panelW + 1, boxBottom + 1, 0xFF555555);
        g.fill(panelX,     boxTop,     panelX + panelW,     boxBottom,     0xFF1E1E1E);

        // Top settings row — the six sizing spinners (Scale / Text / IGap / LPad / VPad / ECol)
        drawSpinnerRow(g, 0, 6, topSpinnersY, vMouseX, vMouseY);

        // Phantom colors section header
        g.centeredText(font, Component.literal("â”€ Phantom Colors â”€"),
                panelX + panelW / 2, colorHeaderY + 2, 0x888888);

        // Compact color line: label + current-color swatch per role (click to cycle presets)
        int[] rowColors = { tempColorRested, tempColorWarning, tempColorCritical };
        for (int row = 0; row < 3; row++) {
            int cellX = innerX + row * (innerW / 3);
            g.text(font, Component.literal(COLOR_ROW_LABELS[row]), cellX, colorRowsBaseY + 3, 0xFFCCCCCC);
            int swX = cellX + font.width(COLOR_ROW_LABELS[row]) + 4;
            int swY = colorRowsBaseY + 1;
            g.fill(swX - 1, swY - 1, swX + 13, swY + 13, 0xFFAAAAAA);
            g.fill(swX, swY, swX + 12, swY + 12, rowColors[row]);
        }

        // Bottom row — Sync / Warning / Critical / Opacity up-down spinners
        drawSpinnerRow(g, 6, 4, botSpinnersY, vMouseX, vMouseY);

        // Stats section header
        g.centeredText(font, Component.literal("â”€ Stats â”€"),
                panelX + panelW / 2, statsHeaderY + 2, 0x888888);

        // Scroll area background
        g.fill(innerX - 2, statsScrollTop, innerX + innerW + 2, statsScrollBottom, 0xFF181818);

        // Stat rows â€” only the visible slice, rendered as manual fake buttons
        NixStatsConfig tmp = buildTempConfig();
        if (tempStats != null && !tempStats.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            int n = tempStats.size();

            for (int rel = 0; rel < visibleStatRows; rel++) {
                int i = statsScrollOffset + rel;
                if (i >= n) break;
                StatEntry entry = tempStats.get(i);
                int rowY = statsScrollTop + rel * 16;

                // â†‘ fake button
                boolean upHov = vMouseX >= innerX && vMouseX < innerX + 14
                             && vMouseY >= rowY  && vMouseY < rowY + 14;
                g.fill(innerX, rowY, innerX + 14, rowY + 14, upHov ? 0xFF666666 : 0xFF3A3A3A);
                drawUpArrow(g, innerX + 7, rowY + 7, i > 0 ? 0xFFFFFFFF : 0xFF555555);

                // â†“ fake button
                boolean downHov = vMouseX >= innerX + 16 && vMouseX < innerX + 30
                               && vMouseY >= rowY      && vMouseY < rowY + 14;
                g.fill(innerX + 16, rowY, innerX + 30, rowY + 14, downHov ? 0xFF666666 : 0xFF3A3A3A);
                drawDownArrow(g, innerX + 23, rowY + 7, i < n - 1 ? 0xFFFFFFFF : 0xFF555555);

                // Ã— fake button
                boolean xHov = vMouseX >= innerX + innerW - 14 && vMouseX < innerX + innerW
                            && vMouseY >= rowY             && vMouseY < rowY + 14;
                g.fill(innerX + innerW - 14, rowY, innerX + innerW, rowY + 14,
                        xHov ? 0xFF883333 : 0xFF3A3A3A);
                drawX(g, innerX + innerW - 7, rowY + 7, 0xFFFF5555);

                // Icon (10Ã—10 scaled from 16Ã—16)
                ItemStack icon = StatSidebar.getIcon(entry);
                int iconX = innerX + 32;
                if (!icon.isEmpty()) {
                    g.pose().pushMatrix();
                    g.pose().translate(iconX, rowY + 3);
                    g.pose().scale(10f / 16f, 10f / 16f);
                    g.item(icon, 0, 0);
                    g.pose().popMatrix();
                }

                // Value (right-aligned, before Ã—)
                int rawValue = StatSidebar.readStatValue(entry, mc);
                String valStr = StatSidebar.formatValue(entry, rawValue);
                int valColor  = StatSidebar.getValueColor(entry, rawValue, tmp);
                int valW = font.width(valStr);
                int valX = innerX + innerW - 16 - valW;
                g.text(font, valStr, valX, rowY + 4, valColor);

                // Label (truncated to fit between icon and value)
                int labelX    = iconX + 12;
                int maxLabelW = valX - labelX - 4;
                String label  = truncateLabel(entry.label, maxLabelW);
                g.text(font, label, labelX, rowY + 4, 0xFFCCCCCC);
            }

            // Scrollbar (right of panel, only when needed)
            if (n > visibleStatRows) {
                int sbX    = panelX + panelW + 2;
                int sbH    = visibleStatRows * 16;
                int thumbH = Math.max(8, sbH * visibleStatRows / n);
                int maxOff = n - visibleStatRows;
                int thumbY = statsScrollTop + (maxOff > 0 ? (sbH - thumbH) * statsScrollOffset / maxOff : 0);
                g.fill(sbX, statsScrollTop, sbX + 4, statsScrollTop + sbH, 0xFF444444);
                g.fill(sbX, thumbY, sbX + 4, thumbY + thumbH, 0xFFAAAAAA);
            }
        }

        // Thin separator lines around scroll area
        g.fill(innerX - 2, statsScrollTop - 1,    innerX + innerW + 2, statsScrollTop,     0xFF444444);
        g.fill(innerX - 2, statsScrollBottom,      innerX + innerW + 2, statsScrollBottom + 1, 0xFF444444);

        // Live preview â€” anchored to right edge, vertically centered
        int previewW = StatSidebar.computeFrameWPx(tmp, minecraft.font, minecraft, tempScale);
        int previewH = Math.round(StatSidebar.frameH(tmp) * tempScale);
        int previewX = layoutW - previewW - 8;
        if (previewX > panelX + panelW + 8) {
            int previewY = Math.max(4, (layoutH - previewH) / 2);
            g.text(font, "Preview:", previewX, previewY - 12, 0xAAAAAA);
            StatSidebar.render(g, previewX, previewY, tempScale, tmp);
        }

        super.extractRenderState(g, vMouseX, vMouseY, partialTick);

        g.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        double mouseX = vx(event.x()), mouseY = vy(event.y());
        MouseButtonEvent translated = new MouseButtonEvent(mouseX, mouseY, event.buttonInfo());

        // Compact color swatches: left-click = next preset, right-click = previous
        if (!consumed && (event.button() == 0 || event.button() == 1)) {
            for (int row = 0; row < 3; row++) {
                int cellX = innerX + row * (innerW / 3);
                int swX = cellX + font.width(COLOR_ROW_LABELS[row]) + 4;
                int swY = colorRowsBaseY + 1;
                if (mouseX >= swX && mouseX < swX + 12 && mouseY >= swY && mouseY < swY + 12) {
                    cycleColor(row, event.button() == 0 ? 1 : -1);
                    return true;
                }
            }
        }

        // Up/down spinners (top settings row + threshold row)
        if (!consumed && event.button() == 0) {
            if (spinnerRowClick(mouseX, mouseY, 0, 6, topSpinnersY)) return true;
            if (spinnerRowClick(mouseX, mouseY, 6, 4, botSpinnersY)) return true;
        }

        if (!consumed && event.button() == 0) {
            // Stat row â†‘â†“Ã— (manual hit-testing against visible rows)
            if (tempStats != null) {
                int n = tempStats.size();
                for (int rel = 0; rel < visibleStatRows; rel++) {
                    int i = statsScrollOffset + rel;
                    if (i >= n) break;
                    int rowY = statsScrollTop + rel * 16;
                    if (mouseY >= rowY && mouseY < rowY + 14) {
                        if (mouseX >= innerX && mouseX < innerX + 14) {
                            if (i > 0) {
                                StatEntry tmp = tempStats.remove(i);
                                tempStats.add(i - 1, tmp);
                                statsScrollOffset = Math.max(0, Math.min(
                                        Math.max(0, tempStats.size() - visibleStatRows), statsScrollOffset));
                                rebuildWidgets();
                            }
                            return true;
                        }
                        if (mouseX >= innerX + 16 && mouseX < innerX + 30) {
                            if (i < n - 1) {
                                StatEntry tmp = tempStats.remove(i);
                                tempStats.add(i + 1, tmp);
                                rebuildWidgets();
                            }
                            return true;
                        }
                        if (mouseX >= innerX + innerW - 14 && mouseX < innerX + innerW) {
                            tempStats.remove(i);
                            statsScrollOffset = Math.max(0, Math.min(
                                    Math.max(0, tempStats.size() - visibleStatRows), statsScrollOffset));
                            rebuildWidgets();
                            return true;
                        }
                    }
                }
            }
        }
        return super.mouseClicked(translated, consumed);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        MouseButtonEvent translated = new MouseButtonEvent(vx(event.x()), vy(event.y()), event.buttonInfo());
        return super.mouseDragged(translated, dragX / uiScale, dragY / uiScale);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        MouseButtonEvent translated = new MouseButtonEvent(vx(event.x()), vy(event.y()), event.buttonInfo());
        return super.mouseReleased(translated);
    }

    @Override
    public void mouseMoved(double mx, double my) {
        super.mouseMoved(vx(mx), vy(my));
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        int n = tempStats != null ? tempStats.size() : 0;
        int maxScroll = Math.max(0, n - visibleStatRows);
        if (maxScroll > 0) {
            statsScrollOffset = Math.max(0, Math.min(maxScroll,
                    statsScrollOffset - (int) Math.signum(scrollY)));
            return true;
        }
        return super.mouseScrolled(vx(mx), vy(my), scrollX, scrollY);
    }

    private void cycleColor(int row, int dir) {
        int sel = switch (row) {
            case 0 -> swatchSelRested;
            case 1 -> swatchSelWarning;
            default -> swatchSelCritical;
        };
        int next = sel < 0 ? (dir > 0 ? 0 : PRESET_COLORS.length - 1)
                           : (sel + dir + PRESET_COLORS.length) % PRESET_COLORS.length;
        int color = PRESET_COLORS[next];
        switch (row) {
            case 0 -> { tempColorRested   = color; swatchSelRested   = next; }
            case 1 -> { tempColorWarning  = color; swatchSelWarning  = next; }
            case 2 -> { tempColorCritical = color; swatchSelCritical = next; }
        }
    }

    private static final String[] SPIN_LABELS = { "Scale", "Text", "IGap", "LPad", "VPad", "ECol", "Sync", "Warn", "Crit", "Opac" };

    /** Draw a row of n up/down spinners starting at spinner id {@code startId}. */
    private void drawSpinnerRow(GuiGraphicsExtractor g, int startId, int n, int rowY, int vmx, int vmy) {
        int cellW = innerW / n;
        int ctrlY = rowY + 10;
        for (int c = 0; c < n; c++) {
            int id = startId + c;
            int cellX = innerX + c * cellW;
            int ax = cellX;   // arrows on the LEFT, value to their right
            g.text(font, Component.literal(SPIN_LABELS[id]), cellX, rowY, 0xFFCCCCCC);
            boolean upHov = vmx >= ax && vmx < ax + 9 && vmy >= ctrlY - 1 && vmy < ctrlY + 7;
            boolean dnHov = vmx >= ax && vmx < ax + 9 && vmy >= ctrlY + 7 && vmy < ctrlY + 15;
            g.fill(ax, ctrlY - 1, ax + 9, ctrlY + 7,  upHov ? 0xFF666666 : 0xFF3A3A3A);
            g.fill(ax, ctrlY + 7, ax + 9, ctrlY + 15, dnHov ? 0xFF666666 : 0xFF3A3A3A);
            drawUpArrow(g, ax + 4, ctrlY + 3, 0xFFFFFFFF);
            drawDownArrow(g, ax + 4, ctrlY + 11, 0xFFFFFFFF);
            g.text(font, Component.literal(spinValue(id)), ax + 13, ctrlY + 1, 0xFFFFFFFF);
        }
    }

    private boolean spinnerRowClick(double mx, double my, int startId, int n, int rowY) {
        int cellW = innerW / n;
        int ctrlY = rowY + 10;
        for (int c = 0; c < n; c++) {
            int ax = innerX + c * cellW;
            if (mx >= ax && mx < ax + 9) {
                if (my >= ctrlY - 1 && my < ctrlY + 7)  { spinAdjust(startId + c, 1);  return true; }
                if (my >= ctrlY + 7 && my < ctrlY + 15) { spinAdjust(startId + c, -1); return true; }
            }
        }
        return false;
    }

    private String spinValue(int id) {
        return switch (id) {
            case 0 -> String.format("%.1fx", tempScale);
            case 1 -> String.format("%.1fx", tempTextScale);
            case 2 -> String.valueOf(tempIconGap);
            case 3 -> String.valueOf(tempLabelPad);
            case 4 -> String.valueOf(tempValuePad);
            case 5 -> String.valueOf(tempEmptyLabelWidth);
            case 6 -> tempSyncInterval + "s";
            case 7 -> Math.round(tempThresholdWarning * 100) + "%";
            case 8 -> Math.round(tempThresholdCritical * 100) + "%";
            default -> Math.round(tempHudOpacity * 100) + "%";
        };
    }

    private void spinAdjust(int id, int dir) {
        switch (id) {
            case 0 -> tempScale        = clampF(Math.round((tempScale + dir * 0.1f) * 10) / 10f, 0.1f, 3.0f);
            case 1 -> tempTextScale    = clampF(Math.round((tempTextScale + dir * 0.1f) * 10) / 10f, 0.5f, 2.0f);
            case 2 -> tempIconGap         = (int) clampF(tempIconGap  + dir, 0, 20);
            case 3 -> tempLabelPad        = (int) clampF(tempLabelPad + dir, 0, 20);
            case 4 -> tempValuePad        = (int) clampF(tempValuePad + dir, 0, 20);
            case 5 -> tempEmptyLabelWidth = (int) clampF(tempEmptyLabelWidth + dir, 0, 20);
            case 6 -> tempSyncInterval    = (int) clampF(tempSyncInterval + dir, 1, 60);
            case 7 -> tempThresholdWarning  = clampF(Math.round((tempThresholdWarning + dir * 0.05f) * 20) / 20f, 0f, 1f);
            case 8 -> tempThresholdCritical = clampF(Math.round((tempThresholdCritical + dir * 0.05f) * 20) / 20f, 0f, 1f);
            case 9 -> tempHudOpacity        = clampF(Math.round((tempHudOpacity + dir * 0.05f) * 20) / 20f, 0f, 1f);
        }
    }

    private static float clampF(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }

    // Arrows/Ã— drawn as pixel triangles rather than font glyphs â€” renders identically on every
    // MC version and loader (some versions rasterize the â†‘/â†“/Ã— glyphs oddly). Centered on (cx, cy).
    private static void drawUpArrow(GuiGraphicsExtractor g, int cx, int cy, int color) {
        for (int r = 0; r < 3; r++) g.fill(cx - r, cy - 1 + r, cx + r + 1, cy + r, color);
    }
    private static void drawDownArrow(GuiGraphicsExtractor g, int cx, int cy, int color) {
        for (int r = 0; r < 3; r++) g.fill(cx - (2 - r), cy - 1 + r, cx + (2 - r) + 1, cy + r, color);
    }
    private static void drawX(GuiGraphicsExtractor g, int cx, int cy, int color) {
        for (int i = -2; i <= 2; i++) {
            g.fill(cx + i, cy + i, cx + i + 1, cy + i + 1, color);
            g.fill(cx + i, cy - i, cx + i + 1, cy - i + 1, color);
        }
    }

    private void applyToConfig() {
        NixStatsConfig cfg = NixStatsConfig.get();
        cfg.sidebarTitle       = tempTitle != null ? tempTitle : "nixStats";
        cfg.scale              = tempScale;
        cfg.textScale          = tempTextScale;
        cfg.iconGap            = tempIconGap;
        cfg.labelPad           = tempLabelPad;
        cfg.valuePad           = tempValuePad;
        cfg.emptyLabelWidth    = tempEmptyLabelWidth;
        cfg.syncInterval       = tempSyncInterval;
        cfg.colorRested        = tempColorRested;
        cfg.colorWarning       = tempColorWarning;
        cfg.colorCritical      = tempColorCritical;
        cfg.thresholdWarning   = tempThresholdWarning;
        cfg.thresholdCritical  = tempThresholdCritical;
        cfg.hudOpacity         = tempHudOpacity;
        cfg.statNameMode       = tempStatNameMode;
        cfg.stats              = deepCopy(tempStats);
    }

    private static Component namesButtonLabel() {
        return Component.literal(tempStatNameMode.caption());
    }

    private NixStatsConfig buildTempConfig() {
        NixStatsConfig tmp = new NixStatsConfig();
        tmp.sidebarTitle      = tempTitle != null ? tempTitle : "nixStats";
        tmp.scale             = tempScale;
        tmp.textScale         = tempTextScale;
        tmp.iconGap           = tempIconGap;
        tmp.labelPad          = tempLabelPad;
        tmp.valuePad          = tempValuePad;
        tmp.emptyLabelWidth   = tempEmptyLabelWidth;
        tmp.syncInterval      = tempSyncInterval;
        tmp.colorRested       = tempColorRested;
        tmp.colorWarning      = tempColorWarning;
        tmp.colorCritical     = tempColorCritical;
        tmp.thresholdWarning  = tempThresholdWarning;
        tmp.thresholdCritical = tempThresholdCritical;
        tmp.hudOpacity        = tempHudOpacity;
        tmp.statNameMode      = tempStatNameMode;
        tmp.stats             = deepCopy(tempStats);
        return tmp;
    }

    private static List<StatEntry> deepCopy(List<StatEntry> src) {
        if (src == null) return new ArrayList<>();
        List<StatEntry> copy = new ArrayList<>();
        for (StatEntry e : src) copy.add(new StatEntry(e.statType, e.targetId, e.label));
        return copy;
    }

    private static int findSwatch(int color) {
        for (int i = 0; i < PRESET_COLORS.length; i++)
            if (PRESET_COLORS[i] == color) return i;
        return -1;
    }

    private String truncateLabel(String label, int maxPx) {
        if (label == null || maxPx <= 0) return "";
        if (font.width(label) <= maxPx) return label;
        String t = label;
        while (!t.isEmpty() && font.width(t + "..") > maxPx)
            t = t.substring(0, t.length() - 1);
        return t + "..";
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
