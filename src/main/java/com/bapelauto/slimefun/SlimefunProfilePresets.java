// ============================================
// FILE: SlimefunProfilePresets.java
// Path: src/main/java/com/bapelauto/slimefun/SlimefunProfilePresets.java
// ============================================
package com.bapelauto.slimefun;

import com.bapelauto.AutoBotMod;
import com.bapelauto.click.TimingPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Pre-configured profiles for common Slimefun setups
 */
public class SlimefunProfilePresets {
    
    public enum SlimefunPreset {
        // Auto-Collection Presets
        AUTO_SMELTERY(
            "Slimefun: Auto-Smeltery",
            "Automatic smeltery output collection",
            createSmelteryConfig()
        ),
        AUTO_ELECTRIC_MACHINES(
            "Slimefun: Electric Machines",
            "Fast electric machine output collection",
            createElectricMachineConfig()
        ),
        AUTO_ORE_PROCESSING(
            "Slimefun: Ore Processing",
            "Complete ore processing chain automation",
            createOreProcessingConfig()
        ),
        
        // Crafting Presets
        AUTO_ENHANCED_CRAFTING(
            "Slimefun: Enhanced Crafting",
            "Auto-craft with Enhanced Crafting Table",
            createEnhancedCraftingConfig()
        ),
        AUTO_ARMOR_FORGE(
            "Slimefun: Armor Forge",
            "Automatic armor crafting",
            createArmorForgeConfig()
        ),
        AUTO_MAGIC_WORKBENCH(
            "Slimefun: Magic Workbench",
            "Magic item auto-crafting",
            createMagicWorkbenchConfig()
        ),
        
        // Advanced Presets
        AUTO_GEO_MINER(
            "Slimefun: GEO Miner",
            "Automatic GEO resource collection",
            createGeoMinerConfig()
        ),
        AUTO_REACTOR_MONITOR(
            "Slimefun: Reactor Monitor",
            "Safe reactor output monitoring (SLOW & SAFE)",
            createReactorMonitorConfig()
        ),
        AUTO_FOOD_PRODUCTION(
            "Slimefun: Food Production",
            "Automated food machine collection",
            createFoodProductionConfig()
        ),
        
        // Cargo Presets
        CARGO_SYSTEM(
            "Slimefun: Cargo System",
            "Cargo node automation",
            createCargoConfig()
        ),
        
        // Multi-Machine Presets
        FULL_AUTO_FACTORY(
            "Slimefun: Full Auto Factory",
            "Complete factory automation (all machines)",
            createFullAutoFactoryConfig()
        ),
        ENERGY_NETWORK(
            "Slimefun: Energy Network",
            "Generator and energy management",
            createEnergyNetworkConfig()
        );
        
        private final String name;
        private final String description;
        private final Map<String, String> config;
        
        SlimefunPreset(String name, String description, Map<String, String> config) {
            this.name = name;
            this.description = description;
            this.config = config;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Map<String, String> getConfig() { return config; }
    }
    
    // Config Builders
    
