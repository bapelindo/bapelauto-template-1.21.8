// ============================================
// FILE: SlimefunInputFeeder.java
// Path: src/main/java/com/bapelauto/slimefun/SlimefunInputFeeder.java
// ============================================
package com.bapelauto.slimefun;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.*;

/**
 * Intelligent Auto-Input System for Slimefun Machines
 * Automatically feeds required items from player inventory to machine input slots
 */
public class SlimefunInputFeeder {
    
    // Machine input slot mappings
    private static final Map<SlimefunDetector.SlimefunMachine, int[]> INPUT_SLOTS = new HashMap<>();
    
    // Item requirements for specific machines
    private static final Map<SlimefunDetector.SlimefunMachine, List<String>> REQUIRED_ITEMS = new HashMap<>();
    
    static {
        // Define input slots for each machine type
        INPUT_SLOTS.put(SlimefunDetector.SlimefunMachine.ELECTRIC_FURNACE, new int[]{10}); // Input slot
        INPUT_SLOTS.put(SlimefunDetector.SlimefunMachine.ELECTRIC_ORE_GRINDER, new int[]{10});
        INPUT_SLOTS.put(SlimefunDetector.SlimefunMachine.ELECTRIC_SMELTERY, new int[]{10, 11, 12});
        INPUT_SLOTS.put(SlimefunDetector.SlimefunMachine.ELECTRIC_GOLD_PAN, new int[]{10});
        INPUT_SLOTS.put(SlimefunDetector.SlimefunMachine.CARBON_PRESS, new int[]{10});
        INPUT_SLOTS.put(SlimefunDetector.SlimefunMachine.ELECTRIC_PRESS, new int[]{10});
        INPUT_SLOTS.put(SlimefunDetector.SlimefunMachine.COAL_GENERATOR, new int[]{10}); // Fuel slot
        INPUT_SLOTS.put(SlimefunDetector.SlimefunMachine.BIO_REACTOR, new int[]{10, 11, 12, 13, 14, 15, 16});
        
        // Define required items (for filtering)
        REQUIRED_ITEMS.put(SlimefunDetector.SlimefunMachine.CARBON_PRESS, 
            Arrays.asList("carbon", "compressed_carbon"));
        REQUIRED_ITEMS.put(SlimefunDetector.SlimefunMachine.COAL_GENERATOR, 
            Arrays.asList("coal", "charcoal", "coal_block"));
        REQUIRED_ITEMS.put(SlimefunDetector.SlimefunMachine.BIO_REACTOR, 
            Arrays.asList("wheat", "carrot", "potato", "beetroot", "melon", "pumpkin"));
    }
    
    private boolean enabled = false;
    private long lastFeedTime = 0;
    private long feedDelay = 500; // 500ms between feeds
    private int totalItemsFed = 0;
    
    private SlimefunDetector.SlimefunMachine currentMachine = SlimefunDetector.SlimefunMachine.UNKNOWN;
    
    /**
     * Main tick - check and feed items to machine
     */
    public void tick(MinecraftClient client, SlimefunDetector.SlimefunMachine machine) {
        if (!enabled) return;
        if (client.currentScreen == null || !(client.currentScreen instanceof HandledScreen)) return;
        if (client.interactionManager == null || client.player == null) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFeedTime < feedDelay) return;
        
        currentMachine = machine;
        
        HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;
        ScreenHandler handler = screen.getScreenHandler();
        
        // Check if input slots need items
        int[] inputSlots = INPUT_SLOTS.get(machine);
        if (inputSlots == null) return;
        
