// ============================================
// FILE: InventoryManager.java
// Path: src/main/java/com/bapelauto/inventory/InventoryManager.java
//
// Ported to Minecraft 26.1.2 / Fabric (official Mojang mappings).
//   - client.gameMode -> client.gameMode
//   - screen.getMenu() -> screen.getMenu()
//   - handler.containerId -> handler.containerId
//   - slot.hasItem()/getStack() -> slot.hasItem()/getItem()
//   - ClickType was renamed to ContainerInput, and
//     handleInventoryMouseClick(...) was renamed to handleContainerInput(...)
//     (confirmed via real Minecraft source); see click/ClickExecutor.java
//     for details.
// ============================================
package com.bapelauto.inventory;

import com.bapelauto.util.Log;

import com.bapelauto.ShardedConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryManager {
    private boolean autoStealEnabled = false;
    private boolean autoStoreEnabled = false;

    private long inventoryDelay = 150;
    private long lastActionTime = 0;
    private int nextStealSlotId = 0;
    private int totalItemsMoved = 0;

    // Item ids (e.g. "minecraft:diamond") that auto-steal/auto-store will
    // never touch, so continuous automation can't quietly move away
    // something valuable the player forgot was in that container.
    private final Set<String> protectedItems = ConcurrentHashMap.newKeySet();

    public void tick(Minecraft client, AbstractContainerScreen<?> screen) {
        if (!autoStealEnabled && !autoStoreEnabled) return;
        if (client.gameMode == null || client.player == null) return;

        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastActionTime) < inventoryDelay) return;

        try {
            AbstractContainerMenu handler = screen.getMenu();
            int totalSlots = handler.slots.size();
            int containerEnd = Math.max(0, totalSlots - 36);

            if (autoStealEnabled) {
                if (nextStealSlotId >= containerEnd) {
                    nextStealSlotId = 0;
                }

                for (int i = nextStealSlotId; i < containerEnd; i++) {
                    if (i >= handler.slots.size()) break;

                    Slot slot = handler.getSlot(i);
                    if (slot.hasItem() && slot.mayPickup(client.player) && !isProtected(slot.getItem())) {
                        client.gameMode.handleContainerInput(handler.containerId, i, 0, ContainerInput.QUICK_MOVE, client.player);
                        totalItemsMoved++;
                        nextStealSlotId = i + 1;
                        lastActionTime = currentTime;
                        return;
                    }
                }
                nextStealSlotId = 0;
            }

            if (autoStoreEnabled) {
                int playerStart = Math.max(0, totalSlots - 36);
                for (int i = playerStart; i < totalSlots; i++) {
                    if (i >= handler.slots.size()) break;

                    Slot slot = handler.getSlot(i);
                    if (slot.hasItem() && !isProtected(slot.getItem())) {
                        client.gameMode.handleContainerInput(handler.containerId, i, 0, ContainerInput.QUICK_MOVE, client.player);
                        totalItemsMoved++;
                        lastActionTime = currentTime;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Log.error("[InventoryManager] Error", e);
            nextStealSlotId = 0;
        }
    }

    public static void performSingleSteal(Minecraft client) {
        if (client.player == null || !(client.screen instanceof AbstractContainerScreen<?> screen)) return;

        try {
            AbstractContainerMenu handler = screen.getMenu();
            int totalSlots = handler.slots.size();
            int containerEnd = Math.max(0, totalSlots - 36);
            int count = 0;

            for (int i = 0; i < containerEnd; i++) {
                Slot slot = handler.getSlot(i);
                if (slot.hasItem() && slot.mayPickup(client.player)) {
                    client.gameMode.handleContainerInput(handler.containerId, i, 0, ContainerInput.QUICK_MOVE, client.player);
                    count++;
                }
            }

            if (count > 0) {
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
            }
        } catch (Exception e) {
            Log.error("[InventoryManager] Single steal failed", e);
        }
    }

    public static void performSingleStore(Minecraft client) {
        if (client.player == null || !(client.screen instanceof AbstractContainerScreen<?> screen)) return;

        try {
            AbstractContainerMenu handler = screen.getMenu();
            int totalSlots = handler.slots.size();
            int playerStart = Math.max(0, totalSlots - 36);
            int count = 0;

            for (int i = playerStart; i < totalSlots; i++) {
                Slot slot = handler.getSlot(i);
                if (slot.hasItem()) {
                    client.gameMode.handleContainerInput(handler.containerId, i, 0, ContainerInput.QUICK_MOVE, client.player);
                    count++;
                }
            }

            if (count > 0) {
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
            }
        } catch (Exception e) {
            Log.error("[InventoryManager] Single store failed", e);
        }
    }

    public void disableAll() {
        autoStealEnabled = false;
        autoStoreEnabled = false;
        nextStealSlotId = 0;
    }

    private boolean isProtected(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return protectedItems.contains(stack.getItem().toString().toLowerCase());
    }

    /**
     * Add an item id (e.g. "minecraft:diamond") to the auto-steal/auto-store
     * protection list. Accepts the same string form as ItemStack.getItem()'s
     * toString(), matched case-insensitively.
     */
    public void addProtectedItem(String itemId) {
        if (itemId != null && !itemId.isBlank()) {
            protectedItems.add(itemId.trim().toLowerCase());
        }
    }

    public void removeProtectedItem(String itemId) {
        if (itemId != null) {
            protectedItems.remove(itemId.trim().toLowerCase());
        }
    }

    public Set<String> getProtectedItems() {
        return Collections.unmodifiableSet(protectedItems);
    }

    public void loadFromConfig(ShardedConfigManager config) {
        autoStealEnabled = config.getBoolean("autoStealEnabled", false);
        autoStoreEnabled = config.getBoolean("autoStoreEnabled", false);
        inventoryDelay = config.getLong("inventoryDelay", 150);

        protectedItems.clear();
        String stored = config.getString("protectedItems", "");
        if (!stored.isBlank()) {
            for (String itemId : stored.split(",")) {
                addProtectedItem(itemId);
            }
        }
    }

    public void saveToConfig(ShardedConfigManager config) {
        config.set("autoStealEnabled", autoStealEnabled);
        config.set("autoStoreEnabled", autoStoreEnabled);
        config.set("inventoryDelay", inventoryDelay);
        config.set("protectedItems", String.join(",", protectedItems));
    }

    // Getters and setters
    public boolean isAutoStealEnabled() { return autoStealEnabled; }
    public void setAutoStealEnabled(boolean enabled) { autoStealEnabled = enabled; }
    public boolean isAutoStoreEnabled() { return autoStoreEnabled; }
    public void setAutoStoreEnabled(boolean enabled) { autoStoreEnabled = enabled; }
    public long getInventoryDelay() { return inventoryDelay; }
    public void setInventoryDelay(long delay) { inventoryDelay = Math.max(50, delay); }
    public int getTotalItemsMoved() { return totalItemsMoved; }
    public void resetItemsMoved() { totalItemsMoved = 0; }
}