package net.lunix.nixstats;

import net.lunix.nixstats.mixin.ClientAdvancementsAccessor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Client-side advancement reads for the sidebar. Everything comes from the advancement
 * tree the server syncs to the client (vanilla + datapack + modded alike), so datapack
 * advancements are handled with no special-casing. Progress is read from the integrated
 * server in single-player (tick-accurate) and from the synced client map on servers.
 */
public final class Advancements {

    private Advancements() {}

    /** The client's advancement tree (all advancements the server has sent), or null if not in a world. */
    public static AdvancementTree clientTree(Minecraft mc) {
        if (mc.getConnection() == null) return null;
        ClientAdvancements ca = mc.getConnection().getAdvancements();
        return ca != null ? ca.getTree() : null;
    }

    /**
     * The advancement tree to enumerate from: the integrated server's in single-player (its
     * data is complete and reliable — the same reason the stat reads prefer it), otherwise the
     * client's synced tree on multiplayer. Null if neither is available yet.
     */
    public static AdvancementTree bestTree(Minecraft mc) {
        MinecraftServer srv = mc.getSingleplayerServer();
        if (srv != null) return srv.getAdvancements().tree();
        return clientTree(mc);
    }

    /**
     * Every advancement that has a display — i.e. the ones shown on the vanilla advancements
     * screen. This naturally drops the hundreds of {@code minecraft:recipes/...} and hidden
     * backing advancements, matching what a player thinks of as "advancements".
     */
    public static List<AdvancementHolder> displayable(Minecraft mc) {
        AdvancementTree tree = bestTree(mc);
        if (tree == null) return List.of();
        List<AdvancementHolder> out = new ArrayList<>();
        for (AdvancementNode node : tree.nodes()) {
            AdvancementHolder h = node.holder();
            if (h.value().display().isPresent()) out.add(h);
        }
        return out;
    }

    /** Resolve a single advancement by its string id, or null. */
    public static AdvancementHolder byId(Minecraft mc, String id) {
        if (id == null) return null;
        Identifier loc = Identifier.tryParse(id);
        if (loc == null) return null;
        AdvancementTree tree = bestTree(mc);
        if (tree == null) return null;
        AdvancementNode node = tree.get(loc);
        return node != null ? node.holder() : null;
    }

    /** Progress for one advancement: integrated server in single-player, else the synced client map. */
    public static AdvancementProgress progress(Minecraft mc, AdvancementHolder holder) {
        if (holder == null) return null;
        MinecraftServer srv = mc.getSingleplayerServer();
        if (srv != null && mc.player != null) {
            ServerPlayer sp = srv.getPlayerList().getPlayer(mc.player.getUUID());
            if (sp != null) return sp.getAdvancements().getOrStartProgress(holder);
        }
        if (mc.getConnection() == null) return null;
        ClientAdvancements ca = mc.getConnection().getAdvancements();
        if (ca == null) return null;
        return ((ClientAdvancementsAccessor) ca).nixstats$getProgress().get(holder);
    }

    public static boolean isDone(AdvancementProgress p) {
        return p != null && p.isDone();
    }

    /**
     * Done/total counts over displayable advancements. A null/blank namespace means the grand
     * total across everything; otherwise only advancements in that namespace (e.g. "minecraft",
     * a datapack's namespace, or a mod id) are counted. Returns {@code [done, total]}.
     */
    public static int[] total(Minecraft mc, String namespace) {
        List<AdvancementHolder> all = displayable(mc);
        boolean scoped = namespace != null && !namespace.isEmpty();

        // Resolve the progress source once, so a big tree isn't re-scanned per advancement.
        ServerPlayer sp = null;
        ClientAdvancementsAccessor acc = null;
        MinecraftServer srv = mc.getSingleplayerServer();
        if (srv != null && mc.player != null) {
            sp = srv.getPlayerList().getPlayer(mc.player.getUUID());
        }
        if (sp == null && mc.getConnection() != null) {
            ClientAdvancements ca = mc.getConnection().getAdvancements();
            if (ca != null) acc = (ClientAdvancementsAccessor) ca;
        }

        int done = 0, tot = 0;
        for (AdvancementHolder h : all) {
            if (scoped && !h.id().getNamespace().equals(namespace)) continue;
            tot++;
            AdvancementProgress p = sp != null
                ? sp.getAdvancements().getOrStartProgress(h)
                : (acc != null ? acc.nixstats$getProgress().get(h) : null);
            if (p != null && p.isDone()) done++;
        }
        return new int[]{done, tot};
    }

    /** Namespaces that contribute at least one displayable advancement, sorted. */
    public static List<String> namespaces(Minecraft mc) {
        Set<String> set = new TreeSet<>();
        for (AdvancementHolder h : displayable(mc)) set.add(h.id().getNamespace());
        return new ArrayList<>(set);
    }

    /**
     * Icon for a per-namespace total: that namespace's root (tab) advancement icon — the same
     * icon the vanilla advancements screen shows as its tab. Falls back to the first displayable
     * advancement in the namespace, then EMPTY.
     */
    public static ItemStack namespaceRootIcon(Minecraft mc, String namespace) {
        AdvancementTree tree = bestTree(mc);
        if (tree == null) return ItemStack.EMPTY;
        for (AdvancementNode node : tree.roots()) {
            AdvancementHolder h = node.holder();
            if (h.id().getNamespace().equals(namespace) && h.value().display().isPresent())
                return h.value().display().get().getIcon().create();
        }
        for (AdvancementHolder h : displayable(mc)) {
            if (h.id().getNamespace().equals(namespace))
                return h.value().display().get().getIcon().create();
        }
        return ItemStack.EMPTY;
    }

    /** Advancement title as plain text, or the id if it somehow has no display. */
    public static String title(AdvancementHolder h) {
        return h.value().display()
            .map(d -> d.getTitle().getString())
            .orElse(h.id().toString());
    }

    /** Displayable advancements as {id, title} pairs, sorted by title — for the picker. */
    public static List<String[]> listForPicker(Minecraft mc) {
        List<String[]> out = new ArrayList<>();
        for (AdvancementHolder h : displayable(mc)) {
            out.add(new String[]{h.id().toString(), title(h)});
        }
        out.sort(Comparator.comparing(a -> a[1]));
        return out;
    }

    /**
     * Displayable advancements grouped by namespace for the picker's collapsible tree.
     * Namespaces are sorted; each namespace's items are {id, title} sorted by title.
     */
    public static Map<String, List<String[]>> groupedByNamespace(Minecraft mc) {
        Map<String, List<String[]>> map = new TreeMap<>();
        for (AdvancementHolder h : displayable(mc)) {
            map.computeIfAbsent(h.id().getNamespace(), k -> new ArrayList<>())
               .add(new String[]{h.id().toString(), title(h)});
        }
        for (List<String[]> v : map.values()) v.sort(Comparator.comparing(a -> a[1]));
        return map;
    }
}
