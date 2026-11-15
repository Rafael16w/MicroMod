package de.micromod;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;

public class MenuScreen extends Screen {
    public MenuScreen() {
        super(Text.literal("Micro-Mod"));
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 90;
        int y = this.height / 2 - 55;
        addDrawableChild(toggle("Noclip", x, y, () -> Features.noclip, Features::toggleNoclip));
        addDrawableChild(toggle("Freecam", x, y + 25, () -> Features.freecam, Features::toggleFreecam));
        addDrawableChild(toggle("X-Ray (Erze)", x, y + 50, () -> Features.xray, Features::toggleXray));
        addDrawableChild(toggle("ESP (Fässer/Truhen)", x, y + 75, () -> Features.esp, Features::toggleEsp));
        addDrawableChild(ButtonWidget.builder(Text.literal("Schließen"), b -> this.close())
                .dimensions(x, y + 105, 180, 20).build());
    }

    private ButtonWidget toggle(String name, int x, int y, BooleanSupplier state, Runnable action) {
        return ButtonWidget.builder(label(name, state.getAsBoolean()), b -> {
            action.run();
            b.setMessage(label(name, state.getAsBoolean()));
        }).dimensions(x, y, 180, 20).build();
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "AN" : "AUS"));
    }
}
