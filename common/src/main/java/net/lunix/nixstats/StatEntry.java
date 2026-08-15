package net.lunix.nixstats;

public class StatEntry {

    // stat types: phantom | block_mined | item_used | item_crafted | item_broken
    //             item_picked_up | item_dropped | entity_killed | entity_killed_by | custom
    public String statType = "phantom";
    public String targetId = null;   // registry ID or custom stat ID; null for phantom
    public String label    = "Phantom";

    public StatEntry() {}

    public StatEntry(String statType, String targetId, String label) {
        this.statType = statType;
        this.targetId = targetId;
        this.label    = label;
    }

    public static StatEntry phantom() {
        return new StatEntry("phantom", null, "Phantom");
    }

    /**
     * The action half of a "<name> <action>" label, or null for stat types that don't
     * have one. Only the types listed here draw an icon that identifies their subject
     * on its own, so only they can afford to lose the name.
     */
    private static String actionOnlyLabel(String statType) {
        if (statType == null) return null;
        return switch (statType) {
            case "block_mined"      -> "Mined";
            case "item_used"        -> "Used";
            case "item_crafted"     -> "Crafted";
            case "item_broken"      -> "Broken";
            case "item_picked_up"   -> "Picked Up";
            case "item_dropped"     -> "Dropped";
            case "entity_killed"    -> "Killed";
            case "entity_killed_by" -> "Killed By";
            default                 -> null;
        };
    }

    /**
     * Label as it should appear in the HUD. With {@code hideNames} set, item/block/mob
     * rows show only their action and lean on the icon for identity. Everything else
     * keeps its full label: General stats all share one generic page icon, and for
     * advancements and the phantom timer the name *is* the meaning.
     */
    public static String displayLabel(StatEntry entry, boolean hideNames) {
        if (entry == null) return "";
        if (!hideNames)    return entry.label;
        String action = actionOnlyLabel(entry.statType);
        return action != null ? action : entry.label;
    }
}
