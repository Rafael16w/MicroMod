package de.micromod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class MicroModClient implements ClientModInitializer {
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("micromod", "main"));

    private static KeyBinding keyMenu, keyNoclip, keyFreecam, keyXray, keyEsp;

    private static KeyBinding reg(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(name, key, CATEGORY));
    }

    @Override
    public void onInitializeClient() {
        keyMenu = reg("key.micromod.menu", GLFW.GLFW_KEY_RIGHT_SHIFT);
        keyNoclip = reg("key.micromod.noclip", GLFW.GLFW_KEY_V);
        keyFreecam = reg("key.micromod.freecam", GLFW.GLFW_KEY_B);
        keyXray = reg("key.micromod.xray", GLFW.GLFW_KEY_X);
        keyEsp = reg("key.micromod.esp", GLFW.GLFW_KEY_Z);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        HudRenderCallback.EVENT.register(Esp::render);
    }

    private void onTick(MinecraftClient client) {
        while (keyMenu.wasPressed()) client.setScreen(new MenuScreen());
        while (keyNoclip.wasPressed()) Features.toggleNoclip();
        while (keyFreecam.wasPressed()) Features.toggleFreecam();
        while (keyXray.wasPressed()) Features.toggleXray();
        while (keyEsp.wasPressed()) Features.toggleEsp();

        Features.tick(client);
        Esp.tick(client);
    }
}
