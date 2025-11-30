// ============================================
// FILE: SlimefunConfigScreen.java
// Path: src/main/java/com/bapelauto/slimefun/SlimefunConfigScreen.java
// ============================================
package com.bapelauto.slimefun;

import com.bapelauto.AutoBotMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Dedicated UI for Slimefun automation settings
 */
public class SlimefunConfigScreen extends Screen {
    private final Screen parent;
    private final SlimefunAutoManager slimefunManager;
    
    private int selectedPresetIndex = -1;
    
    public SlimefunConfigScreen(Screen parent) {
        super(Text.literal("Slimefun Automation"));
        this.parent = parent;
        this.slimefunManager = AutoBotMod.getSlimefunManager();
    }
    
    @Override
    protected void init() {
        int cx = this.width / 2;
        int startY = 40;
        
        // ==========================================
        // SLIMEFUN MODE TOGGLE
        // ==========================================
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Slimefun Mode: " + (slimefunManager.isSlimefunModeEnabled() ? "§a§lON" : "§c§lOFF")),
            b -> {
                if (slimefunManager.isSlimefunModeEnabled()) {
                    slimefunManager.disable(this.client);
                } else {
                    slimefunManager.enable(this.client);
                }
                b.setMessage(Text.literal("Slimefun Mode: " + 
                    (slimefunManager.isSlimefunModeEnabled() ? "§a§lON" : "§c§lOFF")));
            }
        ).dimensions(cx - 150, startY, 300, 25).build());
        
        startY += 35;
        
        // ==========================================
        // QUICK SETUP
        // ==========================================
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a★ Quick Setup (Auto-Detect)"),
            b -> {
                slimefunManager.quickSetup(this.client);
            }
        ).dimensions(cx - 150, startY, 300, 25).build());
        
        startY += 35;
        
        // ==========================================
        // SAFETY MODE
        // ==========================================
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Safety Mode: " + (slimefunManager.isSafetyMode() ? "§aON" : "§cOFF")),
            b -> {
                slimefunManager.setSafetyMode(!slimefunManager.isSafetyMode());
                b.setMessage(Text.literal("Safety Mode: " + 
                    (slimefunManager.isSafetyMode() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 300, 20).build()); // FIXED (Added closing parenthesis)
        
        startY += 30;
        
        // ==========================================
        // PRESET SELECTION
        // ==========================================
        SlimefunProfilePresets.SlimefunPreset[] presets = SlimefunProfilePresets.SlimefunPreset.values();
        
        int listY = startY + 10;
        for (int i = 0; i < Math.min(presets.length, 8); i++) {
            final int index = i;
            SlimefunProfilePresets.SlimefunPreset preset = presets[i];
            
            boolean isSelected = (i == selectedPresetIndex);
            String btnText = preset.getName();
            if (isSelected) btnText = "§e▶ " + btnText;
            
            // Preset button
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal(btnText),
                b -> {
                    selectedPresetIndex = index;
                    
                    // Show description
                    if (this.client != null && this.client.player != null) {
                        this.client.player.sendMessage(
                            Text.literal("§7" + preset.getDescription()), false
                        );
                    }
                    
                    this.clearChildren();
                    this.init();
                }
            ).dimensions(cx - 150, listY, 240, 18).build());
            
            // Apply button
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aApply"),
                b -> {
                    slimefunManager.applyPreset(preset, this.client);
                }
            ).dimensions(cx + 95, listY, 55, 18).build());
            
            listY += 20;
        }
        
        // ==========================================
        // STATISTICS
        // ==========================================
        int statsY = this.height - 90;
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§b📊 Show Statistics"),
            b -> {
                slimefunManager.showStatistics(this.client);
            }
        ).dimensions(cx - 100, statsY, 95, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§e🔄 Reset Stats"),
            b -> {
                slimefunManager.resetStatistics();
                if (this.client != null && this.client.player != null) {
                    this.client.player.sendMessage(
                        Text.literal("§e[Slimefun] Statistics reset"), false
                    );
                }
            }
        ).dimensions(cx + 5, statsY, 95, 20).build());
        
        // ==========================================
        // FOOTER BUTTONS
        // ==========================================
        int footerY = this.height - 30;
        
        // Back button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§c« Back"),
            b -> this.close()
        ).dimensions(cx - 150, footerY, 90, 20).build());
        
        // Main config button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§eMain Config"),
            b -> {
                if (this.client != null) {
                    this.client.setScreen(new com.bapelauto.AutoBotConfigScreen(this.parent));
                }
            }
        ).dimensions(cx - 45, footerY, 90, 20).build());
        
        // Help button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§b? Help"),
            b -> {
                showHelp();
            }
        ).dimensions(cx + 60, footerY, 90, 20).build());
    }
    
    private void showHelp() {
        if (this.client == null || this.client.player == null) return;
        
        this.client.player.sendMessage(
            Text.literal("§e§l=== Slimefun Quick Guide ==="), false
        );
        this.client.player.sendMessage(
            Text.literal("§71. Enable §aSlimefun Mode"), false
        );
        this.client.player.sendMessage(
            Text.literal("§72. Open a Slimefun machine GUI"), false
        );
        this.client.player.sendMessage(
            Text.literal("§73. Press §6[\\] §7or use §aQuick Setup"), false
        );
        this.client.player.sendMessage(
            Text.literal("§74. Press §6[=] §7to start automation"), false
        );
        this.client.player.sendMessage(
            Text.literal("§75. Bot will auto-collect outputs!"), false
        );
        this.client.player.sendMessage(
            Text.literal("§c⚠ §7Always enable §eSafety Mode §7for reactors!"), false
        );
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fillGradient(0, 0, this.width, this.height, 0xEE000000, 0xEE101010);
        super.render(context, mouseX, mouseY, delta);
        
        int cx = this.width / 2;
        
        // Title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§2§lSlimefun §6§lAutomation"),
            cx, 15, 0xFFFFFF
        );
        
        // Current machine info
        if (slimefunManager.isSlimefunModeEnabled()) {
            SlimefunDetector.SlimefunMachine current = slimefunManager.getCurrentMachine();
            if (current != SlimefunDetector.SlimefunMachine.UNKNOWN) {
                context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("§7Current: §f" + current.getDisplayName()),
                    cx, 28, 0xAAAAAA
                );
            } else {
                context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("§7Open a Slimefun machine to auto-detect"),
                    cx, 28, 0x888888
                );
            }
        }
        
        // Section header
        context.drawTextWithShadow(
            this.textRenderer,
            Text.literal("§e§lQuick Presets:"),
            cx - 150, 120, 0xFFFF55
        );
        
        // Status info
        String statusInfo = slimefunManager.getStatusInfo();
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal(statusInfo),
            cx, this.height - 105, 0xAAAAAA
        );
        
        // Footer hints
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§8Hotkey: [\\] for Quick Setup | Press [?] for full guide"),
            cx, this.height - 15, 0x666666
        );
    }
    
    @Override
    public void close() {
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