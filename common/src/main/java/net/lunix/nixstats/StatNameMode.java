package net.lunix.nixstats;

/**
 * How much of a HUD row's label to draw. Cycled by the config screen's one button,
 * whose caption is always the mode currently in effect.
 *
 * <p>Only the eight item/block/entity stat types are affected — see
 * {@link StatEntry#displayLabel}. The rest keep their full label in every mode.
 */
public enum StatNameMode {

    /** Full stored label, e.g. "Stone Mined". */
    NAMES("Show Names"),

    /** Action only, e.g. "Mined" — the icon carries the subject. */
    ABBREV("Show Abbrev"),

    /**
     * No label at all: icon and number only. Listing the same block under several
     * categories is then indistinguishable — that ambiguity is by design.
     */
    NONE("Show None");

    private final String caption;

    StatNameMode(String caption) {
        this.caption = caption;
    }

    /** Button caption naming this mode. */
    public String caption() {
        return caption;
    }

    /** Next mode in the cycle: Names → Abbrev → None → Names. */
    public StatNameMode next() {
        StatNameMode[] all = values();
        return all[(ordinal() + 1) % all.length];
    }
}
