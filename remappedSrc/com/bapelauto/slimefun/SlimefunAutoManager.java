// ============================================
// FILE: SlimefunAutoManager.java (UPDATED with Input Feeder & Recipe System)
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
 * Complete Slimefun automation manager (ENHANCED VERSION)
 * Now includes intelligent input feeding and recipe management
 */
public class SlimefunAutoManager {
    
    private boolean slimefunModeEnabled = false;
    private SlimefunDetector.SlimefunMachine currentMachine = SlimefunDetector.SlimefunMachine.UNKNOWN;
    private long lastAutoDetectTime = 0;
    private static final long AUTO_DETECT_COOLDOWN = 2000;
    
    // NEW: Input Feeder System
    private final SlimefunInputFeeder inputFeeder;
    
    // NEW: Recipe Feeder System
    private final RecipeFeeder recipeFeeder;
    
    // Statistics
    private int totalSlimefunClicks = 0;
    private int totalItemsCollected = 0;
    private final Map<SlimefunDetector.SlimefunMachine, Integer> machineUsageCount = new HashMap<>();
    
    // Safety features
    private boolean safetyMode = true;
    private int consecutiveFailures = 0;
    private static final int MAX_FAILURES = 5;
    
    // Operating modes
    private boolean autoInputEnabled = false;
    private boolean autoRecipeEnabled = false;
    
    public SlimefunAutoManager() {
        this.inputFeeder = new SlimefunInputFeeder();
        this.recipeFeeder = new RecipeFeeder();
    }
    
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
        
        // NEW: Tick input feeder if enabled
        if (autoInputEnabled && inputFeeder.isEnabled()) {
            inputFeeder.tick(client, currentMachine);
        }
        
