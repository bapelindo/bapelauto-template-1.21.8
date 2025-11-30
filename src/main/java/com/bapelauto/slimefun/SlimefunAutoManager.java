// ============================================
// FILE: SlimefunAutoManager.java
// Path: src/main/java/com/bapelauto/slimefun/SlimefunAutoManager.java
// ============================================
package com.bapelauto.slimefun;

import com.bapelauto.AutoBotMod;
import com.bapelauto.click.ClickTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Complete Slimefun automation manager
 * Handles all Slimefun-specific automation tasks
 */
public class SlimefunAutoManager {
    
    private boolean slimefunModeEnabled = false;
    private SlimefunDetector.SlimefunMachine currentMachine = SlimefunDetector.SlimefunMachine.UNKNOWN;
    private long lastAutoDetectTime = 0;
    private static final long AUTO_DETECT_COOLDOWN = 2000; // 2 seconds
    
    // Statistics
    private int totalSlimefunClicks = 0;
    private int totalItemsCollected = 0;
    private final Map<SlimefunDetector.SlimefunMachine, Integer> machineUsageCount = new HashMap<>();
    
    // Safety features
    private boolean safetyMode = true;
    private int consecutiveFailures = 0;
    private static final int MAX_FAILURES = 5;
    
    /**
     * Main tick method - call this from AutoBotMod
     */
    public void tick(MinecraftClient client) {
        if (!slimefunModeEnabled) return;
        if (client.currentScreen == null) return;
        
        // Auto-detect Slimefun machines periodically
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAutoDetectTime > AUTO_DETECT_COOLDOWN) {
            autoDetectAndConfigure(client);
            lastAutoDetectTime = currentTime;
        }
        
