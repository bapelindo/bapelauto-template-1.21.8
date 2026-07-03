// ============================================
// FILE: SlimefunDetector.java
// Path: src/main/java/com/bapelauto/slimefun/SlimefunDetector.java
// ============================================
package com.bapelauto.slimefun;

import com.bapelauto.click.ClickTarget;
import com.bapelauto.click.TimingPattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Advanced Slimefun machine detection and automation
 * Supports all major Slimefun machines and mechanisms
 */
public class SlimefunDetector {
    
    public enum SlimefunMachine {
        // Basic Machines
        ENHANCED_CRAFTING_TABLE("Enhanced Crafting Table", new int[]{24}, "Auto-craft Slimefun recipes", 150, TimingPattern.FIXED),
        ARMOR_FORGE("Armor Forge", new int[]{24}, "Auto-craft armor", 150, TimingPattern.FIXED),
        GRIND_STONE("Grind Stone", new int[]{24}, "Auto-grind items", 120, TimingPattern.FIXED),
        ORE_CRUSHER("Ore Crusher", new int[]{24}, "Auto-crush ores", 120, TimingPattern.FIXED),
        COMPRESSOR("Compressor", new int[]{24}, "Auto-compress items", 150, TimingPattern.FIXED),
        PRESSURE_CHAMBER("Pressure Chamber", new int[]{24}, "Auto-pressure items", 150, TimingPattern.FIXED),
        MAGIC_WORKBENCH("Magic Workbench", new int[]{24}, "Auto-craft magic items", 180, TimingPattern.FIXED),
        
        // Smeltery
        SMELTERY("Smeltery", new int[]{24}, "Auto-smelt ores", 150, TimingPattern.FIXED),
        ORE_WASHER("Ore Washer", new int[]{24}, "Auto-wash ores", 120, TimingPattern.FIXED),
        
        // Electric Machines
        ELECTRIC_FURNACE("Electric Furnace", new int[]{13}, "Auto-electric smelt", 120, TimingPattern.FIXED),
        ELECTRIC_ORE_GRINDER("Electric Ore Grinder", new int[]{13}, "Auto-electric grind", 120, TimingPattern.FIXED),
        ELECTRIC_GOLD_PAN("Electric Gold Pan", new int[]{13}, "Auto-pan gravel", 100, TimingPattern.BURST),
        ELECTRIC_DUST_WASHER("Electric Dust Washer", new int[]{13}, "Auto-wash dusts", 120, TimingPattern.FIXED),
        ELECTRIC_INGOT_FACTORY("Electric Ingot Factory", new int[]{13}, "Auto-make ingots", 150, TimingPattern.FIXED),
        ELECTRIC_CRUCIBLE("Electric Crucible", new int[]{13}, "Auto-crucible", 150, TimingPattern.FIXED),
        ELECTRIFIED_CRUCIBLE("Electrified Crucible", new int[]{13}, "Fast auto-crucible", 100, TimingPattern.BURST),
        CARBON_PRESS("Carbon Press", new int[]{13}, "Auto-press carbon", 150, TimingPattern.FIXED),
        ELECTRIC_SMELTERY("Electric Smeltery", new int[]{13}, "Auto-electric smelt ores", 120, TimingPattern.FIXED),
        ELECTRIC_PRESS("Electric Press", new int[]{13}, "Auto-press items", 120, TimingPattern.FIXED),
        
        // Food
        FOOD_FABRICATOR("Food Fabricator", new int[]{13}, "Auto-fabricate food", 150, TimingPattern.FIXED),
        FOOD_COMPOSTER("Food Composter", new int[]{13}, "Auto-compost", 120, TimingPattern.FIXED),
        FREEZER("Freezer", new int[]{13}, "Auto-freeze", 150, TimingPattern.FIXED),
        
        // Auto-Crafters
        AUTO_CRAFTER("Auto-Crafter", new int[]{24}, "Auto-craft any recipe", 150, TimingPattern.RANDOMIZED),
        ENHANCED_AUTO_CRAFTER("Enhanced Auto-Crafter", new int[]{24}, "Fast auto-craft", 100, TimingPattern.BURST),
        ARMOR_AUTO_CRAFTER("Armor Auto-Crafter", new int[]{24}, "Auto-craft armor", 150, TimingPattern.FIXED),
        
