package net.lunix.nixstats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NixStatsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path CONFIG_PATH;

    /** Set by the loader entrypoint (Fabric/NeoForge) before load()/save() are used. */
    public static void init(Path configDir) {
        CONFIG_PATH = configDir.resolve("nixstats.json");
    }

    private static NixStatsConfig instance;

    // HUD position (-1 = auto, top-right corner)
    public int posX = -1;
    public int posY = -1;

    // Scale: 0.1 (min) to 3.0 (max)
    public float scale = 1.0f;

    // Text scale relative to HUD scale: 0.5 to 2.0
    public float textScale = 1.0f;

    // Per-column widths (0–20 base units each). The columns stay auto-fit to their
    // content; these tune the breathing room around it.
    //   iconGap  — gap between the row icon and the label      (was the fixed ICON_GAP = 3)
    //   labelPad — padding added to the widest label's width
    //   valuePad — padding added to the widest value's width
    public int iconGap  = 3;
    public int labelPad = 2;
    public int valuePad = 2;

    // Width the middle (label) column takes when *no* row populates it — i.e. every row
    // is a type that sheds its name and the mode is Show None. Default 0 so the column
    // vanishes and the icons sit right against the numbers. As soon as one row does keep
    // a label (a Phantom or advancement row, say), the column auto-fits that instead and
    // this is ignored.
    public int emptyLabelWidth = 0;

    // Shorten large plain counts to 1.5K / 2.3M / 1.1B so the value column stays narrow.
    // Only applies where the displayed number *is* the raw count — distances, times, the
    // phantom clock, advancement progress and tenths-formatted damage keep their own format.
    public boolean abbreviateValues = false;

    // Pre-1.4.2's single knob, which padded the label and value columns together. Kept
    // only to seed the three above on first load; boxed so Gson leaves it null when
    // absent, and nulled after migrating so it drops out of the file on the next save.
    @Deprecated
    private Integer colPad;

    // Sidebar title text
    public String sidebarTitle = "nixStats";

    // Phantom timer colors (ARGB)
    public int colorRested   = 0xFF55FF55;
    public int colorWarning  = 0xFFFFFF55;
    public int colorCritical = 0xFFFF5555;

    // Fraction of PHANTOM_THRESHOLD remaining at which state switches
    public float thresholdWarning  = 0.5f;
    public float thresholdCritical = 0.2f;

    // Multiplayer stat sync interval in seconds (1â€“10)
    public int syncInterval = 5;

    // HUD background/frame opacity, 0.10â€“1.00 (1.0 = fully opaque; text & icons stay opaque)
    public float hudOpacity = 1.0f;

    // Whether the HUD is hidden (toggled by the show/hide keybind)
    public boolean hudHidden = false;

    // How much of each HUD row's label to draw: the full name, the action alone, or
    // nothing at all — the row icon still identifies the subject, and the frame narrows
    // to suit. Display only: tracked stats and their stored labels are untouched.
    public StatNameMode statNameMode = StatNameMode.NAMES;

    // 1.4.1's two-state flag, kept only so an existing config migrates on first load.
    // Boxed so Gson leaves it null when absent; nulled out after migrating, and Gson
    // omits nulls, so it drops out of the file on the next save.
    @Deprecated
    private Boolean hideStatNames;

    // Tracked stats displayed in the sidebar (in order)
    public List<StatEntry> stats = new ArrayList<>();

    public static NixStatsConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(r, NixStatsConfig.class);
                if (instance == null)              instance = defaultConfig();
                if (instance.stats == null)        instance.stats = new ArrayList<>();
                if (instance.sidebarTitle == null) instance.sidebarTitle = "nixStats";
                if (instance.textScale <= 0)       instance.textScale = 1.0f;
                instance.migrateColumnPads();
                instance.migrateStatNameMode();
                instance.hudOpacity = Math.max(0f, Math.min(1f, instance.hudOpacity));
                if (instance.stats.isEmpty())      instance.stats.add(StatEntry.phantom());
            } catch (Exception e) {
                instance = defaultConfig();
            }
        } else {
            instance = defaultConfig();
        }
        save();
    }

    /**
     * Settle the three column pads after a load.
     *
     * <p>A pre-1.4.2 config carries only {@code colPad}, which padded the label and value
     * columns together — seeding both from it leaves every existing HUD exactly the width
     * it was. {@code iconGap} has no old counterpart, so it takes the 3 that used to be
     * hard-coded as {@code StatSidebar.ICON_GAP}.
     */
    @SuppressWarnings("deprecation")
    private void migrateColumnPads() {
        if (colPad != null) {
            labelPad = colPad;
            valuePad = colPad;
            colPad   = null;
        }
        iconGap         = Math.max(0, Math.min(20, iconGap));
        labelPad        = Math.max(0, Math.min(20, labelPad));
        valuePad        = Math.max(0, Math.min(20, valuePad));
        emptyLabelWidth = Math.max(0, Math.min(20, emptyLabelWidth));
    }

    /**
     * Settle {@link #statNameMode} after a load.
     *
     * <p>1.4.1 never wrote a mode, so a file still carrying the old boolean predates the
     * field entirely and that boolean is authoritative: {@code true} meant "action only",
     * which is now {@link StatNameMode#ABBREV}. Keying off the flag rather than off a null
     * mode matters because Gson runs the field initialiser, so an absent mode arrives as
     * NAMES rather than null. Gson does yield null for an unrecognised enum name, hence
     * the second guard.
     */
    @SuppressWarnings("deprecation")
    private void migrateStatNameMode() {
        if (hideStatNames != null) {
            statNameMode  = Boolean.TRUE.equals(hideStatNames) ? StatNameMode.ABBREV : StatNameMode.NAMES;
            hideStatNames = null;
        }
        if (statNameMode == null) statNameMode = StatNameMode.NAMES;
    }

    private static NixStatsConfig defaultConfig() {
        NixStatsConfig c = new NixStatsConfig();
        c.stats.add(StatEntry.phantom());
        return c;
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(instance, w);
            }
        } catch (Exception ignored) {}
    }
}
