// ============================================
// FILE: AutoBotMod.java (FIXED GLFW CRASH)
// Path: src/main/java/com/bapelauto/AutoBotMod.java
// ============================================
package com.bapelauto;

import com.bapelauto.click.*;
import com.bapelauto.world.WorldInteractionManager;
import com.bapelauto.inventory.InventoryManager;
import com.bapelauto.stats.StatsTracker;
import com.bapelauto.realm.RealmTracker;
import com.bapelauto.profile.ProfileManager;
import com.bapelauto.smart.SmartDetector;
import com.bapelauto.conditional.ConditionalAction;
import com.bapelauto.visual.VisualOverlay;
import com.bapelauto.scheduler.Scheduler;
import com.bapelauto.hotkey.HotkeyManager;
import com.bapelauto.slimefun.SlimefunAutoManager;
import com.bapelauto.slimefun.SlimefunDetector;
import com.bapelauto.slimefun.SlimefunConfigScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class AutoBotMod implements ClientModInitializer {
    
    public static final String MOD_ID = "bapelauto";
    public static final String VERSION = "5.3-FIXED"; // Updated version marker
    
    // Core Managers
    private static SessionManager sessionManager;
    private static ShardedConfigManager configManager;
    private static GuiClickManager guiClickManager;
    private static WorldInteractionManager worldManager;
    private static InventoryManager inventoryManager;
    private static RealmTracker realmTracker;
    private static StatsTracker statsTracker;
    
    // Advanced Managers
    private static ProfileManager profileManager;
    private static VisualOverlay visualOverlay;
    private static Scheduler scheduler;
    private static HotkeyManager hotkeyManager;
    private static List<ConditionalAction> conditionalActions;
    
    // Slimefun Manager
    private static SlimefunAutoManager slimefunManager;
    
    // Keybindings
    private static KeyBinding toggleBotKey;
    private static KeyBinding openConfigKey;
    private static KeyBinding captureTargetKey;
    private static KeyBinding toggleTargetKey;
    private static KeyBinding recordMacroKey;
    private static KeyBinding stopRecordingKey;
    private static KeyBinding smartDetectKey;
    private static KeyBinding cycleProfileKey;
    private static KeyBinding toggleOverlayKey;
    private static KeyBinding emergencyStopKey;
    private static KeyBinding slimefunQuickSetupKey;
    private static KeyBinding openSlimefunConfigKey;
    
    // Status & Flags
    private static boolean botRunning = false;
    private boolean hotkeysInitialized = false; // Flag for lazy init
    
    // Basic configs
    private static boolean commandEnabled = false;
    private static boolean showGuiButtons = true;
    private static String command = "/sell all";
    private static long commandDelay = 60000;
    private static long lastCommandTime = Long.MAX_VALUE;
    
    @Override
    public void onInitializeClient() {
        System.out.println("[AutoBot] Starting initialization sequence...");
        try {
            // 1. Initialize CORE managers (Memory objects only)
            sessionManager = new SessionManager();
            configManager = new ShardedConfigManager(sessionManager);
            statsTracker = new StatsTracker();
            
            // 2. Initialize Logic Managers
            guiClickManager = new GuiClickManager(); 
            visualOverlay = new VisualOverlay(); 
            worldManager = new WorldInteractionManager();
            inventoryManager = new InventoryManager();
            realmTracker = new RealmTracker(sessionManager, configManager);
            scheduler = new Scheduler();
            hotkeyManager = new HotkeyManager();
            conditionalActions = new ArrayList<>();
            slimefunManager = new SlimefunAutoManager();
            profileManager = new ProfileManager(configManager);
            
            // 3. Load Configuration
            loadConfiguration();
            
            // NOTE: initializeDefaultHotkeys() removed from here to prevent GLFW crash
            
            System.out.println("[AutoBot v" + VERSION + "] CORE MANAGERS READY");
            
            // 4. Register Keybindings (Safe to do here as they are just registration)
            registerKeybindings();
            
            // 5. Register Events
            registerEvents();
            
            System.out.println("[AutoBot v" + VERSION + "] PRE-INIT COMPLETE");
            
            // Shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            
        } catch (Exception e) {
            System.err.println("[AutoBot] FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadConfiguration() {
        try {
            // Safety Init
            if (guiClickManager == null) guiClickManager = new GuiClickManager();
            if (visualOverlay == null) visualOverlay = new VisualOverlay();
            if (worldManager == null) worldManager = new WorldInteractionManager();
            if (inventoryManager == null) inventoryManager = new InventoryManager();
            if (slimefunManager == null) slimefunManager = new SlimefunAutoManager();

            // Load logic
            commandEnabled = configManager.getBoolean("commandEnabled", false);
            showGuiButtons = configManager.getBoolean("showGuiButtons", true);
            command = configManager.getString("command", "/sell all");
            commandDelay = configManager.getLong("commandDelay", 60000);
            
            worldManager.loadFromConfig(configManager);
            inventoryManager.loadFromConfig(configManager);
            
            long baseDelay = configManager.getLong("targetClickDelay", 100);
            guiClickManager.setBaseDelay(baseDelay);
            
            try {
                String patternStr = configManager.getString("timingPattern", "FIXED");
                guiClickManager.setTimingPattern(TimingPattern.valueOf(patternStr));
            } catch (Exception e) {
                guiClickManager.setTimingPattern(TimingPattern.FIXED);
            }
            
            visualOverlay.setEnabled(configManager.getBoolean("overlayEnabled", true));
            visualOverlay.setShowStats(configManager.getBoolean("overlayShowStats", true));
            
            slimefunManager.setSlimefunModeEnabled(configManager.getBoolean("slimefunModeEnabled", false));
            slimefunManager.setSafetyMode(configManager.getBoolean("slimefunSafetyMode", true));
            
        } catch (Exception e) {
            System.err.println("[AutoBot] Config load error (non-fatal): " + e.getMessage());
        }
    }

    private void registerKeybindings() {
        // Core
        toggleBotKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_0, "category.bapelauto"));
        openConfigKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_HOME, "category.bapelauto"));
        captureTargetKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.capture", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, "category.bapelauto"));
        toggleTargetKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.activate_target", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, "category.bapelauto"));
        recordMacroKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.record", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_BRACKET, "category.bapelauto"));
        stopRecordingKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.stop_record", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, "category.bapelauto"));
        
        // Advanced
        smartDetectKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.smart_detect", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "category.bapelauto"));
        cycleProfileKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.cycle_profile", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.bapelauto"));
        toggleOverlayKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.toggle_overlay", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.bapelauto"));
        emergencyStopKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.emergency_stop", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_END, "category.bapelauto"));
        
        // Slimefun
        slimefunQuickSetupKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.slimefun_quick", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_BRACKET, "category.bapelauto.slimefun"));
        openSlimefunConfigKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.slimefun_config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_INSERT, "category.bapelauto.slimefun"));
    }
    
    private void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
        
        // Safe logic ticks
        ClientTickEvents.END_CLIENT_TICK.register(client -> { if(realmTracker != null) realmTracker.tick(client); });
        ClientTickEvents.END_CLIENT_TICK.register(client -> { if(scheduler != null) scheduler.tick(client); });
        ClientTickEvents.END_CLIENT_TICK.register(client -> { if(slimefunManager != null) slimefunManager.tick(client); });
        
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.allowKeyPress(screen).register((_screen, key, scancode, modifiers) -> {
                if (!botRunning) return true;
                
                // Hotkey interception
                if (guiClickManager != null) {
                    if (captureTargetKey.matchesKey(key, scancode)) {
                        guiClickManager.captureTarget(client, 100);
                        return false;
                    }
                    if (toggleTargetKey.matchesKey(key, scancode)) {
                        guiClickManager.toggle(client);
                        return false;
                    }
                    if (recordMacroKey.matchesKey(key, scancode)) {
                        guiClickManager.startRecording(client);
                        return false;
                    }
                    if (stopRecordingKey.matchesKey(key, scancode)) {
                        guiClickManager.stopRecording(client);
                        return false;
                    }
                }
                
                if (smartDetectKey.matchesKey(key, scancode)) {
                    performSmartDetect(client);
                    return false;
                }
                
                if (slimefunQuickSetupKey.matchesKey(key, scancode)) {
                    performSlimefunQuickSetup(client);
                    return false;
                }
                
                // Custom hotkeys handler
                if (hotkeyManager != null && hotkeyManager.handleKeyPress(key, client)) {
                    return false;
                }
                
                return true;
            });
        });
    }
    
    private void handleTick(MinecraftClient client) {
        // LAZY INITIALIZATION - Fix for GLFW crash
        // Kita hanya register hotkey default SETELAH game berjalan dan player ada
        if (!hotkeysInitialized && client.player != null) {
            initializeDefaultHotkeys();
            hotkeysInitialized = true;
            System.out.println("[AutoBot] Late initialization complete.");
        }

        if (client.player == null) return;
        
        // Key checks
        if (toggleBotKey.wasPressed()) toggleMaster(client);
        if (openConfigKey.wasPressed()) client.setScreen(new AutoBotConfigScreen(client.currentScreen));
        if (openSlimefunConfigKey.wasPressed()) client.setScreen(new SlimefunConfigScreen(client.currentScreen));
        
        if (smartDetectKey.wasPressed() && client.currentScreen != null) performSmartDetect(client);
        if (slimefunQuickSetupKey.wasPressed() && client.currentScreen != null) performSlimefunQuickSetup(client);
        
        if (cycleProfileKey.wasPressed() && profileManager != null) profileManager.cycleProfile(client);
        
        if (toggleOverlayKey.wasPressed() && visualOverlay != null) {
            visualOverlay.toggleAll();
            String status = visualOverlay.isEnabled() ? "§aON" : "§cOFF";
            client.player.sendMessage(Text.literal("§e[Overlay] " + status), true);
        }
        
        if (emergencyStopKey.wasPressed()) performEmergencyStop(client);
        
        if (botRunning) executeBot(client);
    }
    
    private void toggleMaster(MinecraftClient client) {
        botRunning = !botRunning;
        
        if (botRunning) {
            if(statsTracker != null) statsTracker.startSession();
            lastCommandTime = System.currentTimeMillis();
            
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§a§l[AutoBot] MASTER ON"), true);
                if (slimefunManager != null && slimefunManager.isInSlimefunGUI(client)) {
                    client.player.sendMessage(Text.literal("§e[Tip] Press Slimefun quick setup key!"), false);
                }
                client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 1.0F);
            }
        } else {
            // Disable all systems
            if(worldManager != null) worldManager.disableAll();
            if(inventoryManager != null) inventoryManager.disableAll();
            if(guiClickManager != null) guiClickManager.clearTargets();
            commandEnabled = false;
            
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§c§l[AutoBot] MASTER OFF"), true);
                client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 1.0F, 1.0F);
            }
        }
    }
    
    private void executeBot(MinecraftClient client) {
        long currentTime = System.currentTimeMillis();
        
        // GUI interactions
        if (client.currentScreen != null) {
            if (client.currentScreen instanceof HandledScreen && inventoryManager != null) {
                inventoryManager.tick(client, (HandledScreen<?>) client.currentScreen);
            }
            
            if (guiClickManager != null) guiClickManager.tick(client);
            
            if (commandEnabled && !command.isEmpty() && (currentTime - lastCommandTime) >= commandDelay) {
                sendCommand(client);
                lastCommandTime = currentTime;
            }
        }
        // World interactions
        else {
            if (worldManager != null) worldManager.tick(client);
            
            if (commandEnabled && !command.isEmpty() && (currentTime - lastCommandTime) >= commandDelay) {
                sendCommand(client);
                lastCommandTime = currentTime;
            }
        }
        
        // Conditional actions
        if (conditionalActions != null) {
            for (ConditionalAction action : conditionalActions) {
                action.tick(client);
            }
        }
    }
    
    private void sendCommand(MinecraftClient client) {
        if (client.player == null || command.trim().isEmpty()) return;
        try {
            if (command.startsWith("/")) {
                client.player.networkHandler.sendChatCommand(command.substring(1));
            } else {
                client.player.networkHandler.sendChatMessage(command);
            }
            if(statsTracker != null) statsTracker.incrementCommands();
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void performSmartDetect(MinecraftClient client) {
        if (client.currentScreen == null) return;
        
        if (SlimefunDetector.isSlimefunGUI(client.currentScreen)) {
            performSlimefunQuickSetup(client);
            return;
        }
        
        SmartDetector.GuiType guiType = SmartDetector.detectGuiType(client.currentScreen);
        if (guiType != SmartDetector.GuiType.UNKNOWN) {
            SmartDetector.autoConfigureForGui(client, guiType);
            List<ClickTarget> targets = SmartDetector.autoDetectTargets(client);
            if (!targets.isEmpty() && guiClickManager != null) {
                guiClickManager.clearTargets();
                try {
                    guiClickManager.setTimingPattern(TimingPattern.valueOf(SmartDetector.suggestTimingPattern(guiType)));
                } catch (Exception e) { /* ignore */ }
                
                long[] delays = SmartDetector.suggestDelays(guiType);
                guiClickManager.setBaseDelay(delays[0]);
                guiClickManager.setMinDelay(delays[1]);
                guiClickManager.setMaxDelay(delays[2]);
            }
        }
    }
    
    private void performSlimefunQuickSetup(MinecraftClient client) {
        if (client.currentScreen == null || slimefunManager == null) return;
        if (!SlimefunDetector.isSlimefunGUI(client.currentScreen)) {
            if (client.player != null) client.player.sendMessage(Text.literal("§c[Slimefun] Not a Slimefun machine GUI"), false);
            return;
        }
        slimefunManager.quickSetup(client);
    }
    
    private void performEmergencyStop(MinecraftClient client) {
        botRunning = false;
        if(worldManager != null) worldManager.disableAll();
        if(inventoryManager != null) inventoryManager.disableAll();
        if(guiClickManager != null) guiClickManager.clearTargets();
        commandEnabled = false;
        if(scheduler != null) scheduler.setEnabled(false);
        if(slimefunManager != null) slimefunManager.setSlimefunModeEnabled(false);
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§c§l[EMERGENCY] ALL SYSTEMS DISABLED!"), true);
            client.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, 1.0F, 0.8F);
        }
    }
    
    // Method ini sekarang dipanggil dari handleTick(), bukan onInitializeClient()
    private void initializeDefaultHotkeys() {
        if (hotkeyManager == null) return;
        try {
            hotkeyManager.registerHotkey("quick_steal", GLFW.GLFW_KEY_KP_7, HotkeyManager.HotkeyAction.QUICK_STEAL, null);
            hotkeyManager.registerHotkey("quick_store", GLFW.GLFW_KEY_KP_9, HotkeyManager.HotkeyAction.QUICK_STORE, null);
        } catch (Exception e) {
            System.err.println("[AutoBot] Failed to register default hotkeys: " + e.getMessage());
        }
    }
    
    public static void saveConfiguration() {
        if (configManager == null) return;
        
        configManager.set("commandEnabled", commandEnabled);
        configManager.set("showGuiButtons", showGuiButtons);
        configManager.set("command", command);
        configManager.set("commandDelay", commandDelay);
        
        if(worldManager != null) worldManager.saveToConfig(configManager);
        if(inventoryManager != null) inventoryManager.saveToConfig(configManager);
        
        if(visualOverlay != null) {
            configManager.set("overlayEnabled", visualOverlay.isEnabled());
            configManager.set("overlayShowStats", visualOverlay.isShowStats());
        }
        
        if(slimefunManager != null) {
            configManager.set("slimefunModeEnabled", slimefunManager.isSlimefunModeEnabled());
            configManager.set("slimefunSafetyMode", slimefunManager.isSafetyMode());
        }
        
        configManager.saveConfig();
        System.out.println("[AutoBot] Configuration saved");
    }
    
    private void shutdown() {
        saveConfiguration();
        if(configManager != null) configManager.cleanup();
        if(sessionManager != null) sessionManager.shutdown();
    }
    
    // Public Accessors (Null-Safe)
    public static boolean isRunning() { return botRunning; }
    public static boolean isShowGuiButtons() { return showGuiButtons; }
    public static void setShowGuiButtons(boolean show) { showGuiButtons = show; }
    
    public static GuiClickManager getGuiClickManager() { return guiClickManager; }
    public static WorldInteractionManager getWorldManager() { return worldManager; }
    public static InventoryManager getInventoryManager() { return inventoryManager; }
    public static StatsTracker getStatsTracker() { return statsTracker; }
    public static SessionManager getSessionManager() { return sessionManager; }
    public static ShardedConfigManager getConfigManager() { return configManager; }
    public static RealmTracker getRealmTracker() { return realmTracker; }
    public static ProfileManager getProfileManager() { return profileManager; }
    public static VisualOverlay getVisualOverlay() { return visualOverlay; }
    public static Scheduler getScheduler() { return scheduler; }
    public static HotkeyManager getHotkeyManager() { return hotkeyManager; }
    public static List<ConditionalAction> getConditionalActions() { return conditionalActions; }
    public static SlimefunAutoManager getSlimefunManager() { return slimefunManager; }
    
    public static boolean isCommandEnabled() { return commandEnabled; }
    public static void setCommandEnabled(boolean enabled) { commandEnabled = enabled; }
    public static String getCommand() { return command; }
    public static void setCommand(String cmd) { command = cmd; }
    public static long getCommandDelay() { return commandDelay; }
    public static void setCommandDelay(long delay) { commandDelay = Math.max(1000, delay); }
}