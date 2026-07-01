package net.lunix.nixstats.screen;

import net.lunix.nixstats.NixStatsConfig;
import net.lunix.nixstats.StatEntry;
import net.lunix.nixstats.StatSidebar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
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
    private static final int DESIGN_MIN_H = 430;

    private static boolean tempLoaded        = false;
    private static String  tempTitle;
    private static float   tempScale;
    private static float   tempTextScale;
    private static int     tempColPad;
    private static int     tempSyncInterval;
    private static int     tempColorRested;
    private static int     tempColorWarning;
    private static int     tempColorCritical;
    private static float   tempThresholdWarning;
    private static float   tempThresholdCritical;
    private static List<StatEntry> tempStats;
    private static int     swatchSelRested;
    private static int     swatchSelWarning;
    private static int     swatchSelCritical;

    private static final String[] COLOR_ROW_LABELS = { "Rested:", "Warning:", "Critical:" };

    private final Screen parent;

    // Layout — set each init
    private int panelX, panelW;
    private int boxTop, boxBottom;
    private int titleEditY, scaleSliderY, textScaleSliderY, colPadSliderY, syncSliderY;
    private int colorHeaderY, colorRowsBaseY, warnSliderY, critSliderY;
    private int statsHeaderY, statsListBaseY, addBtnY, buttonsY;
    private int innerX, innerW;

    // Scale-to-fit: everything is laid out in a virtual canvas (layoutW x layoutH)
    // then rendered through a uniform scale + offset so it always fits the screen.
    private float uiScale = 1f;
    private float uiOffX = 0f, uiOffY = 0f;
    private int layoutW, layoutH;

    // Scroll state — instance fields so they survive rebuildWidgets()
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
            tempColPad            = Math.max(0, Math.min(20, cfg.colPad));
            tempSyncInterval      = Math.max(1, Math.min(60, cfg.syncInterval));
            tempColorRested       = cfg.colorRested;
            tempColorWarning      = cfg.colorWarning;
            tempColorCritical     = cfg.colorCritical;
            tempThresholdWarning  = cfg.thresholdWarning;
            tempThresholdCritical = cfg.thresholdCritical;
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
        scaleSliderY     = y; y += 19;
        textScaleSliderY = y; y += 19;
        colPadSliderY    = y; y += 19;
        syncSliderY      = y; y += 24;
        colorHeaderY     = y; y += 14;
        colorRowsBaseY   = y; y += 78;
        warnSliderY      = y; y += 19;
        critSliderY      = y; y += 24;
        statsHeaderY     = y; y += 14;
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

        // ── Widgets (fixed-position sections only; stat rows handled manually) ──

        EditBox titleBox = new EditBox(font, innerX, titleEditY, innerW, 16,
                Component.literal("Sidebar Title"));
        titleBox.setMaxLength(32);
        titleBox.setHint(Component.literal("Sidebar Title..."));
        titleBox.setValue(tempTitle);
        titleBox.setResponder(t -> tempTitle = t);
        addRenderableWidget(titleBox);

        double scaleVal = (tempScale - 0.1) / 2.9;
        addRenderableWidget(new AbstractSliderButton(innerX, scaleSliderY, innerW, 16,
                Component.literal("Scale: " + String.format("%.1f", (double) tempScale) + "x"), scaleVal) {
            @Override protected void updateMessage() {
                setMessage(Component.literal("Scale: " + String.format("%.2f", 0.1 + this.value * 2.9) + "x"));
            }
            @Override protected void applyValue() { tempScale = (float)(0.1 + this.value * 2.9); }
        });

        double textScaleVal = (tempTextScale - 0.5) / 1.5;
        addRenderableWidget(new AbstractSliderButton(innerX, textScaleSliderY, innerW, 16,
                Component.literal("Text: " + String.format("%.1f", (double) tempTextScale) + "x"), textScaleVal) {
            @Override protected void updateMessage() {
                setMessage(Component.literal("Text: " + String.format("%.2f", 0.5 + this.value * 1.5) + "x"));
            }
            @Override protected void applyValue() { tempTextScale = (float)(0.5 + this.value * 1.5); }
        });

        double colPadVal = tempColPad / 20.0;
        addRenderableWidget(new AbstractSliderButton(innerX, colPadSliderY, innerW, 16,
                Component.literal("Col Pad: " + tempColPad), colPadVal) {
            @Override protected void updateMessage() {
                setMessage(Component.literal("Col Pad: " + (int) Math.round(this.value * 20)));
            }
            @Override protected void applyValue() { tempColPad = (int) Math.round(this.value * 20); }
        });

        double syncVal = (tempSyncInterval - 1) / 59.0;
        addRenderableWidget(new AbstractSliderButton(innerX, syncSliderY, innerW, 16,
                Component.literal("Sync: Every " + tempSyncInterval + "s"), syncVal) {
            @Override protected void updateMessage() {
                setMessage(Component.literal("Sync: Every " + (1 + (int) Math.round(this.value * 59)) + "s"));
            }
            @Override protected void applyValue() { tempSyncInterval = 1 + (int) Math.round(this.value * 59); }
        });

        addRenderableWidget(new AbstractSliderButton(innerX, warnSliderY, innerW, 16,
                Component.literal("Warning: " + Math.round(tempThresholdWarning * 100) + "%"),
                tempThresholdWarning) {
            @Override protected void updateMessage() {
                setMessage(Component.literal("Warning: " + Math.round(this.value * 100) + "%"));
            }
            @Override protected void applyValue() { tempThresholdWarning = (float) this.value; }
        });

        addRenderableWidget(new AbstractSliderButton(innerX, critSliderY, innerW, 16,
                Component.literal("Critical: " + Math.round(tempThresholdCritical * 100) + "%"),
                tempThresholdCritical) {
            @Override protected void updateMessage() {
                setMessage(Component.literal("Critical: " + Math.round(this.value * 100) + "%"));
            }
            @Override protected void applyValue() { tempThresholdCritical = (float) this.value; }
        });

        // + Add Stat (below scroll area, above bottom buttons)
        addRenderableWidget(Button.builder(Component.literal("+ Add Stat"), btn ->
            minecraft.setScreenAndShow(new StatPickerScreen(this, entry -> {
                if (tempStats == null) tempStats = new ArrayList<>();
                tempStats.add(entry);
            }))
        ).bounds(innerX, addBtnY, innerW, 16).build());

        // Bottom buttons
        int bx = panelX;
        addRenderableWidget(Button.builder(Component.literal("Set Position"), btn -> {
            applyToConfig();
            NixStatsConfig.save();
            minecraft.setScreenAndShow(new PositionScreen(this));
        }).bounds(bx, buttonsY, 100, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Save"), btn -> {
            applyToConfig();
            NixStatsConfig.save();
            tempLoaded = false;
            minecraft.setScreenAndShow(parent);
        }).bounds(bx + 110, buttonsY, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> {
            tempLoaded = false;
            minecraft.setScreenAndShow(parent);
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

        // Phantom colors section header
        g.centeredText(font, Component.literal("─ Phantom Colors ─"),
                panelX + panelW / 2, colorHeaderY + 2, 0x888888);

        // Color swatches
        int[] rowColors = { tempColorRested, tempColorWarning, tempColorCritical };
        int[] rowSels   = { swatchSelRested, swatchSelWarning, swatchSelCritical };

        for (int row = 0; row < 3; row++) {
            int labelY  = colorRowsBaseY + row * 26;
            int swatchY = labelY + 12;
            for (int i = 0; i < PRESET_COLORS.length; i++) {
                int sx = innerX + i * 13;
                g.fill(sx - 1, swatchY - 1, sx + 11, swatchY + 11,
                        rowSels[row] == i ? 0xFFFFFFFF : 0xFF444444);
                g.fill(sx, swatchY, sx + 10, swatchY + 10, PRESET_COLORS[i]);
            }
            if (rowSels[row] == -1) {
                int cx = innerX + PRESET_COLORS.length * 13 + 2;
                g.fill(cx - 1, swatchY - 1, cx + 11, swatchY + 11, 0xFFFFFFFF);
                g.fill(cx, swatchY, cx + 10, swatchY + 10, rowColors[row]);
            }
        }

        // Stats section header
        g.centeredText(font, Component.literal("─ Stats ─"),
                panelX + panelW / 2, statsHeaderY + 2, 0x888888);

        // Scroll area background
        g.fill(innerX - 2, statsScrollTop, innerX + innerW + 2, statsScrollBottom, 0xFF181818);

        // Stat rows — only the visible slice, rendered as manual fake buttons
        NixStatsConfig tmp = buildTempConfig();
        if (tempStats != null && !tempStats.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            int n = tempStats.size();

            for (int rel = 0; rel < visibleStatRows; rel++) {
                int i = statsScrollOffset + rel;
                if (i >= n) break;
                StatEntry entry = tempStats.get(i);
                int rowY = statsScrollTop + rel * 16;

                // ↑ fake button
                boolean upHov = vMouseX >= innerX && vMouseX < innerX + 14
                             && vMouseY >= rowY  && vMouseY < rowY + 14;
                g.fill(innerX, rowY, innerX + 14, rowY + 14, upHov ? 0xFF666666 : 0xFF3A3A3A);
                g.centeredText(font, Component.literal("↑"), innerX + 7, rowY + 3,
                        i > 0 ? 0xFFFFFFFF : 0xFF555555);

                // ↓ fake button
                boolean downHov = vMouseX >= innerX + 16 && vMouseX < innerX + 30
                               && vMouseY >= rowY      && vMouseY < rowY + 14;
                g.fill(innerX + 16, rowY, innerX + 30, rowY + 14, downHov ? 0xFF666666 : 0xFF3A3A3A);
                g.centeredText(font, Component.literal("↓"), innerX + 23, rowY + 3,
                        i < n - 1 ? 0xFFFFFFFF : 0xFF555555);

                // × fake button
                boolean xHov = vMouseX >= innerX + innerW - 14 && vMouseX < innerX + innerW
                            && vMouseY >= rowY             && vMouseY < rowY + 14;
                g.fill(innerX + innerW - 14, rowY, innerX + innerW, rowY + 14,
                        xHov ? 0xFF883333 : 0xFF3A3A3A);
                g.centeredText(font, Component.literal("×"), innerX + innerW - 7, rowY + 3, 0xFFFF5555);

                // Icon (10×10 scaled from 16×16)
                ItemStack icon = StatSidebar.getIcon(entry);
                int iconX = innerX + 32;
                if (!icon.isEmpty()) {
                    g.pose().pushMatrix();
                    g.pose().translate(iconX, rowY + 3);
                    g.pose().scale(10f / 16f, 10f / 16f);
                    g.item(icon, 0, 0);
                    g.pose().popMatrix();
                }

                // Value (right-aligned, before ×)
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

        // Live preview — anchored to right edge, vertically centered
        int previewW = StatSidebar.computeFrameWPx(tmp, minecraft.font, minecraft, tempScale);
        int previewH = Math.round(StatSidebar.frameH(tmp) * tempScale);
        int previewX = layoutW - previewW - 8;
        if (previewX > panelX + panelW + 8) {
            int previewY = Math.max(4, (layoutH - previewH) / 2);
            g.text(font, "Preview:", previewX, previewY - 12, 0xAAAAAA);
            StatSidebar.render(g, previewX, previewY, tempScale, tmp);
        }

        super.extractRenderState(g, vMouseX, vMouseY, partialTick);

        // Color row labels drawn last — on top of swatches
        for (int row = 0; row < 3; row++) {
            int labelY = colorRowsBaseY + row * 26;
            g.text(font, Component.literal(COLOR_ROW_LABELS[row]), innerX, labelY, 0xFFCCCCCC);
        }

        g.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean consumed) {
        double mouseX = vx(event.x()), mouseY = vy(event.y());
        MouseButtonEvent translated = new MouseButtonEvent(mouseX, mouseY, event.buttonInfo());

        if (!consumed && event.button() == 0) {
            // Color swatches
            for (int row = 0; row < 3; row++) {
                int swatchY = colorRowsBaseY + row * 26 + 12;
                for (int i = 0; i < PRESET_COLORS.length; i++) {
                    int sx = innerX + i * 13;
                    if (mouseX >= sx && mouseX < sx + 10 && mouseY >= swatchY && mouseY < swatchY + 10) {
                        applySwatchClick(row, i);
                        return true;
                    }
                }
            }

            // Stat row ↑↓× (manual hit-testing against visible rows)
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

    private void applySwatchClick(int row, int idx) {
        int color = PRESET_COLORS[idx];
        switch (row) {
            case 0 -> { tempColorRested   = color; swatchSelRested   = idx; }
            case 1 -> { tempColorWarning  = color; swatchSelWarning  = idx; }
            case 2 -> { tempColorCritical = color; swatchSelCritical = idx; }
        }
    }

    private void applyToConfig() {
        NixStatsConfig cfg = NixStatsConfig.get();
        cfg.sidebarTitle       = tempTitle != null ? tempTitle : "nixStats";
        cfg.scale              = tempScale;
        cfg.textScale          = tempTextScale;
        cfg.colPad             = tempColPad;
        cfg.syncInterval       = tempSyncInterval;
        cfg.colorRested        = tempColorRested;
        cfg.colorWarning       = tempColorWarning;
        cfg.colorCritical      = tempColorCritical;
        cfg.thresholdWarning   = tempThresholdWarning;
        cfg.thresholdCritical  = tempThresholdCritical;
        cfg.stats              = deepCopy(tempStats);
    }

    private NixStatsConfig buildTempConfig() {
        NixStatsConfig tmp = new NixStatsConfig();
        tmp.sidebarTitle      = tempTitle != null ? tempTitle : "nixStats";
        tmp.scale             = tempScale;
        tmp.textScale         = tempTextScale;
        tmp.colPad            = tempColPad;
        tmp.syncInterval      = tempSyncInterval;
        tmp.colorRested       = tempColorRested;
        tmp.colorWarning      = tempColorWarning;
        tmp.colorCritical     = tempColorCritical;
        tmp.thresholdWarning  = tempThresholdWarning;
        tmp.thresholdCritical = tempThresholdCritical;
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
