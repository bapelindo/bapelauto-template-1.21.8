// ============================================
// FILE: VisualOverlay.java
// Path: src/main/java/com/bapelauto/visual/VisualOverlay.java
// ============================================
package com.bapelauto.visual;

import com.bapelauto.AutoBotMod;
import com.bapelauto.click.ClickTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * Visual overlay for real-time feedback and target visualization
 */
public class VisualOverlay {
    
    private boolean enabled = true;
    private boolean showTargets = true;
    private boolean showStats = true;
    private boolean showClickIndicator = false;
    
    private final List<ClickIndicator> clickIndicators = new ArrayList<>();
    
    /**
     * Render overlay on GUI screens
     */
    public void renderGuiOverlay(DrawContext context, MinecraftClient client, int mouseX, int mouseY) {
        if (!enabled) return;
        
        // Draw targets
        if (showTargets && client.currentScreen instanceof HandledScreen) {
            renderTargets(context, client);
        }
        
        // Draw stats HUD
        if (showStats) {
            renderStatsHud(context, client);
        }
        
        // Draw click indicators
        if (showClickIndicator) {
            renderClickIndicators(context);
        }
    }
    
    /**
     * Render overlay in world (HUD)
     */
    public void renderWorldOverlay(DrawContext context, MinecraftClient client) {
        if (!enabled) return;
        
        // Status indicator in corner
        renderStatusIndicator(context, client);
        
        // Active features list
        if (showStats) {
            renderActiveFeatures(context, client);
        }
    }
    
    private void renderTargets(DrawContext context, MinecraftClient client) {
        var guiClickManager = AutoBotMod.getGuiClickManager();
        if (guiClickManager == null || !guiClickManager.hasTargets()) return;
        
        HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;
        
        // Highlight captured targets
        // Note: This is a simplified version - actual slot highlighting requires more screen details
        
        // Draw target count
        String targetText = "§6Targets: " + guiClickManager.getTargetCount();
        context.drawTextWithShadow(
            client.textRenderer,
            targetText,
            5, 5,
            0xFFFFAA
        );
        
        // Draw mode and pattern
        String modeText = "§7Mode: §f" + guiClickManager.getCurrentMode().getDisplayName();
        context.drawTextWithShadow(
            client.textRenderer,
            modeText,
            5, 17,
            0xAAAAAA
        );
        
        String patternText = "§7Pattern: §f" + guiClickManager.getTimingPattern().getDisplayName();
        context.drawTextWithShadow(
            client.textRenderer,
            patternText,
            5, 29,
            0xAAAAAA
        );
    }
    
    private void renderStatsHud(DrawContext context, MinecraftClient client) {
        if (!AutoBotMod.isRunning()) return;
        
        int x = 5;
        int y = client.getWindow().getScaledHeight() - 100;
        
        // Background
        context.fill(x - 2, y - 2, x + 150, y + 75, 0x88000000);
        
        // Title
        context.drawTextWithShadow(
            client.textRenderer,
            "§6§l[AutoBot Stats]",
            x, y,
            0xFFAA00
        );
        y += 12;
        
        // Stats
        var statsTracker = AutoBotMod.getStatsTracker();
        var worldManager = AutoBotMod.getWorldManager();
        var guiClickManager = AutoBotMod.getGuiClickManager();
        var inventoryManager = AutoBotMod.getInventoryManager();
        
        String[] stats = {
            "§7World: §f" + worldManager.getTotalClicks(),
            "§7GUI: §f" + guiClickManager.getTotalClicks(),
            "§7Items: §f" + inventoryManager.getTotalItemsMoved(),
            "§7Cmds: §f" + statsTracker.getTotalCommands(),
            "§7Time: §f" + statsTracker.getFormattedDuration()
        };
        
        for (String stat : stats) {
            context.drawTextWithShadow(
                client.textRenderer,
                stat,
                x, y,
                0xAAAAAA
            );
            y += 11;
        }
    }
    
