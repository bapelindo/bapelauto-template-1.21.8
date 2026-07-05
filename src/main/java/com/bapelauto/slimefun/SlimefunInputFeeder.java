// ============================================
// FILE: SlimefunInputFeeder.java
// Path: src/main/java/com/bapelauto/slimefun/SlimefunInputFeeder.java
//
// Ported to Minecraft 26.1.2 / Fabric (official Mojang mappings).
//   - net.minecraft.item.Item -> net.minecraft.world.item.Item (correct
//     vanilla package; the old import path was already wrong even on
//     1.21.x - kept only if actually used, otherwise removed as unused)
//   - client.gameMode -> client.gameMode
//   - screen.getMenu() -> screen.getMenu()
//   - handler.containerId -> handler.containerId
//   - client.player.containerMenu -> client.player.containerMenu
//   - .getCarried() -> .getCarried()
//   - player.sendMessage(...) -> ChatUtil.displayClientMessage(...)
//     (LocalPlayer no longer has any send/display-message method at all;
//     see util/ChatUtil.java for the real replacement and its caveats)
//   - ClickType was renamed to ContainerInput, and
//     handleInventoryMouseClick(...) was renamed to handleContainerInput(...)
//     (confirmed via real Minecraft source); see click/ClickExecutor.java
//     for details.
// ============================================
package com.bapelauto.slimefun;

import com.bapelauto.util.Log;

import com.bapelauto.util.ChatUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.network.chat.Component;

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
    public void tick(Minecraft client, SlimefunDetector.SlimefunMachine machine) {
        if (!enabled) return;
        if (client.screen == null || !(client.screen instanceof AbstractContainerScreen)) return;
        if (client.gameMode == null || client.player == null) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFeedTime < feedDelay) return;

        currentMachine = machine;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) client.screen;
        AbstractContainerMenu handler = screen.getMenu();

        // Check if input slots need items
        int[] inputSlots = INPUT_SLOTS.get(machine);
        if (inputSlots == null) return;

        // Find empty input slots
        for (int inputSlot : inputSlots) {
            if (inputSlot >= handler.slots.size()) continue;

            Slot slot = handler.getSlot(inputSlot);
            if (!slot.hasItem() || slot.getItem().getCount() < slot.getItem().getMaxStackSize() - 10) {
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
    private boolean feedItemToSlot(Minecraft client, AbstractContainerMenu handler,
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
            if (!playerSlot.hasItem()) continue;

            ItemStack stack = playerSlot.getItem();

            // Check if this item is suitable for this machine
            if (isSuitableItem(stack, machine, requiredItems)) {
                // Move item to input slot
                try {
                    // Pick up item from player inventory
                    client.gameMode.handleContainerInput(
                        handler.containerId, i, 0, ContainerInput.PICKUP, client.player
                    );

                    // Place in machine input slot
                    client.gameMode.handleContainerInput(
                        handler.containerId, targetSlot, 0, ContainerInput.PICKUP, client.player
                    );

                    // If still holding items, put them back
                    if (!client.player.containerMenu.getCarried().isEmpty()) {
                        client.gameMode.handleContainerInput(
                            handler.containerId, i, 0, ContainerInput.PICKUP, client.player
                        );
                    }

                    totalItemsFed++;

                    if (client.player != null) {
                        ChatUtil.displayClientMessage(client, 
                            Component.literal("§a[Auto-Input] Fed " + stack.getItem().getName(stack).getString() +
                                       " to " + machine.getDisplayName()),
                            true
                        );
                    }

                    return true;

                } catch (Exception e) {
                    Log.error("[InputFeeder] Error feeding item", e);
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

        String itemName = stack.getItem().getName(stack).getString().toLowerCase();
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
    public void configureForMachine(Minecraft client, SlimefunDetector.SlimefunMachine machine) {
        currentMachine = machine;

        if (!INPUT_SLOTS.containsKey(machine)) {
            if (client.player != null) {
                ChatUtil.displayClientMessage(client, 
                    Component.literal("§c[Auto-Input] Machine type not supported: " + machine.getDisplayName()),
                    false
                );
            }
            return;
        }

        if (client.player != null) {
            ChatUtil.displayClientMessage(client, 
                Component.literal("§a[Auto-Input] Configured for " + machine.getDisplayName()),
                true
            );

            List<String> items = REQUIRED_ITEMS.get(machine);
            if (items != null && !items.isEmpty()) {
                ChatUtil.displayClientMessage(client, 
                    Component.literal("§7Required items: §f" + String.join(", ", items)),
                    false
                );
            }
        }
    }

    /**
     * Smart detection - automatically configure based on open GUI
     */
    public void autoDetectAndConfigure(Minecraft client) {
        if (client.screen == null) return;

        SlimefunDetector.SlimefunMachine detected = SlimefunDetector.detectMachineType(client.screen);

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