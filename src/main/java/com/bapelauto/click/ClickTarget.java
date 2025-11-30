// ============================================
// FILE: ClickTarget.java
// Path: src/main/java/com/bapelauto/click/ClickTarget.java
// ============================================
package com.bapelauto.click;

public class ClickTarget {
    public enum Type { POINT, SLOT }
    
    public final Type type;
    public final double x, y;
    public final int slotId;
    public final long customDelay;
    
    public ClickTarget(double x, double y, long delay) {
        this.type = Type.POINT;
        this.x = x;
        this.y = y;
        this.slotId = -1;
        this.customDelay = delay;
    }
    
    public ClickTarget(int slotId, long delay) {
        this.type = Type.SLOT;
        this.slotId = slotId;
        this.x = 0;
        this.y = 0;
        this.customDelay = delay;
    }
    
    @Override
    public String toString() {
        if (type == Type.SLOT) {
            return String.format("Slot[%d, delay=%dms]", slotId, customDelay);
        }
        return String.format("Point[%.0f,%.0f, delay=%dms]", x, y, customDelay);
    }
}