    private void renderStatusIndicator(DrawContext context, MinecraftClient client) {
        int x = client.getWindow().getScaledWidth() - 120;
        int y = 5;
        
        // Status box
        int color = AutoBotMod.isRunning() ? 0x8800FF00 : 0x88FF0000;
        context.fill(x, y, x + 115, y + 20, color);
        
        // Status text
        String status = AutoBotMod.isRunning() ? "§a§lACTIVE" : "§c§lINACTIVE";
        context.drawCenteredTextWithShadow(
            client.textRenderer,
            status,
            x + 57, y + 6,
            0xFFFFFF
        );
    }
    
    private void renderActiveFeatures(DrawContext context, MinecraftClient client) {
        int x = client.getWindow().getScaledWidth() - 120;
        int y = 30;
        
        List<String> activeFeatures = new ArrayList<>();
        
        var worldManager = AutoBotMod.getWorldManager();
        var inventoryManager = AutoBotMod.getInventoryManager();
        var guiClickManager = AutoBotMod.getGuiClickManager();
        
        if (worldManager.isLeftClickEnabled()) activeFeatures.add("§7• §aLeft Click");
        if (worldManager.isRightClickEnabled()) activeFeatures.add("§7• §aRight Click");
        if (inventoryManager.isAutoStealEnabled()) activeFeatures.add("§7• §aAuto Steal");
        if (inventoryManager.isAutoStoreEnabled()) activeFeatures.add("§7• §aAuto Store");
        if (guiClickManager.isActive()) activeFeatures.add("§7• §aGUI Click");
        if (AutoBotMod.isCommandEnabled()) activeFeatures.add("§7• §aAuto Command");
        
        if (activeFeatures.isEmpty()) return;
        
        // Background
        context.fill(x, y, x + 115, y + (activeFeatures.size() * 11) + 15, 0x88000000);
        
        // Title
        context.drawTextWithShadow(
            client.textRenderer,
            "§e§lActive Features",
            x + 5, y + 3,
            0xFFFF55
        );
        y += 15;
        
        // Features list
        for (String feature : activeFeatures) {
            context.drawTextWithShadow(
                client.textRenderer,
                feature,
                x + 5, y,
                0xAAAAAA
            );
            y += 11;
        }
    }
    
    private void renderClickIndicators(DrawContext context) {
        long currentTime = System.currentTimeMillis();
        
        clickIndicators.removeIf(indicator -> 
            currentTime - indicator.time > 500
        );
        
        for (ClickIndicator indicator : clickIndicators) {
            float alpha = 1.0f - ((currentTime - indicator.time) / 500.0f);
            int alphaInt = (int)(alpha * 255);
            int color = (alphaInt << 24) | 0x00FF00;
            
            // Draw a small circle or cross at click position
            context.fill(
                (int)indicator.x - 2, (int)indicator.y - 2,
                (int)indicator.x + 2, (int)indicator.y + 2,
                color
            );
        }
    }
    
    /**
     * Add click indicator at position
     */
    public void addClickIndicator(double x, double y) {
        if (!showClickIndicator) return;
        clickIndicators.add(new ClickIndicator(x, y, System.currentTimeMillis()));
    }
    
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public boolean isShowTargets() { return showTargets; }
    public void setShowTargets(boolean show) { this.showTargets = show; }
    
    public boolean isShowStats() { return showStats; }
    public void setShowStats(boolean show) { this.showStats = show; }
    
    public boolean isShowClickIndicator() { return showClickIndicator; }
    public void setShowClickIndicator(boolean show) { this.showClickIndicator = show; }
    
    /**
     * Toggle all overlays
     */
    public void toggleAll() {
        enabled = !enabled;
    }
    
    private static class ClickIndicator {
        final double x, y;
        final long time;
        
        ClickIndicator(double x, double y, long time) {
            this.x = x;
            this.y = y;
            this.time = time;
        }
    }
}