    private static Map<String, String> createSmelteryConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "FIXED");
        config.put("targetClickDelay", "150");
        config.put("minDelay", "100");
        config.put("maxDelay", "200");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "false");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        config.put("showGuiButtons", "true");
        return config;
    }
    
    private static Map<String, String> createElectricMachineConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "BURST");
        config.put("targetClickDelay", "100");
        config.put("minDelay", "50");
        config.put("maxDelay", "150");
        config.put("burstCount", "5");
        config.put("burstPause", "2000");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "false");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        return config;
    }
    
    private static Map<String, String> createOreProcessingConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "RANDOMIZED");
        config.put("targetClickDelay", "120");
        config.put("minDelay", "80");
        config.put("maxDelay", "180");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "true"); // Auto-store processed ores
        config.put("inventoryDelay", "150");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        return config;
    }
    
    private static Map<String, String> createEnhancedCraftingConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "FIXED");
        config.put("targetClickDelay", "150");
        config.put("minDelay", "120");
        config.put("maxDelay", "200");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "false");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        return config;
    }
    
    private static Map<String, String> createArmorForgeConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "FIXED");
        config.put("targetClickDelay", "150");
        config.put("minDelay", "120");
        config.put("maxDelay", "200");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "false");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        return config;
    }
    
    private static Map<String, String> createMagicWorkbenchConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "RANDOMIZED");
        config.put("targetClickDelay", "180");
        config.put("minDelay", "150");
        config.put("maxDelay", "250");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "false");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        return config;
    }
    
    private static Map<String, String> createGeoMinerConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "FIXED");
        config.put("targetClickDelay", "200");
        config.put("minDelay", "150");
        config.put("maxDelay", "300");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "true");
        config.put("inventoryDelay", "200");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        return config;
    }
    
    private static Map<String, String> createReactorMonitorConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "FIXED");
        config.put("targetClickDelay", "500"); // VERY SLOW for safety
        config.put("minDelay", "400");
        config.put("maxDelay", "600");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "false");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        config.put("showGuiButtons", "true");
        return config;
    }
    
    private static Map<String, String> createFoodProductionConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "BURST");
        config.put("targetClickDelay", "120");
        config.put("minDelay", "80");
        config.put("maxDelay", "180");
        config.put("burstCount", "5");
        config.put("burstPause", "2000");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "true");
        config.put("inventoryDelay", "150");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        return config;
    }
    
    private static Map<String, String> createCargoConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "FIXED");
        config.put("targetClickDelay", "100");
        config.put("minDelay", "80");
        config.put("maxDelay", "150");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "false");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        return config;
    }
    
    private static Map<String, String> createFullAutoFactoryConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "RANDOMIZED");
        config.put("targetClickDelay", "150");
        config.put("minDelay", "100");
        config.put("maxDelay", "250");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "true");
        config.put("inventoryDelay", "180");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "true");
        config.put("command", "/sf research"); // Example command
        config.put("commandDelay", "300000"); // 5 minutes
        return config;
    }
    
    private static Map<String, String> createEnergyNetworkConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("timingPattern", "FIXED");
        config.put("targetClickDelay", "200");
        config.put("minDelay", "150");
        config.put("maxDelay", "300");
        config.put("autoStealEnabled", "false");
        config.put("autoStoreEnabled", "false");
        config.put("leftClickEnabled", "false");
        config.put("rightClickEnabled", "false");
        config.put("commandEnabled", "false");
        return config;
    }
    
    /**
     * Apply a preset configuration
     */
    public static void applyPreset(SlimefunPreset preset, MinecraftClient client) {
        var configManager = AutoBotMod.getConfigManager();
        var guiClickManager = AutoBotMod.getGuiClickManager();
        var inventoryManager = AutoBotMod.getInventoryManager();
        
        // Apply all config values
        Map<String, String> config = preset.getConfig();
        config.forEach(configManager::set);
        
        // Apply timing pattern
        String patternStr = config.get("timingPattern");
        if (patternStr != null) {
            try {
                TimingPattern pattern = TimingPattern.valueOf(patternStr);
                guiClickManager.setTimingPattern(pattern);
            } catch (Exception e) {
                // Use default
            }
        }
        
        // Apply delays
        guiClickManager.setBaseDelay(Long.parseLong(config.getOrDefault("targetClickDelay", "100")));
        guiClickManager.setMinDelay(Long.parseLong(config.getOrDefault("minDelay", "50")));
        guiClickManager.setMaxDelay(Long.parseLong(config.getOrDefault("maxDelay", "200")));
        
        if (config.containsKey("burstCount")) {
            guiClickManager.setBurstCount(Integer.parseInt(config.get("burstCount")));
        }
        if (config.containsKey("burstPause")) {
            guiClickManager.setBurstPause(Long.parseLong(config.get("burstPause")));
        }
        
        // Apply inventory settings
        inventoryManager.setAutoStealEnabled(Boolean.parseBoolean(config.getOrDefault("autoStealEnabled", "false")));
        inventoryManager.setAutoStoreEnabled(Boolean.parseBoolean(config.getOrDefault("autoStoreEnabled", "false")));
        if (config.containsKey("inventoryDelay")) {
            inventoryManager.setInventoryDelay(Long.parseLong(config.get("inventoryDelay")));
        }
        
        // Save config
        configManager.saveConfig();
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§a[Preset] Loaded: " + preset.getName()), true
            );
            client.player.sendMessage(
                Text.literal("§7" + preset.getDescription()), false
            );
            client.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 1.5F);
        }
    }
    
    /**
     * Get recommended preset for detected machine
     */
    public static SlimefunPreset getRecommendedPreset(SlimefunDetector.SlimefunMachine machine) {
        switch (machine) {
            case SMELTERY:
            case ORE_WASHER:
                return SlimefunPreset.AUTO_SMELTERY;
                
            case ELECTRIC_FURNACE:
            case ELECTRIC_ORE_GRINDER:
            case ELECTRIC_DUST_WASHER:
            case ELECTRIC_SMELTERY:
            case ELECTRIFIED_CRUCIBLE:
                return SlimefunPreset.AUTO_ELECTRIC_MACHINES;
                
            case ORE_CRUSHER:
            case GRIND_STONE:
            case COMPRESSOR:
            case PRESSURE_CHAMBER:
                return SlimefunPreset.AUTO_ORE_PROCESSING;
                
            case ENHANCED_CRAFTING_TABLE:
            case AUTO_CRAFTER:
            case ENHANCED_AUTO_CRAFTER:
                return SlimefunPreset.AUTO_ENHANCED_CRAFTING;
                
            case ARMOR_FORGE:
            case ARMOR_AUTO_CRAFTER:
                return SlimefunPreset.AUTO_ARMOR_FORGE;
                
            case MAGIC_WORKBENCH:
                return SlimefunPreset.AUTO_MAGIC_WORKBENCH;
                
            case GEO_MINER:
            case OIL_PUMP:
                return SlimefunPreset.AUTO_GEO_MINER;
                
            case NUCLEAR_REACTOR:
            case NETHER_STAR_REACTOR:
            case BIO_REACTOR:
                return SlimefunPreset.AUTO_REACTOR_MONITOR;
                
            case FOOD_FABRICATOR:
            case FOOD_COMPOSTER:
            case FREEZER:
                return SlimefunPreset.AUTO_FOOD_PRODUCTION;
                
            case CARGO_INPUT_NODE:
            case CARGO_OUTPUT_NODE:
                return SlimefunPreset.CARGO_SYSTEM;
                
            default:
                return SlimefunPreset.AUTO_ELECTRIC_MACHINES; // Safe default
        }
    }
    
    /**
     * Quick apply - detect machine and apply best preset
     */
    public static void quickApply(MinecraftClient client) {
        if (client.currentScreen == null) return;
        
        SlimefunDetector.SlimefunMachine machine = SlimefunDetector.detectMachineType(client.currentScreen);
        
        if (machine == SlimefunDetector.SlimefunMachine.UNKNOWN) {
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§c[Slimefun] Not a recognized Slimefun machine"), false
                );
            }
            return;
        }
        
        SlimefunPreset preset = getRecommendedPreset(machine);
        applyPreset(preset, client);
        
        // Also show machine-specific instructions
        String instructions = SlimefunDetector.getMachineInstructions(machine);
        if (client.player != null && !instructions.isEmpty()) {
            client.player.sendMessage(Text.literal("§e➤ " + instructions), false);
        }
        
        // Show safety warnings if needed
        if (SlimefunDetector.needsSpecialHandling(machine)) {
            String warning = SlimefunDetector.getSafetyWarning(machine);
            if (client.player != null && !warning.isEmpty()) {
                client.player.sendMessage(Text.literal(warning), true);
                client.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_ANVIL_LAND, 0.7F, 1.0F);
            }
        }
    }
}