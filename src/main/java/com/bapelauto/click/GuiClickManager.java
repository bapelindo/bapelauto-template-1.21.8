// ============================================
// FILE: GuiClickManager.java
// Path: src/main/java/com/bapelauto/click/GuiClickManager.java
// ============================================
package com.bapelauto.click;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GuiClickManager {
    private final List<ClickTarget> capturedTargets = new ArrayList<>();
    private final MacroRecorder macroRecorder = new MacroRecorder();
    private final ClickExecutor clickExecutor = new ClickExecutor();
    
    private ClickMode currentMode = ClickMode.SINGLE_POINT;
    private TimingPattern timingPattern = TimingPattern.FIXED;
    
    private boolean isActive = false;
    private int currentTargetIndex = 0;
    private long lastClickTime = 0;
    
    // Timing settings
    private long baseDelay = 100;
    private long minDelay = 50;
    private long maxDelay = 200;
    private int burstCount = 5;
    private long burstPause = 2000;
    private int currentBurstCounter = 0;
    
    public void captureTarget(MinecraftClient client, long defaultDelay) {
        if (client.currentScreen == null) return;
        
        ClickTarget newTarget = null;
        
        // Try to capture slot first
        if (client.currentScreen instanceof HandledScreen) {
            Slot slot = getFocusedSlot((HandledScreen<?>) client.currentScreen);
            if (slot != null) {
                newTarget = new ClickTarget(slot.id, defaultDelay);
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§e[Capture] Slot: " + slot.id), true);
                }
            }
        }
        
        // Fall back to point capture
        if (newTarget == null) {
            double mouseX = client.mouse.getX() * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
            double mouseY = client.mouse.getY() * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
            newTarget = new ClickTarget(mouseX, mouseY, defaultDelay);
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§e[Capture] Point: " + (int)mouseX + ", " + (int)mouseY), true);
            }
        }
        
        // Recording mode
        if (macroRecorder.isRecording()) {
            macroRecorder.recordAction(newTarget);
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§a[Recording] Step " + macroRecorder.getActionCount()), true);
            }
        } else {
            // Normal capture mode
            capturedTargets.add(newTarget);
            updateMode();
            
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§6[Capture] Total: " + capturedTargets.size()), true);
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
            }
        }
    }
    
    private void updateMode() {
        if (capturedTargets.isEmpty()) {
            currentMode = ClickMode.SINGLE_POINT;
        } else if (capturedTargets.size() == 1) {
            currentMode = ClickMode.SINGLE_POINT;
        } else {
            // Detect mode based on target types
            boolean allSlots = capturedTargets.stream().allMatch(t -> t.type == ClickTarget.Type.SLOT);
            currentMode = allSlots ? ClickMode.SLOT_SEQUENCE : ClickMode.MULTI_POINT;
        }
    }
    
    public void startRecording(MinecraftClient client) {
        macroRecorder.startRecording(client);
    }
    
    public void stopRecording(MinecraftClient client) {
        macroRecorder.stopRecording(client);
        
        if (macroRecorder.getActionCount() > 0) {
            capturedTargets.clear();
            capturedTargets.addAll(macroRecorder.getTargetsFromMacro());
            currentMode = ClickMode.MACRO_REPLAY;
            currentTargetIndex = 0;
            isActive = false;
            
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§e[Macro] Ready to replay - Press [=] to start"), true);
            }
        }
    }
    
    public void toggle(MinecraftClient client) {
        if (capturedTargets.isEmpty()) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§c[Click] No targets! Press [-] to capture"), false);
            }
            isActive = false;
            return;
        }
        
        isActive = !isActive;
        
        if (isActive) {
            currentTargetIndex = 0;
            currentBurstCounter = 0;
            lastClickTime = System.currentTimeMillis();
        }
        
        if (client.player != null) {
            String status = isActive ? "§a§lACTIVE" : "§cPAUSED";
            String mode = " [" + currentMode.getDisplayName() + "]";
            String pattern = " {" + timingPattern.getDisplayName() + "}";
            client.player.sendMessage(Text.literal("§6[Click] " + status + mode + pattern), true);
        }
    }
    
    public void tick(MinecraftClient client) {
        if (!isActive || capturedTargets.isEmpty()) return;
        if (client.currentScreen == null) return;
        
        long currentTime = System.currentTimeMillis();
        long effectiveDelay = timingPattern.calculateDelay(
            baseDelay, minDelay, maxDelay, 
            currentTargetIndex, burstCount, burstPause, currentBurstCounter
        );
        
        if ((currentTime - lastClickTime) >= effectiveDelay) {
            ClickTarget target = capturedTargets.get(currentTargetIndex);
            
            if (clickExecutor.executeClick(client, target)) {
                lastClickTime = currentTime;
                
                // Update burst counter
                if (timingPattern == TimingPattern.BURST) {
                    currentBurstCounter++;
                    if (currentBurstCounter >= burstCount) {
                        currentBurstCounter = 0;
                    }
                }
                
                // Advance index
                currentTargetIndex++;
                if (currentTargetIndex >= capturedTargets.size()) {
                    currentTargetIndex = 0; // Loop
                }
            }
        }
    }
    
    public void clearTargets() {
        capturedTargets.clear();
        macroRecorder.clear();
        currentTargetIndex = 0;
        isActive = false;
        currentMode = ClickMode.SINGLE_POINT;
    }
    
    private Slot getFocusedSlot(HandledScreen<?> screen) {
        try {
            for (Field f : HandledScreen.class.getDeclaredFields()) {
                if (Slot.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return (Slot) f.get(screen);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    // Getters and setters
    public boolean isActive() { return isActive; }
    public boolean hasTargets() { return !capturedTargets.isEmpty(); }
    public int getTargetCount() { return capturedTargets.size(); }
    public ClickMode getCurrentMode() { return currentMode; }
    public TimingPattern getTimingPattern() { return timingPattern; }
    public int getTotalClicks() { return clickExecutor.getTotalClicks(); }
    
    public void setTimingPattern(TimingPattern pattern) { this.timingPattern = pattern; }
    public void setBaseDelay(long delay) { this.baseDelay = delay; }
    public void setMinDelay(long delay) { this.minDelay = delay; }
    public void setMaxDelay(long delay) { this.maxDelay = delay; }
    public void setBurstCount(int count) { this.burstCount = count; }
    public void setBurstPause(long pause) { this.burstPause = pause; }
    
    public String getStatusInfo() {
        if (capturedTargets.isEmpty()) return "No targets";
        return String.format("%d targets, %s mode", capturedTargets.size(), currentMode.getDisplayName());
    }
}