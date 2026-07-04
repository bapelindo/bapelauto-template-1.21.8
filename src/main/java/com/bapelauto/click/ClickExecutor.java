// ============================================
// FILE: ClickExecutor.java
// Path: src/main/java/com/bapelauto/click/ClickExecutor.java
// ============================================
package com.bapelauto.click;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;

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