        // Find empty input slots
        for (int inputSlot : inputSlots) {
            if (inputSlot >= handler.slots.size()) continue;
            
            Slot slot = handler.getSlot(inputSlot);
            if (!slot.hasStack() || slot.getStack().getCount() < slot.getStack().getMaxCount() - 10) {
                // Slot is empty or low, try to feed it
                if (feedItemToSlot(client, handler, inputSlot, machine)) {
                    lastFeedTime = currentTime;
                    return; // Feed one item at a time
                }
            }
        }
    }
    
    /**
     * Feed an appropriate item from player inventory to machine input slot
     */
    private boolean feedItemToSlot(MinecraftClient client, ScreenHandler handler, 
                                    int targetSlot, SlimefunDetector.SlimefunMachine machine) {
        
        // Get player inventory range
        int totalSlots = handler.slots.size();
        int playerStart = Math.max(0, totalSlots - 36);
        
        // Get required items for this machine
        List<String> requiredItems = REQUIRED_ITEMS.get(machine);
        
        // Search player inventory for suitable items
        for (int i = playerStart; i < totalSlots; i++) {
            if (i >= handler.slots.size()) break;
            
            Slot playerSlot = handler.getSlot(i);
            if (!playerSlot.hasStack()) continue;
            
            ItemStack stack = playerSlot.getStack();
            
            // Check if this item is suitable for this machine
            if (isSuitableItem(stack, machine, requiredItems)) {
                // Move item to input slot
                try {
                    // Pick up item from player inventory
                    client.interactionManager.clickSlot(
                        handler.syncId, i, 0, SlotActionType.PICKUP, client.player
                    );
                    
                    // Place in machine input slot
                    client.interactionManager.clickSlot(
                        handler.syncId, targetSlot, 0, SlotActionType.PICKUP, client.player
                    );
                    
                    // If still holding items, put them back
                    if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                        client.interactionManager.clickSlot(
                            handler.syncId, i, 0, SlotActionType.PICKUP, client.player
                        );
                    }
                    
                    totalItemsFed++;
                    
                    if (client.player != null) {
                        client.player.sendMessage(
                            Text.literal("§a[Auto-Input] Fed " + stack.getItem().getName().getString() + 
                                       " to " + machine.getDisplayName()),
                            true
                        );
                    }
                    
                    return true;
                    
                } catch (Exception e) {
                    System.err.println("[InputFeeder] Error feeding item: " + e.getMessage());
                }
            }
        }
        
        return false;
    }
    
    /**
     * Check if item is suitable for this machine
     */
    private boolean isSuitableItem(ItemStack stack, SlimefunDetector.SlimefunMachine machine, 
                                    List<String> requiredItems) {
        if (stack.isEmpty()) return false;
        
        String itemName = stack.getItem().getName().getString().toLowerCase();
        String itemId = stack.getItem().toString().toLowerCase();
        
        // If no specific requirements, accept any item
        if (requiredItems == null || requiredItems.isEmpty()) {
            return true;
        }
        
        // Check against required items list
        for (String required : requiredItems) {
            if (itemName.contains(required) || itemId.contains(required)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Configure for specific machine type
     */
    public void configureForMachine(MinecraftClient client, SlimefunDetector.SlimefunMachine machine) {
        currentMachine = machine;
        
        if (!INPUT_SLOTS.containsKey(machine)) {
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§c[Auto-Input] Machine type not supported: " + machine.getDisplayName()),
                    false
                );
            }
            return;
        }
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§a[Auto-Input] Configured for " + machine.getDisplayName()),
                true
            );
            
            List<String> items = REQUIRED_ITEMS.get(machine);
            if (items != null && !items.isEmpty()) {
                client.player.sendMessage(
                    Text.literal("§7Required items: §f" + String.join(", ", items)),
                    false
                );
            }
        }
    }
    
    /**
     * Smart detection - automatically configure based on open GUI
     */
    public void autoDetectAndConfigure(MinecraftClient client) {
        if (client.currentScreen == null) return;
        
        SlimefunDetector.SlimefunMachine detected = SlimefunDetector.detectMachineType(client.currentScreen);
        
        if (detected != SlimefunDetector.SlimefunMachine.UNKNOWN) {
            configureForMachine(client, detected);
        }
    }
    
    /**
     * Add custom item requirement
     */
    public void addRequiredItem(SlimefunDetector.SlimefunMachine machine, String itemName) {
        REQUIRED_ITEMS.computeIfAbsent(machine, k -> new ArrayList<>()).add(itemName.toLowerCase());
    }
    
    /**
     * Add custom input slot
     */
    public void addInputSlot(SlimefunDetector.SlimefunMachine machine, int slotId) {
        int[] existing = INPUT_SLOTS.get(machine);
        if (existing == null) {
            INPUT_SLOTS.put(machine, new int[]{slotId});
        } else {
            int[] newSlots = Arrays.copyOf(existing, existing.length + 1);
            newSlots[newSlots.length - 1] = slotId;
            INPUT_SLOTS.put(machine, newSlots);
        }
    }
    
    /**
     * Get status info
     */
    public String getStatusInfo() {
        if (!enabled) return "§7Auto-Input: §cOFF";
        
        return String.format("§7Auto-Input: §aON §7| Fed: §f%d §7| Machine: §f%s",
            totalItemsFed,
            currentMachine != SlimefunDetector.SlimefunMachine.UNKNOWN ? 
                currentMachine.getDisplayName() : "None"
        );
    }
    
    public void reset() {
        totalItemsFed = 0;
        currentMachine = SlimefunDetector.SlimefunMachine.UNKNOWN;
    }
    
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public long getFeedDelay() { return feedDelay; }
    public void setFeedDelay(long delay) { this.feedDelay = Math.max(200, delay); }
    
    public int getTotalItemsFed() { return totalItemsFed; }
    
    public SlimefunDetector.SlimefunMachine getCurrentMachine() { return currentMachine; }
}