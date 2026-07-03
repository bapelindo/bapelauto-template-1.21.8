// ============================================
// FILE: WorldInteractionManager.java
// Path: src/main/java/com/bapelauto/world/WorldInteractionManager.java
// ============================================
package com.bapelauto.world;

import com.bapelauto.ShardedConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class WorldInteractionManager {
    private boolean leftClickEnabled = false;
    private boolean rightClickEnabled = false;
    
    private long leftClickDelay = 200;
    private long rightClickDelay = 200;
    
    private long lastLeftClickTime = 0;
    private long lastRightClickTime = 0;
    
    private int totalClicks = 0;
    
    public void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        
        long currentTime = System.currentTimeMillis();
        
        if (leftClickEnabled && (currentTime - lastLeftClickTime) >= leftClickDelay) {
            performLeftClick(client);
            lastLeftClickTime = currentTime;
        }
        
        if (rightClickEnabled && (currentTime - lastRightClickTime) >= rightClickDelay) {
            performRightClick(client);
            lastRightClickTime = currentTime;
        }
    }
    
    private void performLeftClick(MinecraftClient client) {
        if (client.interactionManager == null || client.player == null) return;
        
        try {
            client.player.swingHand(Hand.MAIN_HAND);
            if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                client.interactionManager.attackEntity(client.player, ((EntityHitResult)client.crosshairTarget).getEntity());
            } else if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                BlockHitResult bh = (BlockHitResult) client.crosshairTarget;
                client.interactionManager.attackBlock(bh.getBlockPos(), bh.getSide());
            }
            totalClicks++;
        } catch (Exception e) {
            System.err.println("[WorldManager] Left click error: " + e.getMessage());
        }
    }
    
    private void performRightClick(MinecraftClient client) {
        if (client.interactionManager == null || client.player == null) return;
        
        try {
            boolean actionTaken = false;
            if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                if (client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, (BlockHitResult)client.crosshairTarget).isAccepted()) {
                    client.player.swingHand(Hand.MAIN_HAND);
                    actionTaken = true;
                }
            }
            if (!actionTaken) {
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            }
            totalClicks++;
        } catch (Exception e) {
            System.err.println("[WorldManager] Right click error: " + e.getMessage());
        }
    }
    
    public void disableAll() {
        leftClickEnabled = false;
        rightClickEnabled = false;
    }
    
    public void loadFromConfig(ShardedConfigManager config) {
        leftClickEnabled = config.getBoolean("leftClickEnabled", false);
        rightClickEnabled = config.getBoolean("rightClickEnabled", false);
        leftClickDelay = config.getLong("leftClickDelay", 200);
        rightClickDelay = config.getLong("rightClickDelay", 200);
    }
    
    public void saveToConfig(ShardedConfigManager config) {
        config.set("leftClickEnabled", leftClickEnabled);
        config.set("rightClickEnabled", rightClickEnabled);
        config.set("leftClickDelay", leftClickDelay);
        config.set("rightClickDelay", rightClickDelay);
    }
    
    // Getters and setters
    public boolean isLeftClickEnabled() { return leftClickEnabled; }
    public void setLeftClickEnabled(boolean enabled) { leftClickEnabled = enabled; }
    public boolean isRightClickEnabled() { return rightClickEnabled; }
    public void setRightClickEnabled(boolean enabled) { rightClickEnabled = enabled; }
    public long getLeftClickDelay() { return leftClickDelay; }
    public void setLeftClickDelay(long delay) { leftClickDelay = Math.max(50, delay); }
    public long getRightClickDelay() { return rightClickDelay; }
    public void setRightClickDelay(long delay) { rightClickDelay = Math.max(50, delay); }
    public int getTotalClicks() { return totalClicks; }
    public void resetClicks() { totalClicks = 0; }
}