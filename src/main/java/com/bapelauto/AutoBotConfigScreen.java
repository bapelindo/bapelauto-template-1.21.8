// ============================================
// FILE: AutoBotConfigScreen.java
// Path: src/main/java/com/bapelauto/AutoBotConfigScreen.java
//
// Ported to Minecraft 26.1.2 / Fabric (official Mojang mappings).
// Key changes from the 1.21.x version:
//   - GuiGraphics -> GuiGraphicsExtractor
//   - render(...) -> extractRenderState(...) (new render-extraction split)
//   - close() -> onClose()
//   - EditBox: setText/getText/setPlaceholder -> setValue/getValue/setHint
//   - Player.sendSystemMessage(...) -> Player.sendSystemMessage(...)
//   - keyPressed(int,int,int) -> keyPressed(KeyEvent) (new input API)
//   - Text colors passed to graphics.text(...) must be ARGB (0xFFrrggbb),
//     not RGB, per Minecraft 1.21.6+ text color changes.
//
// NOTE: I could not compile-test this against the real 26.1.2 jar (it's a
// very fresh release with sparse docs). The KeyEvent accessor below
// (event.key()) is my best-confidence guess based on the record-style API
// Mojang introduced for input events - if your IDE shows a different
// accessor name, that's the only line likely to need a one-word fix.
// ============================================
package com.bapelauto;

import com.bapelauto.click.TimingPattern;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/**
 * Main configuration screen for AutoBot
 */
public class AutoBotConfigScreen extends Screen {
    private final Screen parent;

    private EditBox commandField;
    private EditBox commandDelayField;
    private EditBox leftClickDelayField;
    private EditBox rightClickDelayField;
    private EditBox targetClickDelayField;
    private EditBox inventoryDelayField;

    private int currentTimingPatternIndex = 0;
    private final TimingPattern[] timingPatterns = TimingPattern.values();

