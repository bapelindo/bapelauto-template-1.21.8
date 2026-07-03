// ============================================
// FILE: StatsTracker.java
// Path: src/main/java/com/bapelauto/stats/StatsTracker.java
// ============================================
package com.bapelauto.stats;

public class StatsTracker {
    private long sessionStartTime = 0;
    private int totalCommands = 0;
    
    public void startSession() {
        sessionStartTime = System.currentTimeMillis();
    }
    
    public void incrementCommands() {
        totalCommands++;
    }
    
    public void reset() {
        totalCommands = 0;
        sessionStartTime = System.currentTimeMillis();
    }
    
    public int getTotalCommands() {
        return totalCommands;
    }
    
    public long getSessionDuration() {
        if (sessionStartTime == 0) return 0;
        return (System.currentTimeMillis() - sessionStartTime) / 1000;
    }
    
    public String getFormattedDuration() {
        long seconds = getSessionDuration();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}