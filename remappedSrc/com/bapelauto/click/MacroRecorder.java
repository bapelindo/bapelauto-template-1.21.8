// ============================================
// FILE: MacroRecorder.java
// Path: src/main/java/com/bapelauto/click/MacroRecorder.java
// ============================================
package com.bapelauto.click;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public class MacroRecorder {
    private boolean isRecording = false;
    private long recordingStartTime = 0;
    private final List<MacroAction> recordedActions = new ArrayList<>();
    
    public void startRecording(MinecraftClient client) {
        if (isRecording) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§c[Recording] Already recording!"), false);
            }
            return;
        }
        
        isRecording = true;
        recordingStartTime = System.currentTimeMillis();
        recordedActions.clear();
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§a§l[Recording] STARTED - Capture points with [-], stop with []]"), true);
            client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 2.0F);
        }
    }
    
    public void stopRecording(MinecraftClient client) {
        if (!isRecording) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§c[Recording] Not recording!"), false);
            }
            return;
        }
        
        isRecording = false;
        
        if (recordedActions.isEmpty()) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§c[Recording] No actions recorded!"), false);
            }
            return;
        }
        
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§a§l[Recording] STOPPED - " + recordedActions.size() + " actions saved"), true);
            client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 1.5F);
        }
    }
    
    public void recordAction(ClickTarget target) {
        if (!isRecording) return;
        
        long relativeTime = System.currentTimeMillis() - recordingStartTime;
        recordedActions.add(new MacroAction(relativeTime, target));
    }
    
    public List<MacroAction> getRecordedActions() {
        return new ArrayList<>(recordedActions);
    }
    
    public List<ClickTarget> getTargetsFromMacro() {
        List<ClickTarget> targets = new ArrayList<>();
        for (MacroAction action : recordedActions) {
            targets.add(action.target);
        }
        return targets;
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public int getActionCount() {
        return recordedActions.size();
    }
    
    public void clear() {
        recordedActions.clear();
        isRecording = false;
        recordingStartTime = 0;
    }
}