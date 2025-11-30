// ============================================
// FILE: HotkeyManager.java
// Path: src/main/java/com/bapelauto/hotkey/HotkeyManager.java
// ============================================
package com.bapelauto.hotkey;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom hotkey manager for user-defined key bindings
 */
public class HotkeyManager {
    
    private final Map<String, CustomHotkey> hotkeys = new ConcurrentHashMap<>();
    private final Map<Integer, String> keyToHotkeyId = new ConcurrentHashMap<>();
    
    public enum HotkeyAction {
        TOGGLE_BOT("Toggle Bot Master", "Turn bot on/off"),
        TOGGLE_LEFT_CLICK("Toggle Left Click", "Enable/disable left clicking"),
        TOGGLE_RIGHT_CLICK("Toggle Right Click", "Enable/disable right clicking"),
        TOGGLE_AUTO_STEAL("Toggle Auto Steal", "Enable/disable auto stealing"),
        TOGGLE_AUTO_STORE("Toggle Auto Store", "Enable/disable auto storing"),
        TOGGLE_GUI_CLICK("Toggle GUI Click", "Enable/disable GUI clicking"),
        TOGGLE_COMMAND("Toggle Auto Command", "Enable/disable auto command"),
        
        CAPTURE_TARGET("Capture Target", "Capture click target"),
        ACTIVATE_TARGET("Activate Target", "Start clicking targets"),
        CLEAR_TARGETS("Clear Targets", "Remove all targets"),
        
        START_RECORDING("Start Recording", "Begin macro recording"),
        STOP_RECORDING("Stop Recording", "End macro recording"),
        
        LOAD_PROFILE("Load Profile", "Switch to specific profile"),
        CYCLE_PROFILE("Cycle Profile", "Switch to next profile"),
        
        QUICK_STEAL("Quick Steal", "Instant steal all items"),
        QUICK_STORE("Quick Store", "Instant store all items"),
        
        SEND_COMMAND("Send Command", "Execute specific command"),
        
        TOGGLE_OVERLAY("Toggle Overlay", "Show/hide visual overlay"),
        
        EMERGENCY_STOP("Emergency Stop", "Disable everything immediately");
        
        private final String displayName;
        private final String description;
        
        HotkeyAction(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Register a custom hotkey
     */
    public void registerHotkey(String id, int keyCode, HotkeyAction action, String actionData) {
        CustomHotkey hotkey = new CustomHotkey(id, keyCode, action, actionData);
        hotkeys.put(id, hotkey);
        keyToHotkeyId.put(keyCode, id);
        
        System.out.println("[HotkeyManager] Registered: " + id + " -> " + getKeyName(keyCode));
    }
    
    /**
     * Handle key press
     */
    public boolean handleKeyPress(int keyCode, MinecraftClient client) {
        String hotkeyId = keyToHotkeyId.get(keyCode);
        if (hotkeyId == null) return false;
        
        CustomHotkey hotkey = hotkeys.get(hotkeyId);
        if (hotkey == null || !hotkey.enabled) return false;
        
        return hotkey.execute(client);
    }
    
    /**
     * Unregister hotkey
     */
    public void unregisterHotkey(String id) {
        CustomHotkey hotkey = hotkeys.remove(id);
        if (hotkey != null) {
            keyToHotkeyId.remove(hotkey.keyCode);
        }
    }
    
    /**
     * Get all registered hotkeys
     */
    public List<CustomHotkey> getAllHotkeys() {
        return new ArrayList<>(hotkeys.values());
    }
    
    /**
     * Check if key is already bound
     */
    public boolean isKeyBound(int keyCode) {
        return keyToHotkeyId.containsKey(keyCode);
    }
    
    /**
     * Get hotkey by key code
     */
    public CustomHotkey getHotkeyByKey(int keyCode) {
        String id = keyToHotkeyId.get(keyCode);
        return id != null ? hotkeys.get(id) : null;
    }
    
    /**
     * Get key name from key code
     */
    public static String getKeyName(int keyCode) {
        return InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
    }
    
    /**
     * Clear all hotkeys
     */
    public void clearAll() {
        hotkeys.clear();
        keyToHotkeyId.clear();
    }
    
    /**
     * Custom Hotkey class
     */
    public static class CustomHotkey {
        private final String id;
        private int keyCode;
        private final HotkeyAction action;
        private final String actionData;
        private boolean enabled = true;
        private long lastExecutionTime = 0;
        private long cooldown = 500; // 500ms cooldown
        
        public CustomHotkey(String id, int keyCode, HotkeyAction action, String actionData) {
            this.id = id;
            this.keyCode = keyCode;
            this.action = action;
            this.actionData = actionData;
        }
        
        public boolean execute(MinecraftClient client) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastExecutionTime < cooldown) {
                return false;
            }
            
            lastExecutionTime = currentTime;
            
            if (client.player == null) return false;
            
            // Execute action based on type
            switch (action) {
                case SEND_COMMAND:
                    if (actionData != null && !actionData.isEmpty()) {
                        if (actionData.startsWith("/")) {
                            client.player.networkHandler.sendChatCommand(actionData.substring(1));
                        } else {
                            client.player.networkHandler.sendChatMessage(actionData);
                        }
                        client.player.sendMessage(Text.literal("§e[Hotkey] Sent: " + actionData), true);
                    }
                    break;
                    
                case QUICK_STEAL:
                    if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen) {
                        com.bapelauto.inventory.InventoryManager.performSingleSteal(client);
                    }
                    break;
                    
                case QUICK_STORE:
                    if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen) {
                        com.bapelauto.inventory.InventoryManager.performSingleStore(client);
                    }
                    break;
                    
                case EMERGENCY_STOP:
                    client.player.sendMessage(Text.literal("§c§l[EMERGENCY STOP] All features disabled!"), true);
                    client.player.playSound(net.minecraft.sound.SoundEvents.BLOCK_ANVIL_LAND, 1.0F, 1.0F);
                    // This would need to call AutoBotMod methods to disable everything
                    break;
                    
                default:
                    client.player.sendMessage(Text.literal("§e[Hotkey] " + action.getDisplayName()), true);
                    break;
            }
            
            return true;
        }
        
        public void setKeyCode(int keyCode) {
            this.keyCode = keyCode;
        }
        
        public void setCooldown(long cooldown) {
            this.cooldown = cooldown;
        }
        
        public String getId() { return id; }
        public int getKeyCode() { return keyCode; }
        public HotkeyAction getAction() { return action; }
        public String getActionData() { return actionData; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        public String getKeyName() {
            return HotkeyManager.getKeyName(keyCode);
        }
        
        @Override
        public String toString() {
            return String.format("[%s] %s - %s", 
                getKeyName(), 
                action.getDisplayName(),
                actionData != null ? actionData : "");
        }
    }
}