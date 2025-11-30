// ============================================
// FILE: TimingPattern.java
// Path: src/main/java/com/bapelauto/click/TimingPattern.java
// ============================================
package com.bapelauto.click;

public enum TimingPattern {
    FIXED("Fixed Delay", "Same delay between clicks"),
    RANDOMIZED("Randomized", "Random delay in range"),
    INCREASING("Increasing", "Delay gets longer over time"),
    DECREASING("Decreasing", "Delay gets shorter over time"),
    BURST("Burst Mode", "Fast clicks then pause");
    
    private final String displayName;
    private final String description;
    
    TimingPattern(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public long calculateDelay(long baseDelay, long minDelay, long maxDelay, 
                               int currentIndex, int burstCount, long burstPause, int burstCounter) {
        switch (this) {
            case RANDOMIZED:
                return minDelay + (long)(Math.random() * (maxDelay - minDelay));
                
            case INCREASING:
                long increase = (currentIndex * 50);
                return Math.min(baseDelay + increase, maxDelay);
                
            case DECREASING:
                long decrease = (currentIndex * 50);
                return Math.max(baseDelay - decrease, minDelay);
                
            case BURST:
                return (burstCounter < burstCount) ? minDelay : burstPause;
                
            case FIXED:
            default:
                return baseDelay;
        }
    }
}
