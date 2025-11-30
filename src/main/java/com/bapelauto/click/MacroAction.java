// ============================================
// FILE: MacroAction.java
// Path: src/main/java/com/bapelauto/click/MacroAction.java
// ============================================
package com.bapelauto.click;

public class MacroAction {
    public final long timestamp;
    public final ClickTarget target;
    
    public MacroAction(long timestamp, ClickTarget target) {
        this.timestamp = timestamp;
        this.target = target;
    }
    
    public long getRelativeDelay(long previousTimestamp) {
        return timestamp - previousTimestamp;
    }
    
    @Override
    public String toString() {
        return String.format("Action[%dms -> %s]", timestamp, target);
    }
}