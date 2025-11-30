// ============================================
// FILE: AutoBotMod.java (FINAL - WITH SLIMEFUN)
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
import com.bapelauto.slimefun.SlimefunAutoManager;      // NEW
import com.bapelauto.slimefun.SlimefunDetector;         // NEW
import com.bapelauto.slimefun.SlimefunProfilePresets;   // NEW

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
    public static final String VERSION = "5.1-SLIMEFUN";
    
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
    
    // Slimefun Manager (NEW)
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
    private static KeyBinding slimefunQuickSetupKey;  // NEW
    private static KeyBinding openSlimefunConfigKey;  // NEW
    
    // Status
    private static boolean botRunning = false;
    
    // Basic configs
    private static boolean commandEnabled = false;
    private static boolean showGuiButtons = true;
    private static String command = "/sell all";
    private static long commandDelay = 60000;
    private static long lastCommandTime = Long.MAX_VALUE;
    
    @Override
    public void onInitializeClient() {
        // Initialize core managers
        sessionManager = new SessionManager();
        configManager = new ShardedConfigManager(sessionManager);
        guiClickManager = new GuiClickManager();
        worldManager = new WorldInteractionManager();
        inventoryManager = new InventoryManager();
        realmTracker = new RealmTracker(sessionManager, configManager);
        statsTracker = new StatsTracker();
        
        // Initialize advanced managers
        profileManager = new ProfileManager(configManager);
        visualOverlay = new VisualOverlay();
        scheduler = new Scheduler();
        hotkeyManager = new HotkeyManager();
        conditionalActions = new ArrayList<>();
        
        // Initialize Slimefun manager (NEW)
        slimefunManager = new SlimefunAutoManager();
        
        // Load config
        loadConfiguration();
        
        // Initialize default hotkeys
        initializeDefaultHotkeys();
        
        System.out.println("[AutoBot v" + VERSION + "] ALL FEATURES INITIALIZED");
        System.out.println("[AutoBot] ✓ Core automation");
        System.out.println("[AutoBot] ✓ Advanced features");
        System.out.println("[AutoBot] ✓ Slimefun support");
        System.out.println("[AutoBot] Session: " + sessionManager.getSessionId());
        
        // Register keybindings
        registerKeybindings();
        
        // Register events
        registerEvents();
        
        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    private void registerKeybindings() {
        // Core keybindings
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
        
        // Advanced keybindings
        smartDetectKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.smart_detect", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "category.bapelauto"));
        cycleProfileKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.cycle_profile", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.bapelauto"));
        toggleOverlayKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.toggle_overlay", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.bapelauto"));
        emergencyStopKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.emergency_stop", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_END, "category.bapelauto"));
        
        // Slimefun keybindings (NEW)
        slimefunQuickSetupKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.slimefun_quick", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "category.bapelauto.slimefun"));
        openSlimefunConfigKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key.bapelauto.slimefun_config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_INSERT, "category.bapelauto.slimefun"));
    }
    
    private void registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(this::handleTick);
        ClientTickEvents.END_CLIENT_TICK.register(client -> realmTracker.tick(client));
        ClientTickEvents.END_CLIENT_TICK.register(client -> scheduler.tick(client));
        ClientTickEvents.END_CLIENT_TICK.register(client -> slimefunManager.tick(client)); // NEW
        
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            ScreenKeyboardEvents.allowKeyPress(screen).register((_screen, key, scancode, modifiers) -> {
                if (!botRunning) return true;
                
                // Core hotkeys
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
                
                // Advanced hotkeys
                if (smartDetectKey.matchesKey(key, scancode)) {
                    performSmartDetect(client);
                    return false;
                }
                
                // Slimefun hotkeys (NEW)
                if (slimefunQuickSetupKey.matchesKey(key, scancode)) {
                    performSlimefunQuickSetup(client);
                    return false;
                }
                
                // Custom hotkeys
                if (hotkeyManager.handleKeyPress(key, client)) {
                    return false;
                }
                
                return true;
            });
        });
    }
    
    private void handleTick(MinecraftClient client) {
        if (client.player == null) return;
        
        // Toggle master switch
        if (toggleBotKey.wasPressed()) {
            toggleMaster(client);
        }
        
        // Open config
        if (openConfigKey.wasPressed()) {
            client.setScreen(new AutoBotConfigScreen(client.currentScreen));
        }
        
        // Open Slimefun config (NEW)
        if (openSlimefunConfigKey.wasPressed()) {
            client.setScreen(new com.bapelauto.slimefun.SlimefunConfigScreen(client.currentScreen));
        }
        
        // Advanced keybindings
        if (smartDetectKey.wasPressed() && client.currentScreen != null) {
            performSmartDetect(client);
        }
        
        // Slimefun quick setup (NEW)
        if (slimefunQuickSetupKey.wasPressed() && client.currentScreen != null) {
            performSlimefunQuickSetup(client);
        }
        
        if (cycleProfileKey.wasPressed()) {
            profileManager.cycleProfile(client);
        }
        
        if (toggleOverlayKey.wasPressed()) {
            visualOverlay.toggleAll();
            String status = visualOverlay.isEnabled() ? "§aON" : "§cOFF";
            client.player.sendMessage(Text.literal("§e[Overlay] " + status), true);
        }
        
        if (emergencyStopKey.wasPressed()) {
            performEmergencyStop(client);
        }
        
        // Execute bot logic
        if (botRunning) {
            executeBot(client);
            
            // Tick conditional actions
            for (ConditionalAction action : conditionalActions) {
                action.tick(client);
            }
        }
    }
    
    private void toggleMaster(MinecraftClient client) {
        botRunning = !botRunning;
        
        if (botRunning) {
            statsTracker.startSession();
            lastCommandTime = System.currentTimeMillis();
            
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§a§l[AutoBot] MASTER ON"), true);
                
                // Show Slimefun hint if in Slimefun GUI
                if (slimefunManager.isInSlimefunGUI(client)) {
                    client.player.sendMessage(
                        Text.literal("§e[Tip] Press [\\] for Slimefun quick setup!"), false
                    );
                }
                
                client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 1.0F);
            }
        } else {
            worldManager.disableAll();
            inventoryManager.disableAll();
            guiClickManager.clearTargets();
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
            if (client.currentScreen instanceof HandledScreen) {
                inventoryManager.tick(client, (HandledScreen<?>) client.currentScreen);
            }
            
            guiClickManager.tick(client);
            
            if (commandEnabled && !command.isEmpty() && (currentTime - lastCommandTime) >= commandDelay) {
                sendCommand(client);
                lastCommandTime = currentTime;
            }
        }
        // World interactions
        else {
            worldManager.tick(client);
            
            if (commandEnabled && !command.isEmpty() && (currentTime - lastCommandTime) >= commandDelay) {
                sendCommand(client);
                lastCommandTime = currentTime;
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
            statsTracker.incrementCommands();
        } catch (Exception e) {
            // Ignore
        }
    }
    
    // Smart detection
    private void performSmartDetect(MinecraftClient client) {
        if (client.currentScreen == null) return;
        
        // Check if it's a Slimefun GUI first (NEW)
        if (SlimefunDetector.isSlimefunGUI(client.currentScreen)) {
            performSlimefunQuickSetup(client);
            return;
        }
        
        // Regular smart detection
        SmartDetector.GuiType guiType = SmartDetector.detectGuiType(client.currentScreen);
        
        if (guiType != SmartDetector.GuiType.UNKNOWN) {
            SmartDetector.autoConfigureForGui(client, guiType);
            
            List<ClickTarget> targets = SmartDetector.autoDetectTargets(client);
            if (!targets.isEmpty()) {
                guiClickManager.clearTargets();
                
                String pattern = SmartDetector.suggestTimingPattern(guiType);
                try {
                    guiClickManager.setTimingPattern(TimingPattern.valueOf(pattern));
                } catch (Exception e) {
                    // Use default
                }
                
                long[] delays = SmartDetector.suggestDelays(guiType);
                guiClickManager.setBaseDelay(delays[0]);
                guiClickManager.setMinDelay(delays[1]);
                guiClickManager.setMaxDelay(delays[2]);
            }
        }
    }
    
    // Slimefun quick setup (NEW)
    private void performSlimefunQuickSetup(MinecraftClient client) {
        if (client.currentScreen == null) return;
        
        if (!SlimefunDetector.isSlimefunGUI(client.currentScreen)) {
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§c[Slimefun] Not a Slimefun machine GUI"), false
                );
            }
            return;
        }
        
        slimefunManager.quickSetup(client);
    }
    
    // Emergency stop
    private void performEmergencyStop(MinecraftClient client) {
        botRunning = false;
        worldManager.disableAll();
        inventoryManager.disableAll();
        guiClickManager.clearTargets();
        commandEnabled = false;
        scheduler.setEnabled(false);
        slimefunManager.setSlimefunModeEnabled(false); // NEW
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§c§l[EMERGENCY] ALL SYSTEMS DISABLED!"), true);
            client.player.playSound(SoundEvents.BLOCK_ANVIL_LAND, 1.0F, 0.8F);
        }
    }
    
    // Initialize default hotkeys
    private void initializeDefaultHotkeys() {
        hotkeyManager.registerHotkey(
            "quick_steal",
            GLFW.GLFW_KEY_KP_7,
            HotkeyManager.HotkeyAction.QUICK_STEAL,
            null
        );
        
        hotkeyManager.registerHotkey(
            "quick_store",
            GLFW.GLFW_KEY_KP_9,
            HotkeyManager.HotkeyAction.QUICK_STORE,
            null
        );
    }
    
    private void loadConfiguration() {
        commandEnabled = configManager.getBoolean("commandEnabled", false);
        showGuiButtons = configManager.getBoolean("showGuiButtons", true);
        command = configManager.getString("command", "/sell all");
        commandDelay = configManager.getLong("commandDelay", 60000);
        
        worldManager.loadFromConfig(configManager);
        inventoryManager.loadFromConfig(configManager);
        guiClickManager.setBaseDelay(configManager.getLong("targetClickDelay", 100));
        guiClickManager.setTimingPattern(
            TimingPattern.valueOf(configManager.getString("timingPattern", "FIXED"))
        );
        
        // Load overlay settings
        visualOverlay.setEnabled(configManager.getBoolean("overlayEnabled", true));
        visualOverlay.setShowStats(configManager.getBoolean("overlayShowStats", true));
        
        // Load Slimefun settings (NEW)
        boolean slimefunMode = configManager.getBoolean("slimefunModeEnabled", false);
        slimefunManager.setSlimefunModeEnabled(slimefunMode);
        slimefunManager.setSafetyMode(configManager.getBoolean("slimefunSafetyMode", true));
    }
    
    public static void saveConfiguration() {
        configManager.set("commandEnabled", commandEnabled);
        configManager.set("showGuiButtons", showGuiButtons);
        configManager.set("command", command);
        configManager.set("commandDelay", commandDelay);
        
        worldManager.saveToConfig(configManager);
        inventoryManager.saveToConfig(configManager);
        
        // Save overlay settings
        configManager.set("overlayEnabled", visualOverlay.isEnabled());
        configManager.set("overlayShowStats", visualOverlay.isShowStats());
        
        // Save Slimefun settings (NEW)
        configManager.set("slimefunModeEnabled", slimefunManager.isSlimefunModeEnabled());
        configManager.set("slimefunSafetyMode", slimefunManager.isSafetyMode());
        
        configManager.saveConfig();
        System.out.println("[AutoBot] Configuration saved");
    }
    
    private void shutdown() {
        saveConfiguration();
        configManager.cleanup();
        sessionManager.shutdown();
    }
    
    // Public accessors
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
    
    // Advanced accessors
    public static ProfileManager getProfileManager() { return profileManager; }
    public static VisualOverlay getVisualOverlay() { return visualOverlay; }
    public static Scheduler getScheduler() { return scheduler; }
    public static HotkeyManager getHotkeyManager() { return hotkeyManager; }
    public static List<ConditionalAction> getConditionalActions() { return conditionalActions; }
    
    // Slimefun accessor (NEW)
    public static SlimefunAutoManager getSlimefunManager() { return slimefunManager; }
    
    public static boolean isCommandEnabled() { return commandEnabled; }
    public static void setCommandEnabled(boolean enabled) { commandEnabled = enabled; }
    public static String getCommand() { return command; }
    public static void setCommand(String cmd) { command = cmd; }
    public static long getCommandDelay() { return commandDelay; }
    public static void setCommandDelay(long delay) { commandDelay = Math.max(1000, delay); }
}