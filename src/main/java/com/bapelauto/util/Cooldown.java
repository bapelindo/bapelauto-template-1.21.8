// ============================================
// FILE: Cooldown.java
// Path: src/main/java/com/bapelauto/util/Cooldown.java
//
// Small shared helper for the "has enough time passed since I last did X"
// pattern that was hand-rolled with a raw `long lastXTime` field and
// System.currentTimeMillis() arithmetic in well over a dozen places across
// the manager classes (WorldInteractionManager, InventoryManager,
// HotkeyManager, SlimefunAutoManager, GuiClickManager, ...). Not a behavior
// change - just naming the concept once instead of re-deriving it per file.
// ============================================
package com.bapelauto.util;

public final class Cooldown {
    private long lastTriggered = 0;

    /** True if at least delayMs has passed since the last trigger(). */
    public boolean isReady(long delayMs) {
        return System.currentTimeMillis() - lastTriggered >= delayMs;
    }

    /** Marks the cooldown as triggered right now. */
    public void trigger() {
        lastTriggered = System.currentTimeMillis();
    }

    /**
     * If ready, marks it triggered and returns true; otherwise leaves the
     * cooldown untouched and returns false. Use this when the "trigger"
     * should always happen as soon as the delay elapses. Use isReady()/
     * trigger() separately when the trigger should only happen after the
     * guarded action actually succeeds.
     */
    public boolean tryConsume(long delayMs) {
        if (isReady(delayMs)) {
            trigger();
            return true;
        }
        return false;
    }
}
