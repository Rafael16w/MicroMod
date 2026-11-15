package de.micromod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class Features {
    public static boolean noclip, freecam, xray, esp;
    public static Vec3d freecamPos = Vec3d.ZERO;

    private Features() {}

    private static void msg(String name, boolean on) {
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (p != null) p.sendMessage(Text.literal(name + ": " + (on ? "AN" : "AUS")), true);
    }

    public static void toggleNoclip() {
        noclip = !noclip;
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (!noclip && p != null) {
            p.noClip = false;
            if (!p.getAbilities().creativeMode) {
                p.getAbilities().allowFlying = false;
                p.getAbilities().flying = false;
            }
        }
        msg("Noclip", noclip);
    }

    public static void toggleFreecam() {
        freecam = !freecam;
        ClientPlayerEntity p = MinecraftClient.getInstance().player;
        if (freecam && p != null) freecamPos = p.getEyePos();
        msg("Freecam", freecam);
    }

    public static void toggleXray() {
        xray = !xray;
        msg("X-Ray", xray);
    }

    public static void toggleEsp() {
        esp = !esp;
        msg("ESP", esp);
    }

    /** Bewegt die Freecam-Kamera (wird jeden Client-Tick aufgerufen). */
    public static void tick(MinecraftClient c) {
        ClientPlayerEntity p = c.player;
        if (!freecam || p == null) return;

        double yr = Math.toRadians(p.getYaw());
        double pr = Math.toRadians(p.getPitch());
        Vec3d fwd = new Vec3d(-Math.sin(yr) * Math.cos(pr), -Math.sin(pr), Math.cos(yr) * Math.cos(pr));
        Vec3d right = new Vec3d(-Math.cos(yr), 0, -Math.sin(yr));

        Vec3d move = Vec3d.ZERO;
        if (c.options.forwardKey.isPressed()) move = move.add(fwd);
        if (c.options.backKey.isPressed()) move = move.subtract(fwd);
        if (c.options.rightKey.isPressed()) move = move.add(right);
        if (c.options.leftKey.isPressed()) move = move.subtract(right);
        if (c.options.jumpKey.isPressed()) move = move.add(0, 1, 0);
        if (c.options.sneakKey.isPressed()) move = move.subtract(0, 1, 0);

        double speed = c.options.sprintKey.isPressed() ? 1.5 : 0.6;
        freecamPos = freecamPos.add(move.multiply(speed));
    }
}
