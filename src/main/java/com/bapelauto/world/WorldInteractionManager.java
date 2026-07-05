// ============================================
// FILE: WorldInteractionManager.java
// Path: src/main/java/com/bapelauto/world/WorldInteractionManager.java
// ============================================
package com.bapelauto.world;

import com.bapelauto.util.Cooldown;
import com.bapelauto.util.Log;

import com.bapelauto.ShardedConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WorldInteractionManager {
    private boolean leftClickEnabled = false;
    private boolean rightClickEnabled = false;

    private long leftClickDelay = 200;
    private long rightClickDelay = 200;

    private final Cooldown leftClickCooldown = new Cooldown();
    private final Cooldown rightClickCooldown = new Cooldown();

    private int totalClicks = 0;

    public void tick(Minecraft client) {
        if (client.player == null || client.level == null) return;

        if (leftClickEnabled && leftClickCooldown.tryConsume(leftClickDelay)) {
            performLeftClick(client);
        }

        if (rightClickEnabled && rightClickCooldown.tryConsume(rightClickDelay)) {
            performRightClick(client);
        }
    }
    
    private void performLeftClick(Minecraft client) {
        if (client.gameMode == null || client.player == null) return;
        
        try {
            client.player.swing(InteractionHand.MAIN_HAND);
            if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.ENTITY) {
                client.gameMode.attack(client.player, ((EntityHitResult)client.hitResult).getEntity());
            } else if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult bh = (BlockHitResult) client.hitResult;
                client.gameMode.startDestroyBlock(bh.getBlockPos(), bh.getDirection());
            }
            totalClicks++;
        } catch (Exception e) {
            Log.error("[WorldManager] Left click error", e);
        }
    }
    
    private void performRightClick(Minecraft client) {
        if (client.gameMode == null || client.player == null) return;
        
        try {
            boolean actionTaken = false;
            if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.BLOCK) {
                if (client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, (BlockHitResult)client.hitResult).consumesAction()) {
                    client.player.swing(InteractionHand.MAIN_HAND);
                    actionTaken = true;
                }
            }
            if (!actionTaken) {
                client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
            }
            totalClicks++;
        } catch (Exception e) {
            Log.error("[WorldManager] Right click error", e);
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