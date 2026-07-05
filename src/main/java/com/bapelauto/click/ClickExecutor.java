// ============================================
// FILE: ClickExecutor.java
// Path: src/main/java/com/bapelauto/click/ClickExecutor.java
//
// CONFIRMED (real AbstractContainerMenu source pasted by the user): ClickType
// was renamed to ContainerInput, staying in net.minecraft.world.inventory
// (same package as AbstractContainerMenu, which is why that class uses it
// unqualified with no import). Same constant names (PICKUP, QUICK_MOVE,
// SWAP, CLONE, THROW, PICKUP_ALL, QUICK_CRAFT).
//
// CONFIRMED (real source pasted by the user): screen.mouseClicked/
// mouseReleased(double, double, int) were replaced by
// mouseClicked(MouseButtonEvent, boolean doubleClick) and
// mouseReleased(MouseButtonEvent). MouseButtonEvent is a record
// (double x, double y, MouseButtonInfo buttonInfo) and MouseButtonInfo is a
// record (int button, int modifiers) - both in net.minecraft.client.input.
// ============================================
package com.bapelauto.click;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
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
                    ContainerInput.PICKUP, 
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
            MouseButtonEvent event = new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0));
            client.screen.mouseClicked(event, false);
            client.screen.mouseReleased(event);
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