// ============================================
// FILE: ConditionalAction.java (FIXED FOR 26.1 - MOJMAP 1.22 PURIST)
// Path: src/main/java/com/bapelauto/conditional/ConditionalAction.java
// ============================================
package com.bapelauto.conditional;

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
        LOG_MESSAGE("Log Message"),
        SEND_CHAT("Send Chat/Command"),
        TOGGLE_BOT("Toggle Bot"),
        TRIGGER_HOTKEY("Trigger Hotkey"),
        EMERGENCY_STOP("Emergency Stop"),
        CUSTOM("Custom");
        
        private final String displayName;
        ActionType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    private final ConditionType conditionType;
    private final ActionType actionType;
    private final double threshold;
    private final String actionData;
    private String description;
    
    private long cooldown = 1000; // 1s default cooldown
    private long lastExecutionTime = 0;
    private Predicate<Minecraft> customCondition;
    private Runnable customAction;
    
    public ConditionalAction(ConditionType condition, ActionType action, double threshold, String actionData) {
        this.conditionType = condition;
        this.actionType = action;
        this.threshold = threshold;
        this.actionData = actionData;
    }
    
    public void setCustomCondition(Predicate<Minecraft> condition) { this.customCondition = condition; }
    public void setCustomAction(Runnable action) { this.customAction = action; }
    public void setDescription(String desc) { this.description = desc; }
    
    public boolean checkCondition(Minecraft client) {
        Player player = client.player;
        if (player == null) return false;
        
        switch (conditionType) {
            case HEALTH_BELOW:
                return player.getHealth() < threshold;
            case HEALTH_ABOVE:
                return player.getHealth() > threshold;
            case HUNGER_BELOW:
                // FIX: Mojmap 1.22 menggunakan player.getFoodData().getFoodLevel()
                return player.getFoodData().getFoodLevel() < threshold;
            case ITEM_COUNT_BELOW:
                return countItem(player, actionData) < threshold;
            case ITEM_COUNT_ABOVE:
                return countItem(player, actionData) > threshold;
            case INVENTORY_FULL:
                // Free slot check standard Mojmap
                return player.getInventory().getFreeSlot() == -1;
            case INVENTORY_EMPTY:
                return isInventoryEmpty(player);
            case IN_COMBAT:
                // FIX: getAttacker() -> getLastAttacker() & getLastHurtByMob() untuk akurasi combat state
                return player.getLastAttacker() != null || player.getLastHurtByMob() != null;
            case NOT_IN_COMBAT:
                // FIX: getAttacker() -> getLastAttacker() & getLastHurtByMob()
                return player.getLastAttacker() == null && player.getLastHurtByMob() == null;
            case TIME_PASSED:
                return (System.currentTimeMillis() - lastExecutionTime) >= threshold;
            case CUSTOM:
                return customCondition != null && customCondition.test(client);
            default:
                return false;
        }
    }
    
    public void executeAction(Minecraft client) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastExecutionTime < cooldown) return;
        
        lastExecutionTime = currentTime;
        
        switch (actionType) {
            case LOG_MESSAGE:
                System.out.println("[ConditionalAction] Triggered: " + actionData);
                break;
            case SEND_CHAT:
                if (client.player != null && actionData != null) {
                    if (actionData.startsWith("/")) {
                        client.player.connection.sendCommand(actionData.substring(1));
                    } else {
                        client.player.connection.sendChat(actionData);
                    }
                }
                break;
            case TOGGLE_BOT:
                break;
            case EMERGENCY_STOP:
                break;
            case CUSTOM:
                if (customAction != null) customAction.run();
                break;
            default:
                break;
        }
    }
    
    public void tick(Minecraft client) {
        if (checkCondition(client)) {
            executeAction(client);
        }
    }
    
    private int countItem(Player player, String itemName) {
        if (itemName == null || itemName.isEmpty()) return 0;
        int count = 0;
        // FIX: Menggunakan getContainerSize() standar Mojmap untuk ukuran inventory penampung
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem().toString().contains(itemName)) {
                count += stack.getCount();
            }
        }
        return count;
    }
    
    private boolean isInventoryEmpty(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
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
        return actions;
    }
}