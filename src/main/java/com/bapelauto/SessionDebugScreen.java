// ============================================
// FILE: SessionDebugScreen.java (FIXED)
// Path: src/main/java/com/bapelauto/SessionDebugScreen.java
// ============================================
package com.bapelauto;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Debug screen for monitoring active sessions and shard status
 */
public class SessionDebugScreen extends Screen {
    private final Screen parent;
    private final SessionManager sessionManager;
    private final ShardedConfigManager configManager;
    
    private List<SessionManager.SessionInfo> sessions;
    private long lastRefresh = 0;
    private static final long REFRESH_INTERVAL = 1000;
    
    public SessionDebugScreen(Screen parent) {
        super(Component.literal("Session Debug"));
        this.parent = parent;
        this.sessionManager = AutoBotMod.getSessionManager();
        this.configManager = AutoBotMod.getConfigManager();
        refreshSessions();
    }
    
    private void refreshSessions() {
        sessions = sessionManager.listActiveSessions();
        lastRefresh = System.currentTimeMillis();
    }
    
    @Override
    protected void init() {
        int cx = this.width / 2;
        int btnY = this.height - 40;
        
        // Close button
        this.addRenderableWidget(Button.builder(Component.literal("§c✖ Close"), b -> {
            this.close();
        }).bounds(cx - 100, btnY, 90, 20).build());
        
        // Refresh button
        this.addRenderableWidget(Button.builder(Component.literal("§a⟳ Refresh"), b -> {
            refreshSessions();
        }).bounds(cx + 10, btnY, 90, 20).build());
        
        // Force cleanup button
        this.addRenderableWidget(Button.builder(Component.literal("§e🧹 Cleanup"), b -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("§e[Session] Running cleanup..."), false);
            }
            refreshSessions();
        }).bounds(cx - 100, btnY - 30, 90, 20).build());
        
        // Export global config
        this.addRenderableWidget(Button.builder(Component.literal("§b📤 Export"), b -> {
            configManager.exportToGlobal();
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("§a[Config] Exported to global"), false);
            }
        }).bounds(cx + 10, btnY - 30, 90, 20).build());
    }
    
    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // Background
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        super.render(context, mouseX, mouseY, delta);
        
        // Auto-refresh
        if (System.currentTimeMillis() - lastRefresh > REFRESH_INTERVAL) {
            refreshSessions();
        }
        
        int cx = this.width / 2;
        int y = 30;
        
        // Title
        context.centeredText(this.font, 
            Component.literal("§6§lSession Manager Debug"), cx, y, 0xFFFFFF);
        y += 20;
        
        // Current session info
        String currentSessionId = sessionManager.getSessionId();
        String shortId = currentSessionId.length() > 30 ? 
            currentSessionId.substring(0, 30) + "..." : currentSessionId;
        
        context.centeredText(this.font, 
            Component.literal("§aCurrent Session: §f" + shortId), cx, y, 0xAAAAAA);
        y += 15;
        
        context.centeredText(this.font, 
            Component.literal("§aShard ID: §f" + configManager.getShardId().substring(0, 30) + "..."), cx, y, 0xAAAAAA);
        y += 25;
        
        // Separator
        context.fill(cx - 200, y, cx + 200, y + 1, 0xFF444444);
        y += 15;
        
        // Active sessions header
        context.centeredText(this.font, 
            Component.literal("§e§lActive Sessions: §f" + sessions.size()), cx, y, 0xFFFF55);
        y += 20;
        
        // List sessions
        if (sessions.isEmpty()) {
            context.centeredText(this.font, 
                Component.literal("§7No active sessions found"), cx, y, 0x888888);
        } else {
            for (SessionManager.SessionInfo session : sessions) {
                boolean isCurrent = session.sessionId.equals(currentSessionId);
                String prefix = isCurrent ? "§a▶ " : "§7  ";
                
                // Session ID (shortened)
                String displayId = session.sessionId.length() > 35 ? 
                    session.sessionId.substring(0, 35) + "..." : session.sessionId;
                
                context.text(this.font, 
                    Component.literal(prefix + displayId), 
                    cx - 190, y, isCurrent ? 0x55FF55 : 0xCCCCCC);
                y += 12;
                
                // Session details
                String realm = session.realmName != null && !session.realmName.isEmpty() ? 
                    session.realmName : "Not Connected";
                long uptime = (System.currentTimeMillis() - session.startTime) / 1000;
                String uptimeStr = formatUptime(uptime);
                long lastSeen = (System.currentTimeMillis() - session.lastHeartbeat) / 1000;
                
                String details = String.format("    PID: %d | Realm: %s | Uptime: %s | Last seen: %ds ago",
                    session.pid, realm, uptimeStr, lastSeen);
                
                context.text(this.font, 
                    Component.literal("§7" + details), 
                    cx - 190, y, 0x888888);
                y += 12;
                
                // Alive status
                String statusColor = session.isAlive() ? "§a" : "§c";
                String statusText = session.isAlive() ? "ALIVE" : "STALE";
                context.text(this.font, 
                    Component.literal("    Status: " + statusColor + statusText), 
                    cx - 190, y, 0x888888);
                y += 18;
            }
        }
        
        // Separator
        y += 5;
        context.fill(cx - 200, y, cx + 200, y + 1, 0xFF444444);
        y += 15;
        
        // System info
        context.centeredText(this.font, 
            Component.literal("§b§lSystem Information"), cx, y, 0x55FFFF);
        y += 15;
        
        long currentPid = ProcessHandle.current().pid();
        context.text(this.font, 
            Component.literal("§7Current Process ID: §f" + currentPid), 
            cx - 190, y, 0xAAAAAA);
        y += 12;
        
        String configPath = configManager.getShardId() + ".properties";
        context.text(this.font, 
            Component.literal("§7Config File: §f" + configPath), 
            cx - 190, y, 0xAAAAAA);
        y += 12;
        
        boolean autoLoad = AutoBotMod.getRealmTracker().isEnableAutoLoad();
        boolean resetPerRealm = AutoBotMod.getRealmTracker().isEnableResetPerRealm();
        
        context.text(this.font, 
            Component.literal("§7Auto-Load: " + (autoLoad ? "§aENABLED" : "§cDISABLED")), 
            cx - 190, y, 0xAAAAAA);
        y += 12;
        
        context.text(this.font, 
            Component.literal("§7Reset Per Realm: " + (resetPerRealm ? "§aENABLED" : "§cDISABLED")), 
            cx - 190, y, 0xAAAAAA);
        
        // Footer
        context.centeredText(this.font, 
            Component.literal("§7Auto-refresh every 1 second"), 
            cx, this.height - 60, 0x666666);
    }
    
    private String formatUptime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        
        if (h > 0) return String.format("%dh %dm %ds", h, m, s);
        if (m > 0) return String.format("%dm %ds", m, s);
        return String.format("%ds", s);
    }
    
    @Override
    public void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}