        // Reactors
        NUCLEAR_REACTOR("Nuclear Reactor", new int[]{13, 14, 15, 16}, "Monitor reactor", 500, TimingPattern.FIXED),
        NETHER_STAR_REACTOR("Nether Star Reactor", new int[]{13, 14, 15, 16}, "Monitor star reactor", 500, TimingPattern.FIXED),
        
        // Cargo
        CARGO_INPUT_NODE("Cargo Input", new int[]{}, "Auto-input cargo", 100, TimingPattern.FIXED),
        CARGO_OUTPUT_NODE("Cargo Output", new int[]{}, "Auto-output cargo", 100, TimingPattern.FIXED),
        
        // GPS/GEO
        GEO_MINER("GEO Miner", new int[]{13}, "Auto-collect geo-resources", 200, TimingPattern.FIXED),
        OIL_PUMP("Oil Pump", new int[]{13}, "Auto-pump oil", 200, TimingPattern.FIXED),
        
        // Generators
        BIO_REACTOR("Bio Reactor", new int[]{13, 14, 15, 16}, "Monitor bio-reactor", 300, TimingPattern.FIXED),
        COAL_GENERATOR("Coal Generator", new int[]{13}, "Auto-feed coal", 200, TimingPattern.FIXED),
        
        // Android
        ANDROID_INTERFACE("Android Interface", new int[]{}, "Android programming", 150, TimingPattern.FIXED),
        
        // Generic
        GENERIC_MACHINE("Generic Machine", new int[]{24}, "Generic Slimefun machine", 150, TimingPattern.RANDOMIZED),
        UNKNOWN("Unknown", new int[]{}, "Not a Slimefun machine", 100, TimingPattern.FIXED);
        
        private final String displayName;
        private final int[] outputSlots;
        private final String description;
        private final long recommendedDelay;
        private final TimingPattern recommendedPattern;
        
        SlimefunMachine(String displayName, int[] outputSlots, String description, 
                       long recommendedDelay, TimingPattern recommendedPattern) {
            this.displayName = displayName;
            this.outputSlots = outputSlots;
            this.description = description;
            this.recommendedDelay = recommendedDelay;
            this.recommendedPattern = recommendedPattern;
        }
        
        public String getDisplayName() { return displayName; }
        public int[] getOutputSlots() { return outputSlots; }
        public String getDescription() { return description; }
        public long getRecommendedDelay() { return recommendedDelay; }
        public TimingPattern getRecommendedPattern() { return recommendedPattern; }
    }
    
    /**
     * Detect if current GUI is a Slimefun machine
     */
    public static boolean isSlimefunGUI(Screen screen) {
        if (!(screen instanceof HandledScreen)) return false;
        
        String title = screen.getTitle().getString().toLowerCase();
        
        // Check for common Slimefun indicators
        return title.contains("slimefun") || 
               title.contains("electric") ||
               title.contains("enhanced") ||
               title.contains("auto") ||
               title.contains("reactor") ||
               title.contains("generator") ||
               title.contains("machine") ||
               title.contains("cargo") ||
               title.contains("android") ||
               detectBySlotPattern((HandledScreen<?>) screen);
    }
    
