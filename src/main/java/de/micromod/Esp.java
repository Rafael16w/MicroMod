package de.micromod;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ESP und X-Ray ohne Eingriff in die Render-Pipeline:
 * Blockpositionen werden auf den Bildschirm projiziert und als farbige Rahmen
 * im HUD gezeichnet. Dadurch sieht man sie durch Waende.
 */
public final class Esp {
    // Farben: 0xAARRGGBB
    public static final int COLOR_BARREL = 0xFFFF2020;   // Rot
    public static final int COLOR_CHEST = 0xFFFFFFFF;    // Weiss
    public static final int COLOR_ENDER = 0xFFB040FF;    // Lila
    public static final int COLOR_SHULKER = 0xFFFF55FF;  // Pink

    private static final int RADIUS = 20;
    private static final int MAX_TARGETS = 600;

    private record Target(BlockPos pos, int color) {}

    private static final List<Target> targets = new ArrayList<>();
    private static final Map<Block, Integer> ORES = new HashMap<>();
    private static int cooldown = 0;

    static {
        ore(Blocks.COAL_ORE, 0xFF555555);
        ore(Blocks.DEEPSLATE_COAL_ORE, 0xFF555555);
        ore(Blocks.IRON_ORE, 0xFFD8AF93);
        ore(Blocks.DEEPSLATE_IRON_ORE, 0xFFD8AF93);
        ore(Blocks.COPPER_ORE, 0xFFE07A3C);
        ore(Blocks.DEEPSLATE_COPPER_ORE, 0xFFE07A3C);
        ore(Blocks.GOLD_ORE, 0xFFFFD700);
        ore(Blocks.DEEPSLATE_GOLD_ORE, 0xFFFFD700);
        ore(Blocks.NETHER_GOLD_ORE, 0xFFFFD700);
        ore(Blocks.REDSTONE_ORE, 0xFFFF0000);
        ore(Blocks.DEEPSLATE_REDSTONE_ORE, 0xFFFF0000);
        ore(Blocks.LAPIS_ORE, 0xFF2040FF);
        ore(Blocks.DEEPSLATE_LAPIS_ORE, 0xFF2040FF);
        ore(Blocks.DIAMOND_ORE, 0xFF33EBFF);
        ore(Blocks.DEEPSLATE_DIAMOND_ORE, 0xFF33EBFF);
        ore(Blocks.EMERALD_ORE, 0xFF22DD44);
        ore(Blocks.DEEPSLATE_EMERALD_ORE, 0xFF22DD44);
        ore(Blocks.NETHER_QUARTZ_ORE, 0xFFEEEEEE);
        ore(Blocks.ANCIENT_DEBRIS, 0xFF8B5A2B);
    }

    private static void ore(Block b, int color) {
        ORES.put(b, color);
    }

    private Esp() {}

    /** Wird jeden Client-Tick aufgerufen; scannt nur alle 20 Ticks. */
    public static void tick(MinecraftClient c) {
        if (c.player == null || c.world == null || (!Features.esp && !Features.xray)) {
            targets.clear();
            return;
        }
        if (cooldown-- > 0) return;
        cooldown = 20;
        scan(c.player, c.world);
    }

    private static void scan(ClientPlayerEntity p, ClientWorld w) {
        targets.clear();
        BlockPos center = p.getBlockPos();
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    if (targets.size() >= MAX_TARGETS) return;
                    m.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    Block b = w.getBlockState(m).getBlock();
                    if (b == Blocks.AIR) continue;

                    if (Features.esp) {
                        int col = storageColor(b);
                        if (col != 0) {
                            targets.add(new Target(m.toImmutable(), col));
                            continue;
                        }
                    }
                    if (Features.xray) {
                        Integer col = ORES.get(b);
                        if (col != null) targets.add(new Target(m.toImmutable(), col));
                    }
                }
            }
        }
    }

    private static int storageColor(Block b) {
        if (b == Blocks.BARREL) return COLOR_BARREL;
        if (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST) return COLOR_CHEST;
        if (b == Blocks.ENDER_CHEST) return COLOR_ENDER;
        if (b instanceof ShulkerBoxBlock) return COLOR_SHULKER;
        return 0;
    }

    // ---------- Projektion ----------
    private static Vec3d cam, F, R, U;
    private static double tanHalf, aspect;
    private static int sw, sh;

    private static boolean project(double x, double y, double z, double[] out) {
        double dx = x - cam.x, dy = y - cam.y, dz = z - cam.z;
        double zc = dx * F.x + dy * F.y + dz * F.z;
        if (zc < 0.05) return false;
        double xc = dx * R.x + dy * R.y + dz * R.z;
        double yc = dx * U.x + dy * U.y + dz * U.z;
        out[0] = sw / 2.0 * (1 + (xc / zc) / (tanHalf * aspect));
        out[1] = sh / 2.0 * (1 - (yc / zc) / tanHalf);
        return true;
    }

    public static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient c = MinecraftClient.getInstance();
        ClientPlayerEntity p = c.player;
        if (p == null || targets.isEmpty() || (!Features.esp && !Features.xray)) return;

        cam = Features.freecam ? Features.freecamPos : p.getEyePos();
        double yr = Math.toRadians(p.getYaw());
        double pr = Math.toRadians(p.getPitch());
        F = new Vec3d(-Math.sin(yr) * Math.cos(pr), -Math.sin(pr), Math.cos(yr) * Math.cos(pr));
        R = new Vec3d(-Math.cos(yr), 0, -Math.sin(yr));
        U = R.crossProduct(F);

        sw = c.getWindow().getScaledWidth();
        sh = c.getWindow().getScaledHeight();
        aspect = (double) sw / (double) sh;
        tanHalf = Math.tan(Math.toRadians(c.options.getFov().getValue()) / 2.0);

        double[] pt = new double[2];
        for (Target t : targets) {
            BlockPos bp = t.pos();
            double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            boolean ok = true;
            for (int i = 0; i < 8 && ok; i++) {
                double cx = bp.getX() + (i & 1);
                double cy = bp.getY() + ((i >> 1) & 1);
                double cz = bp.getZ() + ((i >> 2) & 1);
                if (!project(cx, cy, cz, pt)) {
                    ok = false;
                    break;
                }
                minX = Math.min(minX, pt[0]);
                maxX = Math.max(maxX, pt[0]);
                minY = Math.min(minY, pt[1]);
                maxY = Math.max(maxY, pt[1]);
            }
            if (!ok) continue;
            if (maxX < 0 || maxY < 0 || minX > sw || minY > sh) continue;
            if (maxX - minX > sw * 2 || maxY - minY > sh * 2) continue;

            int x1 = (int) minX, y1 = (int) minY, x2 = (int) maxX, y2 = (int) maxY;
            if (x2 - x1 < 4) { int mid = (x1 + x2) / 2; x1 = mid - 2; x2 = mid + 2; }
            if (y2 - y1 < 4) { int mid = (y1 + y2) / 2; y1 = mid - 2; y2 = mid + 2; }

            int col = t.color();
            ctx.fill(x1, y1, x2, y1 + 1, col);       // oben
            ctx.fill(x1, y2 - 1, x2, y2, col);       // unten
            ctx.fill(x1, y1, x1 + 1, y2, col);       // links
            ctx.fill(x2 - 1, y1, x2, y2, col);       // rechts
        }
    }
}