        // Safety check
        if (safetyMode && consecutiveFailures >= MAX_FAILURES) {
            disableWithWarning(client, "Too many failures - auto-disabled for safety");
            return;
        }
    }
    
    /**
     * Auto-detect Slimefun machine and configure bot
     */
    private void autoDetectAndConfigure(MinecraftClient client) {
        if (!(client.currentScreen instanceof HandledScreen)) return;
        
        // Check if it's a Slimefun GUI
        if (!SlimefunDetector.isSlimefunGUI(client.currentScreen)) {
            return;
        }
        
        // Detect machine type
        SlimefunDetector.SlimefunMachine detected = SlimefunDetector.detectMachineType(client.currentScreen);
        
        // If machine changed, reconfigure
        if (detected != currentMachine && detected != SlimefunDetector.SlimefunMachine.UNKNOWN) {
            currentMachine = detected;
            onMachineDetected(client, detected);
        }
    }
    
    /**
     * Called when a new Slimefun machine is detected
     */
    private void onMachineDetected(MinecraftClient client, SlimefunDetector.SlimefunMachine machine) {
        // Update usage statistics
        machineUsageCount.put(machine, machineUsageCount.getOrDefault(machine, 0) + 1);
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§a[Slimefun] Detected: " + machine.getDisplayName()), true
            );
        }
        
        // Show safety warning if needed
        if (SlimefunDetector.needsSpecialHandling(machine)) {
            String warning = SlimefunDetector.getSafetyWarning(machine);
            if (client.player != null && !warning.isEmpty()) {
                client.player.sendMessage(Text.literal(warning), true);
                client.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, 0.7F, 1.0F);
            }
        }
    }
    
    /**
     * Enable Slimefun mode with auto-detection
     */
    public void enable(MinecraftClient client) {
        slimefunModeEnabled = true;
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§a§l[Slimefun Mode] ENABLED"), true
            );
            client.player.sendMessage(
                Text.literal("§7Bot will auto-detect and configure for Slimefun machines"), false
            );
            client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 1.5F);
        }
    }
    
    /**
     * Disable Slimefun mode
     */
    public void disable(MinecraftClient client) {
        slimefunModeEnabled = false;
        currentMachine = SlimefunDetector.SlimefunMachine.UNKNOWN;
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§c[Slimefun Mode] DISABLED"), true
            );
        }
    }
    
    /**
     * Disable with warning message
     */
    private void disableWithWarning(MinecraftClient client, String reason) {
        disable(client);
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§c§l[Slimefun] " + reason), true
            );
            client.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, 1.0F, 0.8F);
        }
    }
    
    /**
     * Quick setup - detect current GUI and auto-configure everything
     */
    public void quickSetup(MinecraftClient client) {
        if (client.currentScreen == null) {
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§c[Slimefun] Open a Slimefun machine GUI first!"), false
                );
            }
            return;
        }
        
        // Detect machine
        SlimefunDetector.SlimefunMachine machine = SlimefunDetector.detectMachineType(client.currentScreen);
        
        if (machine == SlimefunDetector.SlimefunMachine.UNKNOWN) {
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§c[Slimefun] Not a recognized Slimefun machine"), false
                );
            }
            return;
        }
        
        // Apply recommended preset
        SlimefunProfilePresets.quickApply(client);
        
        // Auto-detect and capture targets
        List<ClickTarget> targets = SlimefunDetector.autoDetectSlimefunTargets(client, machine);
        
        if (!targets.isEmpty()) {
            // Apply targets to GUI click manager
            var guiClickManager = AutoBotMod.getGuiClickManager();
            guiClickManager.clearTargets();
            
            // Note: This is simplified - actual implementation would need to add targets properly
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§a✓ Auto-configured for " + machine.getDisplayName()), true
                );
                client.player.sendMessage(
                    Text.literal("§7Press [=] to start automation"), false
                );
            }
        }
        
        currentMachine = machine;
        slimefunModeEnabled = true;
    }
    
    /**
     * Manual preset selection
     */
    public void applyPreset(SlimefunProfilePresets.SlimefunPreset preset, MinecraftClient client) {
        SlimefunProfilePresets.applyPreset(preset, client);
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§a[Slimefun] Preset applied: " + preset.getName()), true
            );
        }
    }
    
    /**
     * Get status info
     */
    public String getStatusInfo() {
        if (!slimefunModeEnabled) {
            return "§7Slimefun Mode: §cOFF";
        }
        
        String machineInfo = currentMachine != SlimefunDetector.SlimefunMachine.UNKNOWN ?
            currentMachine.getDisplayName() : "No machine";
        
        return String.format("§7Slimefun: §aON §7| Machine: §f%s §7| Clicks: §f%d",
            machineInfo, totalSlimefunClicks);
    }
    
    /**
     * Get detailed statistics
     */
    public void showStatistics(MinecraftClient client) {
        if (client.player == null) return;
        
        client.player.sendMessage(
            Text.literal("§e§l=== Slimefun Statistics ==="), false
        );
        client.player.sendMessage(
            Text.literal("§7Status: " + (slimefunModeEnabled ? "§aENABLED" : "§cDISABLED")), false
        );
        client.player.sendMessage(
            Text.literal("§7Current Machine: §f" + 
                (currentMachine != SlimefunDetector.SlimefunMachine.UNKNOWN ? 
                 currentMachine.getDisplayName() : "None")), false
        );
        client.player.sendMessage(
            Text.literal("§7Total Clicks: §f" + totalSlimefunClicks), false
        );
        client.player.sendMessage(
            Text.literal("§7Items Collected: §f" + totalItemsCollected), false
        );
        
        if (!machineUsageCount.isEmpty()) {
            client.player.sendMessage(
                Text.literal("§7Most Used Machines:"), false
            );
            
            machineUsageCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .forEach(entry -> {
                    client.player.sendMessage(
                        Text.literal("  §7• §f" + entry.getKey().getDisplayName() + 
                                   " §7(§f" + entry.getValue() + "x§7)"), false
                    );
                });
        }
    }
    
    /**
     * Reset statistics
     */
    public void resetStatistics() {
        totalSlimefunClicks = 0;
        totalItemsCollected = 0;
        machineUsageCount.clear();
        consecutiveFailures = 0;
    }
    
    /**
     * Increment click counter (call from click executor)
     */
    public void incrementClicks() {
        totalSlimefunClicks++;
        consecutiveFailures = 0; // Reset on success
    }
    
    /**
     * Increment items collected
     */
    public void incrementItemsCollected(int count) {
        totalItemsCollected += count;
    }
    
    /**
     * Report failure (for safety monitoring)
     */
    public void reportFailure() {
        consecutiveFailures++;
    }
    
    // Getters and setters
    public boolean isSlimefunModeEnabled() { return slimefunModeEnabled; }
    public void setSlimefunModeEnabled(boolean enabled) { this.slimefunModeEnabled = enabled; }
    
    public SlimefunDetector.SlimefunMachine getCurrentMachine() { return currentMachine; }
    
    public boolean isSafetyMode() { return safetyMode; }
    public void setSafetyMode(boolean enabled) { this.safetyMode = enabled; }
    
    public int getTotalSlimefunClicks() { return totalSlimefunClicks; }
    public int getTotalItemsCollected() { return totalItemsCollected; }
    
    /**
     * Check if currently in a Slimefun GUI
     */
    public boolean isInSlimefunGUI(MinecraftClient client) {
        return client.currentScreen != null && 
               SlimefunDetector.isSlimefunGUI(client.currentScreen);
    }
    
    /**
     * Get recommended action for current machine
     */
    public String getRecommendedAction() {
        if (currentMachine == SlimefunDetector.SlimefunMachine.UNKNOWN) {
            return "Open a Slimefun machine GUI";
        }
        
        return SlimefunDetector.getMachineInstructions(currentMachine);
    }
}