// ============================================
// FILE: RealmTracker.java
// Path: src/main/java/com/bapelauto/realm/RealmTracker.java
// ============================================
package com.bapelauto.realm;

import com.bapelauto.SessionManager;
import com.bapelauto.ShardedConfigManager;
import net.minecraft.client.MinecraftClient;

public class RealmTracker {
    private final SessionManager sessionManager;
    private final ShardedConfigManager configManager;
    
    private String currentServerAddress = "";
    private String currentRealmName = "";
    private boolean isConnected = false;
    private long lastRealmChangeTime = 0;
    
    private static final long CHANGE_COOLDOWN = 1000;
    
    private boolean enableAutoLoad = true;
    private boolean enableResetPerRealm = true;
    
    public RealmTracker(SessionManager sessionManager, ShardedConfigManager configManager) {
        this.sessionManager = sessionManager;
        this.configManager = configManager;
        
        this.enableAutoLoad = configManager.getBoolean("enableAutoLoad", true);
        this.enableResetPerRealm = configManager.getBoolean("enableResetPerRealm", true);
    }
    
    public void tick(MinecraftClient client) {
        boolean connected = (client.player != null && client.getNetworkHandler() != null);
        String serverAddr = getServerAddress(client);
        String realmName = getRealmName(client);
        long currentTime = System.currentTimeMillis();
        
        if (connected && !realmName.isEmpty()) {
            sessionManager.updateRealmName(realmName);
        }
        
        // Join detection
        if (!isConnected && connected && !realmName.isEmpty()) {
            handleJoin(realmName, serverAddr, currentTime);
        }
        // Disconnect detection
        else if (isConnected && !connected) {
            handleDisconnect();
        }
        // Realm change detection
        else if (isConnected && connected && !realmName.isEmpty()) {
            if (!realmName.equals(currentRealmName)) {
                if (currentTime - lastRealmChangeTime > CHANGE_COOLDOWN) {
                    handleRealmChange(realmName, currentTime);
                }
            }
        }
    }
    
    private void handleJoin(String realmName, String serverAddr, long currentTime) {
        isConnected = true;
        currentServerAddress = serverAddr;
        currentRealmName = realmName;
        lastRealmChangeTime = currentTime;
        
        System.out.println("[RealmTracker] Joined: " + realmName);
        
        if (enableAutoLoad) {
            configManager.loadConfig();
            System.out.println("[RealmTracker] Config loaded for realm: " + realmName);
        }
    }
    
    private void handleDisconnect() {
        System.out.println("[RealmTracker] Disconnected from: " + currentRealmName);
        configManager.saveConfig();
        
        isConnected = false;
        currentServerAddress = "";
        currentRealmName = "";
    }
    
    private void handleRealmChange(String newRealm, long currentTime) {
        System.out.println("[RealmTracker] Realm changed: " + currentRealmName + " -> " + newRealm);
        
        configManager.saveConfig();
        currentRealmName = newRealm;
        lastRealmChangeTime = currentTime;
        
        if (enableAutoLoad) {
            configManager.loadConfig();
        }
    }
    
    private String getServerAddress(MinecraftClient client) {
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().address;
        }
        return "";
    }
    
    private String getRealmName(MinecraftClient client) {
        if (client.world != null && client.world.getRegistryKey() != null) {
            String fullPath = client.world.getRegistryKey().getValue().getPath();
            if (fullPath.contains(":")) {
                String[] parts = fullPath.split(":");
                return parts[parts.length - 1];
            }
            return fullPath;
        }
        return "";
    }
    
    public String getCurrentRealm() { return currentRealmName; }
    public String getCurrentServer() { return currentServerAddress; }
    public boolean isConnected() { return isConnected; }
    
    public boolean isEnableAutoLoad() { return enableAutoLoad; }
    public void setEnableAutoLoad(boolean enable) { 
        enableAutoLoad = enable;
        configManager.set("enableAutoLoad", enable);
    }
    
    public boolean isEnableResetPerRealm() { return enableResetPerRealm; }
    public void setEnableResetPerRealm(boolean enable) { 
        enableResetPerRealm = enable;
        configManager.set("enableResetPerRealm", enable);
    }
}