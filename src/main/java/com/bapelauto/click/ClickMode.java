// ============================================
// FILE: ClickMode.java
// Path: src/main/java/com/bapelauto/click/ClickMode.java
// ============================================
package com.bapelauto.click;

public enum ClickMode {
    SINGLE_POINT("Single Point", "Click one target repeatedly"),
    MULTI_POINT("Multi Point", "Click multiple points in sequence"),
    SLOT_SEQUENCE("Slot Sequence", "Click multiple slots in order"),
    MACRO_REPLAY("Macro Replay", "Replay recorded actions with timing");
    
    private final String displayName;
    private final String description;
    
    ClickMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
