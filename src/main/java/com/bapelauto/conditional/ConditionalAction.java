// ============================================
// FILE: ConditionalAction.java
// Path: src/main/java/com/bapelauto/conditional/ConditionalAction.java
// ============================================
package com.bapelauto.conditional;

import com.bapelauto.util.ChatUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Conditional action system - execute actions based on conditions
 */
public class ConditionalAction {
    
    public enum ConditionType {
        HEALTH_BELOW("Health Below", "Player health below threshold"),
        HEALTH_ABOVE("Health Above", "Player health above threshold"),
        HUNGER_BELOW("Hunger Below", "Player hunger below threshold"),
        ITEM_COUNT_BELOW("Item Count Below", "Specific item count below threshold"),
        ITEM_COUNT_ABOVE("Item Count Above", "Specific item count above threshold"),
        TIME_PASSED("Time Passed", "Time elapsed since last action"),
        INVENTORY_FULL("Inventory Full", "Player inventory is full"),
        INVENTORY_EMPTY("Inventory Empty", "Player inventory is empty"),
        IN_COMBAT("In Combat", "Player is in combat"),
        NOT_IN_COMBAT("Not In Combat", "Player is not in combat"),
        CUSTOM("Custom", "Custom condition");
        
        private final String displayName;
        private final String description;
        
        ConditionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public enum ActionType {
        SEND_COMMAND("Send Command", "Execute a command"),
        STOP_BOT("Stop Bot", "Disable all automation"),
        PLAY_SOUND("Play Sound", "Play alert sound"),
        SEND_MESSAGE("Send Message", "Display message"),
        SWITCH_PROFILE("Switch Profile", "Load different profile"),
        ENABLE_FEATURE("Enable Feature", "Turn on specific feature"),
        DISABLE_FEATURE("Disable Feature", "Turn off specific feature");
        
        private final String displayName;
        private final String description;
        
        ActionType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    private final ConditionType conditionType;
    private final ActionType actionType;
    private final double threshold;
    private final String actionData;
    private final String description;
    private long lastTriggeredTime = 0;
    private long cooldown = 5000; // 5 second cooldown between triggers
    
    public ConditionalAction(ConditionType condition, ActionType action, double threshold, String actionData, String description) {
        this.conditionType = condition;
        this.actionType = action;
        this.threshold = threshold;
        this.actionData = actionData;
        this.description = description;
    }
    
    /**
     * Check if condition is met
     */
    public boolean checkCondition(Minecraft client) {
        if (client.player == null) return false;
        
        Player player = client.player;
        long currentTime = System.currentTimeMillis();
        
        // Cooldown check
        if (currentTime - lastTriggeredTime < cooldown) {
            return false;
        }
        
        boolean conditionMet = false;
        
        switch (conditionType) {
            case HEALTH_BELOW:
                conditionMet = player.getHealth() < threshold;
                break;
                
            case HEALTH_ABOVE:
                conditionMet = player.getHealth() > threshold;
                break;
                
            case HUNGER_BELOW:
                conditionMet = player.getFoodData().getFoodLevel() < threshold;
                break;
                
            case INVENTORY_FULL:
                conditionMet = isInventoryFull(player);
                break;
                
            case INVENTORY_EMPTY:
                conditionMet = isInventoryEmpty(player);
                break;
                
            case TIME_PASSED:
                conditionMet = (currentTime - lastTriggeredTime) > (threshold * 1000);
                break;
                
            case IN_COMBAT:
                conditionMet = player.getLastHurtByMob() != null;
                break;
                
            case NOT_IN_COMBAT:
                conditionMet = player.getLastHurtByMob() == null;
                break;
                
            default:
                conditionMet = false;
        }
        
        return conditionMet;
    }
    
    /**
     * Execute the action
     */
    public void executeAction(Minecraft client) {
        if (client.player == null) return;
        
        lastTriggeredTime = System.currentTimeMillis();
        
        switch (actionType) {
            case SEND_COMMAND:
                if (actionData != null && !actionData.isEmpty()) {
                    if (actionData.startsWith("/")) {
                        client.player.connection.sendCommand(actionData.substring(1));
                    } else {
                        client.player.connection.sendChat(actionData);
                    }
                }
                break;
                
            case SEND_MESSAGE:
                if (actionData != null) {
                    ChatUtil.displayClientMessage(client, net.minecraft.network.chat.Component.literal("§e[Auto] " + actionData), false);
                }
                break;
                
            case PLAY_SOUND:
                client.player.playSound(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 1.0F);
                break;
                
            case STOP_BOT:
                // This would need to call AutoBotMod to disable
                ChatUtil.displayClientMessage(client, net.minecraft.network.chat.Component.literal("§c[Auto] Bot stopped by condition: " + description), true);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Tick - check and execute if condition met
     */
    public void tick(Minecraft client) {
        if (checkCondition(client)) {
            executeAction(client);
        }
    }
    
    private boolean isInventoryFull(Player player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isInventoryEmpty(Player player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }
    
    public String getDescription() {
        return description != null ? description : 
            conditionType.getDisplayName() + " → " + actionType.getDisplayName();
    }
    
    @Override
    public String toString() {
        return String.format("[%s @ %.1f] → [%s: %s]", 
            conditionType.getDisplayName(), threshold, 
            actionType.getDisplayName(), actionData);
    }
}

/**
 * Manager for conditional actions
 */
class ConditionalActionManager {
    private final List<ConditionalAction> actions = new ArrayList<>();
    
    public void addAction(ConditionalAction action) {
        actions.add(action);
    }
    
    public void removeAction(ConditionalAction action) {
        actions.remove(action);
    }
    
    public void clearAll() {
        actions.clear();
    }
    
    public void tick(Minecraft client) {
        for (ConditionalAction action : actions) {
            action.tick(client);
        }
    }
    
    public List<ConditionalAction> getActions() {
        return new ArrayList<>(actions);
    }
    
    public int getActionCount() {
        return actions.size();
    }
}