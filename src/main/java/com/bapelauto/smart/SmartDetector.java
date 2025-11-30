// ============================================
// FILE: SmartDetector.java
// Path: src/main/java/com/bapelauto/smart/SmartDetector.java
// ============================================
package com.bapelauto.smart;

import com.bapelauto.click.ClickTarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.List;

/**
 * Smart detection system that automatically identifies GUI patterns and suggests actions
 */
public class SmartDetector {
    
    public enum GuiType {
        CHEST("Chest/Container", "Steal items from container"),
        FURNACE("Furnace", "Auto-smelt items"),
        CRAFTING("Crafting Table", "Auto-craft items"),
        MERCHANT("Villager/Merchant", "Auto-trade"),
        ENCHANTING("Enchanting Table", "Auto-enchant"),
        ANVIL("Anvil", "Auto-repair/combine"),
        BREWING("Brewing Stand", "Auto-brew potions"),
        HOPPER("Hopper", "Transfer items"),
        UNKNOWN("Unknown", "No specific pattern detected");
        
        private final String displayName;
        private final String suggestion;
        
        GuiType(String displayName, String suggestion) {
            this.displayName = displayName;
            this.suggestion = suggestion;
        }
        
        public String getDisplayName() { return displayName; }
        public String getSuggestion() { return suggestion; }
    }
    
    /**
     * Detect GUI type from current screen
     */
    public static GuiType detectGuiType(Screen screen) {
        if (screen == null) return GuiType.UNKNOWN;
        
        if (screen instanceof GenericContainerScreen) {
            return GuiType.CHEST;
        } else if (screen instanceof FurnaceScreen) {
            return GuiType.FURNACE;
        } else if (screen instanceof CraftingScreen) {
            return GuiType.CRAFTING;
        } else if (screen instanceof MerchantScreen) {
            return GuiType.MERCHANT;
        } else if (screen instanceof EnchantmentScreen) {
            return GuiType.ENCHANTING;
        } else if (screen instanceof AnvilScreen) {
            return GuiType.ANVIL;
        } else if (screen instanceof BrewingStandScreen) {
            return GuiType.BREWING;
        } else if (screen instanceof HopperScreen) {
            return GuiType.HOPPER;
        }
        
        return GuiType.UNKNOWN;
    }
    
    /**
     * Auto-detect and suggest click targets based on GUI type
     */
    public static List<ClickTarget> autoDetectTargets(MinecraftClient client) {
        List<ClickTarget> targets = new ArrayList<>();
        
        if (!(client.currentScreen instanceof HandledScreen)) {
            return targets;
        }
        
        HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;
        GuiType guiType = detectGuiType(screen);
        
        switch (guiType) {
            case CHEST:
                targets = detectChestPattern(screen);
                break;
            case FURNACE:
                targets = detectFurnacePattern(screen);
                break;
            case CRAFTING:
                targets = detectCraftingPattern(screen);
                break;
            case MERCHANT:
                targets = detectMerchantPattern(screen);
                break;
            case ENCHANTING:
                targets = detectEnchantingPattern(screen);
                break;
            default:
                // Generic detection: all non-empty slots
                targets = detectGenericPattern(screen);
                break;
        }
        
        if (client.player != null && !targets.isEmpty()) {
            client.player.sendMessage(
                Text.literal("§a[Smart Detect] Found " + targets.size() + " targets in " + guiType.getDisplayName()),
                true
            );
            client.player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 1.5F);
        }
        
        return targets;
    }
    
    private static List<ClickTarget> detectChestPattern(HandledScreen<?> screen) {
        List<ClickTarget> targets = new ArrayList<>();
        
        // Detect all container slots (not player inventory)
        int totalSlots = screen.getScreenHandler().slots.size();
        int containerEnd = Math.max(0, totalSlots - 36);
        
        for (int i = 0; i < containerEnd; i++) {
            Slot slot = screen.getScreenHandler().getSlot(i);
            if (slot.hasStack()) {
                targets.add(new ClickTarget(slot.id, 100));
            }
        }
        
        return targets;
    }
    
    private static List<ClickTarget> detectFurnacePattern(HandledScreen<?> screen) {
        List<ClickTarget> targets = new ArrayList<>();
        
        // Furnace slots: 0=input, 1=fuel, 2=output
        // Auto-click output slot repeatedly
        if (screen.getScreenHandler().slots.size() > 2) {
            targets.add(new ClickTarget(2, 150)); // Output slot
        }
        
        return targets;
    }
    
    private static List<ClickTarget> detectCraftingPattern(HandledScreen<?> screen) {
        List<ClickTarget> targets = new ArrayList<>();
        
        // Crafting output slot (slot 0 in crafting table)
        if (screen.getScreenHandler().slots.size() > 0) {
            targets.add(new ClickTarget(0, 120)); // Result slot
        }
        
        return targets;
    }
    
    private static List<ClickTarget> detectMerchantPattern(HandledScreen<?> screen) {
        List<ClickTarget> targets = new ArrayList<>();
        
        // Merchant result slot (slot 2)
        if (screen.getScreenHandler().slots.size() > 2) {
            targets.add(new ClickTarget(2, 200)); // Trade result slot
        }
        
        return targets;
    }
    
    private static List<ClickTarget> detectEnchantingPattern(HandledScreen<?> screen) {
        List<ClickTarget> targets = new ArrayList<>();
        
        // Enchanting slots: 0=item, 1=lapis
        // Could add enchant button positions
        
        return targets;
    }
    
    private static List<ClickTarget> detectGenericPattern(HandledScreen<?> screen) {
        List<ClickTarget> targets = new ArrayList<>();
        
        // Detect all slots with items
        for (Slot slot : screen.getScreenHandler().slots) {
            if (slot.hasStack()) {
                targets.add(new ClickTarget(slot.id, 100));
            }
        }
        
        return targets;
    }
    
    /**
     * Suggest optimal timing pattern based on GUI type
     */
    public static String suggestTimingPattern(GuiType guiType) {
        switch (guiType) {
            case CHEST:
                return "BURST"; // Fast looting
            case FURNACE:
                return "FIXED"; // Steady collection
            case CRAFTING:
                return "RANDOMIZED"; // Human-like crafting
            case MERCHANT:
                return "FIXED"; // Consistent trading
            default:
                return "RANDOMIZED";
        }
    }
    
    /**
     * Get suggested delays for GUI type
     */
    public static long[] suggestDelays(GuiType guiType) {
        // Returns [baseDelay, minDelay, maxDelay]
        switch (guiType) {
            case CHEST:
                return new long[]{80, 50, 120}; // Fast
            case FURNACE:
                return new long[]{150, 100, 200}; // Medium
            case CRAFTING:
                return new long[]{120, 80, 180}; // Medium-fast
            case MERCHANT:
                return new long[]{200, 150, 300}; // Slow (server lag)
            default:
                return new long[]{100, 70, 150}; // Default
        }
    }
    
    /**
     * Auto-configure based on detected GUI
     */
    public static void autoConfigureForGui(MinecraftClient client, GuiType guiType) {
        if (client.player == null) return;
        
        String pattern = suggestTimingPattern(guiType);
        long[] delays = suggestDelays(guiType);
        
        client.player.sendMessage(
            Text.literal("§e[Smart Config] Recommended settings for " + guiType.getDisplayName()),
            false
        );
        client.player.sendMessage(
            Text.literal("§7Pattern: §f" + pattern + " §7| Delay: §f" + delays[0] + "ms"),
            false
        );
        client.player.sendMessage(
            Text.literal("§7Suggestion: §f" + guiType.getSuggestion()),
            false
        );
    }
}