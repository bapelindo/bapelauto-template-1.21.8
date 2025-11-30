// ============================================
// FILE: AutoBotConfigScreen.java
// Path: src/main/java/com/bapelauto/AutoBotConfigScreen.java
// ============================================
package com.bapelauto;

import com.bapelauto.click.TimingPattern;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Main configuration screen for AutoBot
 */
public class AutoBotConfigScreen extends Screen {
    private final Screen parent;
    
    private TextFieldWidget commandField;
    private TextFieldWidget commandDelayField;
    private TextFieldWidget leftClickDelayField;
    private TextFieldWidget rightClickDelayField;
    private TextFieldWidget targetClickDelayField;
    private TextFieldWidget inventoryDelayField;
    
    private int currentTimingPatternIndex = 0;
    private final TimingPattern[] timingPatterns = TimingPattern.values();
    
    public AutoBotConfigScreen(Screen parent) {
        super(Text.literal("AutoBot Configuration"));
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
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Left Click: " + (AutoBotMod.getWorldManager().isLeftClickEnabled() ? "§aON" : "§cOFF")),
            b -> {
                var wm = AutoBotMod.getWorldManager();
                wm.setLeftClickEnabled(!wm.isLeftClickEnabled());
                b.setMessage(Text.literal("Left Click: " + (wm.isLeftClickEnabled() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 140, 20).build());
        
        // Left Click delay field
        leftClickDelayField = new TextFieldWidget(
            this.textRenderer, cx + 0, startY, 150, 20, Text.literal("Left Click Delay")
        );
        leftClickDelayField.setText(String.valueOf(AutoBotMod.getWorldManager().getLeftClickDelay()));
        this.addDrawableChild(leftClickDelayField);
        
        startY += 25;
        
        // Right Click toggle
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Right Click: " + (AutoBotMod.getWorldManager().isRightClickEnabled() ? "§aON" : "§cOFF")),
            b -> {
                var wm = AutoBotMod.getWorldManager();
                wm.setRightClickEnabled(!wm.isRightClickEnabled());
                b.setMessage(Text.literal("Right Click: " + (wm.isRightClickEnabled() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 140, 20).build());
        
        // Right Click delay field
        rightClickDelayField = new TextFieldWidget(
            this.textRenderer, cx + 0, startY, 150, 20, Text.literal("Right Click Delay")
        );
        rightClickDelayField.setText(String.valueOf(AutoBotMod.getWorldManager().getRightClickDelay()));
        this.addDrawableChild(rightClickDelayField);
        
        startY += 35;
        
        // ==========================================
        // INVENTORY SECTION
        // ==========================================
        
        // Auto Steal toggle
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Auto Steal: " + (AutoBotMod.getInventoryManager().isAutoStealEnabled() ? "§aON" : "§cOFF")),
            b -> {
                var im = AutoBotMod.getInventoryManager();
                im.setAutoStealEnabled(!im.isAutoStealEnabled());
                b.setMessage(Text.literal("Auto Steal: " + (im.isAutoStealEnabled() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 140, 20).build());
        
        // Auto Store toggle
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Auto Store: " + (AutoBotMod.getInventoryManager().isAutoStoreEnabled() ? "§aON" : "§cOFF")),
            b -> {
                var im = AutoBotMod.getInventoryManager();
                im.setAutoStoreEnabled(!im.isAutoStoreEnabled());
                b.setMessage(Text.literal("Auto Store: " + (im.isAutoStoreEnabled() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx + 0, startY, 150, 20).build());
        
        startY += 25;
        
        // Inventory delay field
        inventoryDelayField = new TextFieldWidget(
            this.textRenderer, cx - 150, startY, 300, 20, Text.literal("Inventory Delay (ms)")
        );
        inventoryDelayField.setText(String.valueOf(AutoBotMod.getInventoryManager().getInventoryDelay()));
        inventoryDelayField.setPlaceholder(Text.literal("Inventory delay (ms)"));
        this.addDrawableChild(inventoryDelayField);
        
        startY += 35;
        
        // ==========================================
        // GUI CLICK SECTION
        // ==========================================
        
        // GUI Click info
        var gcm = AutoBotMod.getGuiClickManager();
        String clickStatus = gcm.hasTargets() ? "§a" + gcm.getTargetCount() + " targets" : "§7No targets";
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("GUI Click: " + clickStatus),
            b -> {}
        ).dimensions(cx - 150, startY, 140, 20).build());
        
        // Clear targets button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§cClear Targets"),
            b -> {
                AutoBotMod.getGuiClickManager().clearTargets();
                if (this.client != null && this.client.player != null) {
                    this.client.player.sendMessage(Text.literal("§e[GUI] Targets cleared"), true);
                }
                this.clearChildren();
                this.init();
            }
        ).dimensions(cx + 0, startY, 150, 20).build());
        
        startY += 25;
        
        // Target click delay field
        targetClickDelayField = new TextFieldWidget(
            this.textRenderer, cx - 150, startY, 140, 20, Text.literal("Target Click Delay")
        );
        targetClickDelayField.setText(String.valueOf(AutoBotMod.getGuiClickManager().getTimingPattern()));
        this.addDrawableChild(targetClickDelayField);
        
        // Timing pattern cycle button
        TimingPattern currentPattern = AutoBotMod.getGuiClickManager().getTimingPattern();
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Pattern: " + currentPattern.getDisplayName()),
            b -> {
                currentTimingPatternIndex = (currentTimingPatternIndex + 1) % timingPatterns.length;
                TimingPattern newPattern = timingPatterns[currentTimingPatternIndex];
                AutoBotMod.getGuiClickManager().setTimingPattern(newPattern);
                b.setMessage(Text.literal("Pattern: " + newPattern.getDisplayName()));
            }
        ).dimensions(cx + 0, startY, 150, 20).build());
        
        startY += 35;
        
        // ==========================================
        // COMMAND SECTION
        // ==========================================
        
        // Command toggle
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Auto Command: " + (AutoBotMod.isCommandEnabled() ? "§aON" : "§cOFF")),
            b -> {
                AutoBotMod.setCommandEnabled(!AutoBotMod.isCommandEnabled());
                b.setMessage(Text.literal("Auto Command: " + (AutoBotMod.isCommandEnabled() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 300, 20).build());
        
        startY += 25;
        
        // Command text field
        commandField = new TextFieldWidget(
            this.textRenderer, cx - 150, startY, 300, 20, Text.literal("Command")
        );
        commandField.setText(AutoBotMod.getCommand());
        commandField.setPlaceholder(Text.literal("Enter command (e.g., /sell all)"));
        this.addDrawableChild(commandField);
        
        startY += 25;
        
        // Command delay field
        commandDelayField = new TextFieldWidget(
            this.textRenderer, cx - 150, startY, 300, 20, Text.literal("Command Delay")
        );
        commandDelayField.setText(String.valueOf(AutoBotMod.getCommandDelay() / 1000));
        commandDelayField.setPlaceholder(Text.literal("Delay in seconds"));
        this.addDrawableChild(commandDelayField);
        
        startY += 35;
        
        // ==========================================
        // OTHER SETTINGS
        // ==========================================
        
        // Show GUI buttons toggle
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("GUI Buttons: " + (AutoBotMod.isShowGuiButtons() ? "§aON" : "§cOFF")),
            b -> {
                AutoBotMod.setShowGuiButtons(!AutoBotMod.isShowGuiButtons());
                b.setMessage(Text.literal("GUI Buttons: " + (AutoBotMod.isShowGuiButtons() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 300, 20).build());
        
        startY += 35;
        
        // ==========================================
        // BOTTOM BUTTONS
        // ==========================================
        
        int footerY = this.height - 30;
        
        // Save button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a💾 Save"),
            b -> {
                saveSettings();
                if (this.client != null && this.client.player != null) {
                    this.client.player.sendMessage(Text.literal("§a[Config] Settings saved"), true);
                }
            }
        ).dimensions(cx - 150, footerY, 70, 20).build());
        
        // Advanced Config button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§6⚙ Advanced"),
            b -> {
                if (this.client != null) {
                    this.client.setScreen(new AdvancedConfigScreen(this.parent));
                }
            }
        ).dimensions(cx - 75, footerY, 70, 20).build());
        
        // Slimefun Config button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§2🔧 Slimefun"),
            b -> {
                if (this.client != null) {
                    this.client.setScreen(new com.bapelauto.slimefun.SlimefunConfigScreen(this.parent));
                }
            }
        ).dimensions(cx, footerY, 70, 20).build());
        
        // Close button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§c✖ Close"),
            b -> this.close()
        ).dimensions(cx + 75, footerY, 75, 20).build());
        
        // Session Debug button (bottom-left)
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§8Debug"),
            b -> {
                if (this.client != null) {
                    this.client.setScreen(new SessionDebugScreen(this));
                }
            }
        ).dimensions(5, this.height - 30, 60, 20).build());
    }
    
    private void saveSettings() {
        // Save world interaction settings
        try {
            long leftDelay = Long.parseLong(leftClickDelayField.getText());
            AutoBotMod.getWorldManager().setLeftClickDelay(leftDelay);
        } catch (NumberFormatException e) {
            // Keep current value
        }
        
        try {
            long rightDelay = Long.parseLong(rightClickDelayField.getText());
            AutoBotMod.getWorldManager().setRightClickDelay(rightDelay);
        } catch (NumberFormatException e) {
            // Keep current value
        }
        
        // Save inventory settings
        try {
            long invDelay = Long.parseLong(inventoryDelayField.getText());
            AutoBotMod.getInventoryManager().setInventoryDelay(invDelay);
        } catch (NumberFormatException e) {
            // Keep current value
        }
        
        // Save command settings
        String cmd = commandField.getText();
        if (cmd != null && !cmd.trim().isEmpty()) {
            AutoBotMod.setCommand(cmd);
        }
        
        try {
            long cmdDelay = Long.parseLong(commandDelayField.getText()) * 1000; // Convert to ms
            AutoBotMod.setCommandDelay(cmdDelay);
        } catch (NumberFormatException e) {
            // Keep current value
        }
        
        // Save to config file
        AutoBotMod.saveConfiguration();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        super.render(context, mouseX, mouseY, delta);
        
        // Title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§6§l⚡ AutoBot Configuration"),
            this.width / 2, 10, 0xFFFFFF
        );
        
        // Section labels
        int cx = this.width / 2;
        
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal("§e§lWorld Interaction"), cx - 150, 17, 0xFFFF55);
        
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal("§e§lInventory Automation"), cx - 150, 72, 0xFFFF55);
        
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal("§e§lGUI Click Automation"), cx - 150, 127, 0xFFFF55);
        
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal("§e§lAuto Command"), cx - 150, 192, 0xFFFF55);
        
        context.drawTextWithShadow(this.textRenderer, 
            Text.literal("§e§lOther Settings"), cx - 150, 277, 0xFFFF55);
        
        // Hint text
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§8Press [HOME] to open config | Press [0] to toggle bot"),
            this.width / 2, this.height - 45, 0x666666
        );
    }
    
    @Override
    public void close() {
        saveSettings();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}