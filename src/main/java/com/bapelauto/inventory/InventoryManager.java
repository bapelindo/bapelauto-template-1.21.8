// ============================================
// FILE: InventoryManager.java
// Path: src/main/java/com/bapelauto/inventory/InventoryManager.java
//
// Ported to Minecraft 26.1.2 / Fabric (official Mojang mappings).
//   - client.gameMode -> client.gameMode
//   - screen.getMenu() -> screen.getMenu()
//   - handler.containerId -> handler.containerId
//   - slot.hasItem()/getStack() -> slot.hasItem()/getItem()
// ============================================
package com.bapelauto.inventory;

import com.bapelauto.ShardedConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
// TODO(UNRESOLVED): ClickType was removed in this MC version.
// AbstractContainerMenu.clicked() now takes a "ContainerInput" object
// (confirmed real: net.minecraft.world.inventory.ContainerInput) instead of
// (mouseButton, ClickType). MultiPlayerGameMode.handleInventoryMouseClick(...)
// almost certainly changed to match. Please open ContainerInput in your IDE
// (Ctrl+Click on MultiPlayerGameMode.handleInventoryMouseClick) to see its
// real parameter list and any static factory methods (e.g. something like
// ContainerInput.click(...)), then replace every ClickType.PICKUP /
// ClickType.QUICK_MOVE usage below with the equivalent ContainerInput value.
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.sounds.SoundEvents;

public class InventoryManager {
    private boolean autoStealEnabled = false;
    private boolean autoStoreEnabled = false;

    private long inventoryDelay = 150;
    private long lastActionTime = 0;
    private int nextStealSlotId = 0;
    private int totalItemsMoved = 0;

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
                    if (slot.hasItem() && slot.mayPickup(client.player)) {
                        client.gameMode.handleInventoryMouseClick(handler.containerId, i, 0, ClickType.QUICK_MOVE, client.player);
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
                    if (slot.hasItem()) {
                        client.gameMode.handleInventoryMouseClick(handler.containerId, i, 0, ClickType.QUICK_MOVE, client.player);
                        totalItemsMoved++;
                        lastActionTime = currentTime;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[InventoryManager] Error: " + e.getMessage());
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
                    client.gameMode.handleInventoryMouseClick(handler.containerId, i, 0, ClickType.QUICK_MOVE, client.player);
                    count++;
                }
            }

            if (count > 0) {
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                    client.gameMode.handleInventoryMouseClick(handler.containerId, i, 0, ClickType.QUICK_MOVE, client.player);
                    count++;
                }
            }

            if (count > 0) {
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disableAll() {
        autoStealEnabled = false;
        autoStoreEnabled = false;
        nextStealSlotId = 0;
    }

    public void loadFromConfig(ShardedConfigManager config) {
        autoStealEnabled = config.getBoolean("autoStealEnabled", false);
        autoStoreEnabled = config.getBoolean("autoStoreEnabled", false);
        inventoryDelay = config.getLong("inventoryDelay", 150);
    }

    public void saveToConfig(ShardedConfigManager config) {
        config.set("autoStealEnabled", autoStealEnabled);
        config.set("autoStoreEnabled", autoStoreEnabled);
        config.set("inventoryDelay", inventoryDelay);
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