    public AutoBotConfigScreen(Screen parent) {
        super(Component.literal("AutoBot Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int startY = 30;

        // ==========================================
        // WORLD INTERACTION SECTION
        // ==========================================

        // Left Click toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("Left Click: " + (AutoBotMod.getWorldManager().isLeftClickEnabled() ? "§aON" : "§cOFF")),
            b -> {
                var wm = AutoBotMod.getWorldManager();
                wm.setLeftClickEnabled(!wm.isLeftClickEnabled());
                b.setMessage(Component.literal("Left Click: " + (wm.isLeftClickEnabled() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx - 150, startY, 140, 20).build());

        // Left Click delay field
        leftClickDelayField = new EditBox(
            this.font, cx + 0, startY, 150, 20, Component.literal("Left Click Delay")
        );
        leftClickDelayField.setValue(String.valueOf(AutoBotMod.getWorldManager().getLeftClickDelay()));
        this.addRenderableWidget(leftClickDelayField);

        startY += 25;

        // Right Click toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("Right Click: " + (AutoBotMod.getWorldManager().isRightClickEnabled() ? "§aON" : "§cOFF")),
            b -> {
                var wm = AutoBotMod.getWorldManager();
                wm.setRightClickEnabled(!wm.isRightClickEnabled());
                b.setMessage(Component.literal("Right Click: " + (wm.isRightClickEnabled() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx - 150, startY, 140, 20).build());

        // Right Click delay field
        rightClickDelayField = new EditBox(
            this.font, cx + 0, startY, 150, 20, Component.literal("Right Click Delay")
        );
        rightClickDelayField.setValue(String.valueOf(AutoBotMod.getWorldManager().getRightClickDelay()));
        this.addRenderableWidget(rightClickDelayField);

        startY += 35;

        // ==========================================
        // INVENTORY SECTION
        // ==========================================

        // Auto Steal toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("Auto Steal: " + (AutoBotMod.getInventoryManager().isAutoStealEnabled() ? "§aON" : "§cOFF")),
            b -> {
                var im = AutoBotMod.getInventoryManager();
                im.setAutoStealEnabled(!im.isAutoStealEnabled());
                b.setMessage(Component.literal("Auto Steal: " + (im.isAutoStealEnabled() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx - 150, startY, 140, 20).build());

        // Auto Store toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("Auto Store: " + (AutoBotMod.getInventoryManager().isAutoStoreEnabled() ? "§aON" : "§cOFF")),
            b -> {
                var im = AutoBotMod.getInventoryManager();
                im.setAutoStoreEnabled(!im.isAutoStoreEnabled());
                b.setMessage(Component.literal("Auto Store: " + (im.isAutoStoreEnabled() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx + 0, startY, 150, 20).build());

        startY += 25;

        // Inventory delay field
        inventoryDelayField = new EditBox(
            this.font, cx - 150, startY, 300, 20, Component.literal("Inventory Delay (ms)")
        );
        inventoryDelayField.setValue(String.valueOf(AutoBotMod.getInventoryManager().getInventoryDelay()));
        inventoryDelayField.setHint(Component.literal("Inventory delay (ms)"));
        this.addRenderableWidget(inventoryDelayField);

        startY += 35;

        // ==========================================
        // GUI CLICK SECTION
        // ==========================================

        // GUI Click info
        var gcm = AutoBotMod.getGuiClickManager();
        String clickStatus = gcm.hasTargets() ? "§a" + gcm.getTargetCount() + " targets" : "§7No targets";

        this.addRenderableWidget(Button.builder(
            Component.literal("GUI Click: " + clickStatus),
            b -> {}
        ).bounds(cx - 150, startY, 140, 20).build());

        // Clear targets button
        this.addRenderableWidget(Button.builder(
            Component.literal("§cClear Targets"),
            b -> {
                AutoBotMod.getGuiClickManager().clearTargets();
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendSystemMessage(Component.literal("§e[GUI] Targets cleared"), true);
                }
                this.clearWidgets();
                this.init();
            }
        ).bounds(cx + 0, startY, 150, 20).build());

        startY += 25;

        // Target click delay field
        targetClickDelayField = new EditBox(
            this.font, cx - 150, startY, 140, 20, Component.literal("Target Click Delay")
        );
        targetClickDelayField.setValue(String.valueOf(AutoBotMod.getGuiClickManager().getTimingPattern()));
        this.addRenderableWidget(targetClickDelayField);

        // Timing pattern cycle button
        TimingPattern currentPattern = AutoBotMod.getGuiClickManager().getTimingPattern();
        this.addRenderableWidget(Button.builder(
            Component.literal("Pattern: " + currentPattern.getDisplayName()),
            b -> {
                currentTimingPatternIndex = (currentTimingPatternIndex + 1) % timingPatterns.length;
                TimingPattern newPattern = timingPatterns[currentTimingPatternIndex];
                AutoBotMod.getGuiClickManager().setTimingPattern(newPattern);
                b.setMessage(Component.literal("Pattern: " + newPattern.getDisplayName()));
            }
        ).bounds(cx + 0, startY, 150, 20).build());

        startY += 35;

        // ==========================================
        // COMMAND SECTION
        // ==========================================

        // Command toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("Auto Command: " + (AutoBotMod.isCommandEnabled() ? "§aON" : "§cOFF")),
            b -> {
                AutoBotMod.setCommandEnabled(!AutoBotMod.isCommandEnabled());
                b.setMessage(Component.literal("Auto Command: " + (AutoBotMod.isCommandEnabled() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx - 150, startY, 300, 20).build());

        startY += 25;

        // Command text field
        commandField = new EditBox(
            this.font, cx - 150, startY, 300, 20, Component.literal("Command")
        );
        commandField.setValue(AutoBotMod.getCommand());
        commandField.setHint(Component.literal("Enter command (e.g., /sell all)"));
        this.addRenderableWidget(commandField);

        startY += 25;

        // Command delay field
        commandDelayField = new EditBox(
            this.font, cx - 150, startY, 300, 20, Component.literal("Command Delay")
        );
        commandDelayField.setValue(String.valueOf(AutoBotMod.getCommandDelay() / 1000));
        commandDelayField.setHint(Component.literal("Delay in seconds"));
        this.addRenderableWidget(commandDelayField);

        startY += 35;

        // ==========================================
        // OTHER SETTINGS
        // ==========================================

        // Show GUI buttons toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("GUI Buttons: " + (AutoBotMod.isShowGuiButtons() ? "§aON" : "§cOFF")),
            b -> {
                AutoBotMod.setShowGuiButtons(!AutoBotMod.isShowGuiButtons());
                b.setMessage(Component.literal("GUI Buttons: " + (AutoBotMod.isShowGuiButtons() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx - 150, startY, 300, 20).build());

        startY += 35;

        // ==========================================
        // BOTTOM BUTTONS
        // ==========================================

        int footerY = this.height - 30;

        // Save button
        this.addRenderableWidget(Button.builder(
            Component.literal("§a💾 Save"),
            b -> {
                saveSettings();
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendSystemMessage(Component.literal("§a[Config] Settings saved"), true);
                }
            }
        ).bounds(cx - 150, footerY, 70, 20).build());

        // Advanced Config button
        this.addRenderableWidget(Button.builder(
            Component.literal("§6⚙ Advanced"),
            b -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new AdvancedConfigScreen(this.parent));
                }
            }
        ).bounds(cx - 75, footerY, 70, 20).build());

        // Slimefun Config button
        this.addRenderableWidget(Button.builder(
            Component.literal("§2🔧 Slimefun"),
            b -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new com.bapelauto.slimefun.SlimefunConfigScreen(this.parent));
                }
            }
        ).bounds(cx, footerY, 70, 20).build());

        // Close button
        this.addRenderableWidget(Button.builder(
            Component.literal("§c✖ Close"),
            b -> this.onClose()
        ).bounds(cx + 75, footerY, 75, 20).build());

        // Session Debug button (bottom-left)
        this.addRenderableWidget(Button.builder(
            Component.literal("§8Debug"),
            b -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new SessionDebugScreen(this));
                }
            }
        ).bounds(5, this.height - 30, 60, 20).build());
    }

    private void saveSettings() {
        // Save world interaction settings
        try {
            long leftDelay = Long.parseLong(leftClickDelayField.getValue());
            AutoBotMod.getWorldManager().setLeftClickDelay(leftDelay);
        } catch (NumberFormatException e) {
            // Keep current value
        }

        try {
            long rightDelay = Long.parseLong(rightClickDelayField.getValue());
            AutoBotMod.getWorldManager().setRightClickDelay(rightDelay);
        } catch (NumberFormatException e) {
            // Keep current value
        }

        // Save inventory settings
        try {
            long invDelay = Long.parseLong(inventoryDelayField.getValue());
            AutoBotMod.getInventoryManager().setInventoryDelay(invDelay);
        } catch (NumberFormatException e) {
            // Keep current value
        }

        // Save command settings
        String cmd = commandField.getValue();
        if (cmd != null && !cmd.trim().isEmpty()) {
            AutoBotMod.setCommand(cmd);
        }

        try {
            long cmdDelay = Long.parseLong(commandDelayField.getValue()) * 1000; // Convert to ms
            AutoBotMod.setCommandDelay(cmdDelay);
        } catch (NumberFormatException e) {
            // Keep current value
        }

        // Save to config file
        AutoBotMod.saveConfiguration();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // Background
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        super.extractRenderState(context, mouseX, mouseY, delta);

        // Title
        Component title = Component.literal("§6§l⚡ AutoBot Configuration");
        context.text(this.font, title, this.width / 2 - this.font.width(title) / 2, 10, 0xFFFFFFFF, true);

        // Section labels
        int cx = this.width / 2;

        context.text(this.font, Component.literal("§e§lWorld Interaction"), cx - 150, 17, 0xFFFFFF55, true);
        context.text(this.font, Component.literal("§e§lInventory Automation"), cx - 150, 72, 0xFFFFFF55, true);
        context.text(this.font, Component.literal("§e§lGUI Click Automation"), cx - 150, 127, 0xFFFFFF55, true);
        context.text(this.font, Component.literal("§e§lAuto Command"), cx - 150, 192, 0xFFFFFF55, true);
        context.text(this.font, Component.literal("§e§lOther Settings"), cx - 150, 277, 0xFFFFFF55, true);

        // Hint text (centered)
        Component hint = Component.literal("§8Press [HOME] to open config | Press [0] to toggle bot");
        context.text(this.font, hint, this.width / 2 - this.font.width(hint) / 2, this.height - 45, 0xFF666666, true);
    }

    @Override
    public void onClose() {
        saveSettings();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) { // ESC
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}