        // NEW: Tick recipe feeder if enabled
        if (autoRecipeEnabled && recipeFeeder.isEnabled()) {
            recipeFeeder.tick(client);
        }
    }
    
    /**
     * Auto-detect Slimefun machine and configure bot
     */
    private void autoDetectAndConfigure(MinecraftClient client) {
        if (!(client.currentScreen instanceof HandledScreen)) return;
        
        if (!SlimefunDetector.isSlimefunGUI(client.currentScreen)) {
            return;
        }
        
        SlimefunDetector.SlimefunMachine detected = SlimefunDetector.detectMachineType(client.currentScreen);
        
        if (detected != currentMachine && detected != SlimefunDetector.SlimefunMachine.UNKNOWN) {
            currentMachine = detected;
            onMachineDetected(client, detected);
            
            // NEW: Auto-configure input feeder for detected machine
            if (autoInputEnabled) {
                inputFeeder.configureForMachine(client, detected);
            }
        }
    }
    
    /**
     * Called when a new Slimefun machine is detected
     */
    private void onMachineDetected(MinecraftClient client, SlimefunDetector.SlimefunMachine machine) {
        machineUsageCount.put(machine, machineUsageCount.getOrDefault(machine, 0) + 1);
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§a[Slimefun] Detected: " + machine.getDisplayName()), true
            );
        }
        
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
        
        // Disable sub-systems
        autoInputEnabled = false;
        autoRecipeEnabled = false;
        inputFeeder.setEnabled(false);
        recipeFeeder.setEnabled(false);
        
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
            var guiClickManager = AutoBotMod.getGuiClickManager();
            guiClickManager.clearTargets();
            
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
        
        // NEW: Enable input feeding for suitable machines
        if (shouldEnableAutoInput(machine)) {
            enableAutoInput(client);
            inputFeeder.configureForMachine(client, machine);
        }
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
    
    // ========================================
    // NEW: AUTO-INPUT MANAGEMENT
    // ========================================
    
    /**
     * Enable auto-input feeding
     */
    public void enableAutoInput(MinecraftClient client) {
        autoInputEnabled = true;
        inputFeeder.setEnabled(true);
        
        if (currentMachine != SlimefunDetector.SlimefunMachine.UNKNOWN) {
            inputFeeder.configureForMachine(client, currentMachine);
        }
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§a[Auto-Input] ENABLED - Bot will automatically feed items to machine"),
                true
            );
        }
    }
    
    /**
     * Disable auto-input feeding
     */
    public void disableAutoInput(MinecraftClient client) {
        autoInputEnabled = false;
        inputFeeder.setEnabled(false);
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§c[Auto-Input] DISABLED"),
                true
            );
        }
    }
    
    /**
     * Toggle auto-input
     */
    public void toggleAutoInput(MinecraftClient client) {
        if (autoInputEnabled) {
            disableAutoInput(client);
        } else {
            enableAutoInput(client);
        }
    }
    
    /**
     * Check if auto-input should be enabled for this machine
     */
    private boolean shouldEnableAutoInput(SlimefunDetector.SlimefunMachine machine) {
        // Enable for machines that need continuous input
        return machine == SlimefunDetector.SlimefunMachine.COAL_GENERATOR ||
               machine == SlimefunDetector.SlimefunMachine.BIO_REACTOR ||
               machine == SlimefunDetector.SlimefunMachine.CARBON_PRESS ||
               machine == SlimefunDetector.SlimefunMachine.ELECTRIC_FURNACE ||
               machine == SlimefunDetector.SlimefunMachine.ELECTRIC_ORE_GRINDER;
    }
    
    // ========================================
    // NEW: RECIPE MANAGEMENT
    // ========================================
    
    /**
     * Enable auto-recipe feeding
     */
    public void enableAutoRecipe(MinecraftClient client) {
        autoRecipeEnabled = true;
        recipeFeeder.setEnabled(true);
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§a[Auto-Recipe] ENABLED - Bot will maintain crafting recipe"),
                true
            );
            
            if (recipeFeeder.getCurrentRecipe() == null) {
                client.player.sendMessage(
                    Text.literal("§7Use '/autobot recipe learn <name>' to learn current recipe"),
                    false
                );
            }
        }
    }
    
    /**
     * Disable auto-recipe feeding
     */
    public void disableAutoRecipe(MinecraftClient client) {
        autoRecipeEnabled = false;
        recipeFeeder.setEnabled(false);
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§c[Auto-Recipe] DISABLED"),
                true
            );
        }
    }
    
    /**
     * Toggle auto-recipe
     */
    public void toggleAutoRecipe(MinecraftClient client) {
        if (autoRecipeEnabled) {
            disableAutoRecipe(client);
        } else {
            enableAutoRecipe(client);
        }
    }
    
    /**
     * Learn recipe from current crafting grid
     */
    public void learnRecipe(MinecraftClient client, String recipeName) {
        RecipeFeeder.Recipe recipe = recipeFeeder.learnRecipe(client, recipeName);
        
        if (recipe != null) {
            recipeFeeder.setRecipe(recipe);
            recipeFeeder.saveRecipe(recipe);
            
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§a[Recipe] Learned and activated: " + recipeName),
                    true
                );
            }
        }
    }
    
    /**
     * Load and set recipe
     */
    public void loadRecipe(MinecraftClient client, String recipeName) {
        RecipeFeeder.Recipe recipe = recipeFeeder.loadRecipe(recipeName);
        
        if (recipe != null) {
            recipeFeeder.setRecipe(recipe);
            
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§a[Recipe] Loaded: " + recipeName),
                    true
                );
            }
        } else {
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§c[Recipe] Not found: " + recipeName),
                    false
                );
            }
        }
    }
    
    /**
     * List available recipes
     */
    public void listRecipes(MinecraftClient client) {
        if (client.player == null) return;
        
        var recipes = recipeFeeder.getAllRecipes();
        
        if (recipes.isEmpty()) {
            client.player.sendMessage(
                Text.literal("§c[Recipe] No saved recipes"),
                false
            );
            return;
        }
        
        client.player.sendMessage(
            Text.literal("§e[Recipe] Saved Recipes:"),
            false
        );
        
        for (RecipeFeeder.Recipe recipe : recipes) {
            client.player.sendMessage(
                Text.literal("  §7- §f" + recipe.getName()),
                false
            );
        }
    }
    
    // ========================================
    // STATUS & STATISTICS
    // ========================================
    
    /**
     * Get status info
     */
    public String getStatusInfo() {
        if (!slimefunModeEnabled) {
            return "§7Slimefun Mode: §cOFF";
        }
        
        StringBuilder status = new StringBuilder("§7Slimefun: §aON");
        
        if (currentMachine != SlimefunDetector.SlimefunMachine.UNKNOWN) {
            status.append(" §7| §f").append(currentMachine.getDisplayName());
        }
        
        status.append(" §7| Clicks: §f").append(totalSlimefunClicks);
        
        if (autoInputEnabled) {
            status.append(" §7| §aInput: ON");
        }
        
        if (autoRecipeEnabled) {
            status.append(" §7| §aRecipe: ON");
        }
        
        return status.toString();
    }
    
    /**
     * Get detailed status for all systems
     */
    public void showDetailedStatus(MinecraftClient client) {
        if (client.player == null) return;
        
        client.player.sendMessage(
            Text.literal("§e§l=== Slimefun Status ==="),
            false
        );
        
        client.player.sendMessage(
            Text.literal("§7Slimefun Mode: " + (slimefunModeEnabled ? "§aON" : "§cOFF")),
            false
        );
        
        if (currentMachine != SlimefunDetector.SlimefunMachine.UNKNOWN) {
            client.player.sendMessage(
                Text.literal("§7Current Machine: §f" + currentMachine.getDisplayName()),
                false
            );
        }
        
        client.player.sendMessage(
            Text.literal("§7" + inputFeeder.getStatusInfo()),
            false
        );
        
        client.player.sendMessage(
            Text.literal("§7" + recipeFeeder.getStatusInfo()),
            false
        );
        
        client.player.sendMessage(
            Text.literal("§7Total Clicks: §f" + totalSlimefunClicks),
            false
        );
        
        client.player.sendMessage(
            Text.literal("§7Items Collected: §f" + totalItemsCollected),
            false
        );
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
        client.player.sendMessage(
            Text.literal("§7Items Fed: §f" + inputFeeder.getTotalItemsFed()), false
        );
        client.player.sendMessage(
            Text.literal("§7Recipe Items Placed: §f" + recipeFeeder.getTotalItemsPlaced()), false
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
        inputFeeder.reset();
        recipeFeeder.reset();
    }
    
    /**
     * Increment click counter
     */
    public void incrementClicks() {
        totalSlimefunClicks++;
        consecutiveFailures = 0;
    }
    
    /**
     * Increment items collected
     */
    public void incrementItemsCollected(int count) {
        totalItemsCollected += count;
    }
    
    /**
     * Report failure
     */
    public void reportFailure() {
        consecutiveFailures++;
    }
    
    // ========================================
    // GETTERS & SETTERS
    // ========================================
    
    public boolean isSlimefunModeEnabled() { return slimefunModeEnabled; }
    public void setSlimefunModeEnabled(boolean enabled) { this.slimefunModeEnabled = enabled; }
    
    public SlimefunDetector.SlimefunMachine getCurrentMachine() { return currentMachine; }
    
    public boolean isSafetyMode() { return safetyMode; }
    public void setSafetyMode(boolean enabled) { this.safetyMode = enabled; }
    
    public int getTotalSlimefunClicks() { return totalSlimefunClicks; }
    public int getTotalItemsCollected() { return totalItemsCollected; }
    
    // NEW: Getters for sub-systems
    public boolean isAutoInputEnabled() { return autoInputEnabled; }
    public boolean isAutoRecipeEnabled() { return autoRecipeEnabled; }
    
    public SlimefunInputFeeder getInputFeeder() { return inputFeeder; }
    public RecipeFeeder getRecipeFeeder() { return recipeFeeder; }
    
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