    /**
     * Detect Slimefun machine type from GUI
     */
    public static SlimefunMachine detectMachineType(Screen screen) {
        if (!(screen instanceof HandledScreen)) return SlimefunMachine.UNKNOWN;
        
        String title = screen.getTitle().getString().toLowerCase();
        
        // Enhanced Crafting Table variants
        if (title.contains("enhanced crafting")) return SlimefunMachine.ENHANCED_CRAFTING_TABLE;
        if (title.contains("armor forge")) return SlimefunMachine.ARMOR_FORGE;
        if (title.contains("grind stone") || title.contains("grindstone")) return SlimefunMachine.GRIND_STONE;
        if (title.contains("magic workbench")) return SlimefunMachine.MAGIC_WORKBENCH;
        
        // Crushers and Compressors
        if (title.contains("ore crusher")) return SlimefunMachine.ORE_CRUSHER;
        if (title.contains("compressor")) return SlimefunMachine.COMPRESSOR;
        if (title.contains("pressure chamber")) return SlimefunMachine.PRESSURE_CHAMBER;
        
        // Smeltery
        if (title.contains("smeltery") && !title.contains("electric")) return SlimefunMachine.SMELTERY;
        if (title.contains("ore washer")) return SlimefunMachine.ORE_WASHER;
        
        // Electric Machines
        if (title.contains("electric furnace")) return SlimefunMachine.ELECTRIC_FURNACE;
        if (title.contains("electric ore grinder")) return SlimefunMachine.ELECTRIC_ORE_GRINDER;
        if (title.contains("electric gold pan")) return SlimefunMachine.ELECTRIC_GOLD_PAN;
        if (title.contains("electric dust washer")) return SlimefunMachine.ELECTRIC_DUST_WASHER;
        if (title.contains("electric ingot")) return SlimefunMachine.ELECTRIC_INGOT_FACTORY;
        if (title.contains("electric crucible")) return SlimefunMachine.ELECTRIC_CRUCIBLE;
        if (title.contains("electrified crucible")) return SlimefunMachine.ELECTRIFIED_CRUCIBLE;
        if (title.contains("carbon press")) return SlimefunMachine.CARBON_PRESS;
        if (title.contains("electric smeltery")) return SlimefunMachine.ELECTRIC_SMELTERY;
        if (title.contains("electric press")) return SlimefunMachine.ELECTRIC_PRESS;
        
        // Food Machines
        if (title.contains("food fabricator")) return SlimefunMachine.FOOD_FABRICATOR;
        if (title.contains("food composter")) return SlimefunMachine.FOOD_COMPOSTER;
        if (title.contains("freezer")) return SlimefunMachine.FREEZER;
        
        // Auto-Crafters
        if (title.contains("enhanced auto")) return SlimefunMachine.ENHANCED_AUTO_CRAFTER;
        if (title.contains("armor auto")) return SlimefunMachine.ARMOR_AUTO_CRAFTER;
        if (title.contains("auto crafter") || title.contains("auto-crafter")) return SlimefunMachine.AUTO_CRAFTER;
        
        // Reactors
        if (title.contains("nuclear reactor")) return SlimefunMachine.NUCLEAR_REACTOR;
        if (title.contains("nether star reactor")) return SlimefunMachine.NETHER_STAR_REACTOR;
        
        // Cargo
        if (title.contains("cargo input")) return SlimefunMachine.CARGO_INPUT_NODE;
        if (title.contains("cargo output")) return SlimefunMachine.CARGO_OUTPUT_NODE;
        
        // GPS/GEO
        if (title.contains("geo miner") || title.contains("geo-miner")) return SlimefunMachine.GEO_MINER;
        if (title.contains("oil pump")) return SlimefunMachine.OIL_PUMP;
        
        // Generators
        if (title.contains("bio reactor")) return SlimefunMachine.BIO_REACTOR;
        if (title.contains("coal generator")) return SlimefunMachine.COAL_GENERATOR;
        
        // Android
        if (title.contains("android")) return SlimefunMachine.ANDROID_INTERFACE;
        
        // Generic Slimefun check
        if (isSlimefunGUI(screen)) return SlimefunMachine.GENERIC_MACHINE;
        
        return SlimefunMachine.UNKNOWN;
    }
    
    /**
     * Detect by slot pattern (Slimefun machines typically have specific layouts)
     */
    private static boolean detectBySlotPattern(HandledScreen<?> screen) {
        int totalSlots = screen.getScreenHandler().slots.size();
        
        // Slimefun machines typically have 54 slots (27 machine + 27 player)
        // or 45 slots (9 machine + 36 player)
        return totalSlots == 54 || totalSlots == 45 || 
               totalSlots == 63 || totalSlots == 72;
    }
    
    /**
     * Auto-detect and capture output slots for Slimefun machines
     */
    public static List<ClickTarget> autoDetectSlimefunTargets(MinecraftClient client, SlimefunMachine machine) {
        List<ClickTarget> targets = new ArrayList<>();
        
        if (!(client.currentScreen instanceof HandledScreen)) {
            return targets;
        }
        
        HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;
        
        // Use predefined output slots if available
        int[] outputSlots = machine.getOutputSlots();
        if (outputSlots.length > 0) {
            for (int slotId : outputSlots) {
                targets.add(new ClickTarget(slotId, machine.getRecommendedDelay()));
            }
        } else {
            // Fallback: detect slots with items (likely outputs)
            int totalSlots = screen.getScreenHandler().slots.size();
            int machineEnd = Math.max(0, totalSlots - 36); // Exclude player inventory
            
            for (int i = 0; i < machineEnd; i++) {
                Slot slot = screen.getScreenHandler().getSlot(i);
                if (slot.hasStack()) {
                    // Check if slot looks like output (right side or bottom-right area)
                    if (isLikelyOutputSlot(i, machineEnd)) {
                        targets.add(new ClickTarget(i, machine.getRecommendedDelay()));
                    }
                }
            }
        }
        
        if (client.player != null && !targets.isEmpty()) {
            client.player.sendMessage(
                Text.literal("§a[Slimefun] Detected " + machine.getDisplayName() + 
                           " - " + targets.size() + " output slots"), 
                true
            );
            client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 0.7F, 1.5F);
        }
        
