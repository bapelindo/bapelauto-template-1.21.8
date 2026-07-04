// ============================================
// FILE: ClickExecutor.java
// Path: src/main/java/com/bapelauto/click/ClickExecutor.java
// ============================================
package com.bapelauto.click;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
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

public class ClickExecutor {
    private int totalClicks = 0;
    
    public boolean executeClick(Minecraft client, ClickTarget target) {
        try {
            if (target.type == ClickTarget.Type.SLOT && client.screen instanceof AbstractContainerScreen) {
                return clickSlot(client, (AbstractContainerScreen<?>) client.screen, target.slotId);
            } else if (target.type == ClickTarget.Type.POINT && client.screen != null) {
                return clickPoint(client, target.x, target.y);
            }
        } catch (Exception e) {
            System.err.println("[ClickExecutor] Error: " + e.getMessage());
        }
        return false;
    }
    
    private boolean clickSlot(Minecraft client, AbstractContainerScreen<?> screen, int slotId) {
        if (client.gameMode == null || client.player == null) return false;
        
        try {
            if (slotId >= 0 && slotId < screen.getMenu().slots.size()) {
                client.gameMode.handleInventoryMouseClick(
                    screen.getMenu().containerId,
                    slotId,
                    0, 
                    ClickType.PICKUP, 
                    client.player
                );
                totalClicks++;
                return true;
            }
        } catch (Exception e) {
            System.err.println("[ClickExecutor] Slot click error: " + e.getMessage());
        }
        return false;
    }
    
    private boolean clickPoint(Minecraft client, double x, double y) {
        if (client.screen == null) return false;
        
        try {
            client.screen.mouseClicked(x, y, 0);
            client.screen.mouseReleased(x, y, 0);
            totalClicks++;
            return true;
        } catch (Exception e) {
            System.err.println("[ClickExecutor] Point click error: " + e.getMessage());
        }
        return false;
    }
    
    public int getTotalClicks() {
        return totalClicks;
    }
    
    public void resetClicks() {
        totalClicks = 0;
    }
}