        return targets;
    }
    
    /**
     * Check if slot position suggests it's an output slot
     */
    private static boolean isLikelyOutputSlot(int slotId, int machineSlotCount) {
        // Common Slimefun output slot patterns:
        // - Slot 13 (center-right in 3x3 grid)
        // - Slot 16 (bottom-right in 3x3 grid)
        // - Slot 24 (center of 5x5 grid)
        
        int[] commonOutputSlots = {13, 14, 15, 16, 24, 25};
        
        for (int commonSlot : commonOutputSlots) {
            if (slotId == commonSlot) return true;
        }
        
        // If it's in the rightmost column or bottom row of machine area
        if (machineSlotCount <= 27) { // 3x9 or smaller
            int col = slotId % 9;
            int row = slotId / 9;
            return col >= 6 || row >= 2; // Right side or bottom rows
        }
        
        return false;
    }
    
    /**
     * Auto-configure bot for detected Slimefun machine
     */
    public static void autoConfigureForSlimefun(MinecraftClient client, SlimefunMachine machine) {
        if (client.player == null) return;
        
        client.player.sendMessage(
            Text.literal("§e§l[Slimefun Auto-Config]"), false
        );
        client.player.sendMessage(
            Text.literal("§7Machine: §f" + machine.getDisplayName()), false
        );
        client.player.sendMessage(
            Text.literal("§7Type: §f" + machine.getDescription()), false
        );
        client.player.sendMessage(
            Text.literal("§7Pattern: §f" + machine.getRecommendedPattern().getDisplayName()), false
        );
        client.player.sendMessage(
            Text.literal("§7Delay: §f" + machine.getRecommendedDelay() + "ms"), false
        );
        client.player.sendMessage(
            Text.literal("§a✓ Press [=] to start automation"), false
        );
    }
    
    /**
     * Get specific instructions for machine type
     */
    public static String getMachineInstructions(SlimefunMachine machine) {
        switch (machine) {
            case ENHANCED_CRAFTING_TABLE:
            case ARMOR_FORGE:
            case MAGIC_WORKBENCH:
                return "Place recipe ingredients, bot will auto-collect crafted items";
                
            case ELECTRIC_FURNACE:
            case ELECTRIC_ORE_GRINDER:
            case ELECTRIC_SMELTERY:
                return "Ensure machine has power, bot will auto-collect processed items";
                
            case NUCLEAR_REACTOR:
            case NETHER_STAR_REACTOR:
                return "WARNING: Monitor reactor carefully! Bot will collect output safely";
                
            case GEO_MINER:
            case OIL_PUMP:
                return "Bot will automatically collect geo-resources as they're mined";
                
            case AUTO_CRAFTER:
            case ENHANCED_AUTO_CRAFTER:
                return "Set up recipe, bot will collect finished products continuously";
                
            case CARGO_INPUT_NODE:
                return "Bot can assist with cargo input automation";
                
            case CARGO_OUTPUT_NODE:
                return "Bot will collect items from cargo output";
                
            default:
                return "Bot will automatically collect output from this machine";
        }
    }
    
    /**
     * Check if machine needs special handling
     */
    public static boolean needsSpecialHandling(SlimefunMachine machine) {
        return machine == SlimefunMachine.NUCLEAR_REACTOR ||
               machine == SlimefunMachine.NETHER_STAR_REACTOR ||
               machine == SlimefunMachine.BIO_REACTOR;
    }
    
    /**
     * Get safety warnings for dangerous machines
     */
    public static String getSafetyWarning(SlimefunMachine machine) {
        switch (machine) {
            case NUCLEAR_REACTOR:
            case NETHER_STAR_REACTOR:
                return "§c§lWARNING: Reactor detected! Ensure coolant is sufficient!";
            case BIO_REACTOR:
                return "§e§lCAUTION: Monitor fuel levels to prevent shutdown";
            default:
                return "";
        }
    }
}