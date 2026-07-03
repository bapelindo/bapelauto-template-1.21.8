// ============================================
// FILE: Scheduler.java
// Path: src/main/java/com/bapelauto/scheduler/Scheduler.java
// ============================================
package com.bapelauto.scheduler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Advanced scheduler for time-based automation
 */
public class Scheduler {
    
    private final List<ScheduledTask> tasks = new ArrayList<>();
    private boolean enabled = true;
    
    public enum TaskType {
        ONE_TIME("One Time", "Execute once"),
        REPEATING("Repeating", "Execute repeatedly"),
        DAILY("Daily", "Execute at specific time each day"),
        INTERVAL("Interval", "Execute every X minutes");
        
        private final String displayName;
        private final String description;
        
        TaskType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public enum ScheduledActionType {
        SEND_COMMAND("Send Command"),
        TOGGLE_FEATURE("Toggle Feature"),
        LOAD_PROFILE("Load Profile"),
        RESTART_BOT("Restart Bot"),
        STOP_BOT("Stop Bot"),
        PLAY_SOUND("Play Sound"),
        SEND_MESSAGE("Send Message");
        
        private final String displayName;
        
        ScheduledActionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    /**
     * Add a scheduled task
     */
    public void addTask(ScheduledTask task) {
        tasks.add(task);
        System.out.println("[Scheduler] Added task: " + task.getName());
    }
    
    /**
     * Remove a scheduled task
     */
    public void removeTask(ScheduledTask task) {
        tasks.remove(task);
    }
    
    /**
     * Tick - check and execute scheduled tasks
     */
    public void tick(MinecraftClient client) {
        if (!enabled) return;
        
        long currentTime = System.currentTimeMillis();
        
        for (ScheduledTask task : new ArrayList<>(tasks)) {
            if (task.shouldExecute(currentTime)) {
                task.execute(client);
                
                // Remove one-time tasks after execution
                if (task.type == TaskType.ONE_TIME) {
                    tasks.remove(task);
                }
            }
        }
    }
    
    /**
     * Get all scheduled tasks
     */
    public List<ScheduledTask> getTasks() {
        return new ArrayList<>(tasks);
    }
    
    /**
     * Clear all tasks
     */
    public void clearAll() {
        tasks.clear();
    }
    
    /**
     * Get active task count
     */
    public int getActiveTaskCount() {
        return (int) tasks.stream().filter(t -> t.enabled).count();
    }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    /**
     * Scheduled Task class
     */
    public static class ScheduledTask {
        private final String name;
        private final TaskType type;
        private final ScheduledActionType actionType;
        private final String actionData;
        
        private long nextExecutionTime;
        private long interval; // For repeating/interval tasks
        private LocalTime dailyTime; // For daily tasks
        private boolean enabled = true;
        private int executionCount = 0;
        private int maxExecutions = -1; // -1 = unlimited
        
        /**
         * Create a one-time task
         */
        public static ScheduledTask oneTime(String name, long delayMs, ScheduledActionType action, String actionData) {
            ScheduledTask task = new ScheduledTask(name, TaskType.ONE_TIME, action, actionData);
            task.nextExecutionTime = System.currentTimeMillis() + delayMs;
            return task;
        }
        
        /**
         * Create a repeating task
         */
        public static ScheduledTask repeating(String name, long intervalMs, ScheduledActionType action, String actionData) {
            ScheduledTask task = new ScheduledTask(name, TaskType.REPEATING, action, actionData);
            task.interval = intervalMs;
            task.nextExecutionTime = System.currentTimeMillis() + intervalMs;
            return task;
        }
        
        /**
         * Create an interval task (every X minutes)
         */
        public static ScheduledTask interval(String name, long minutes, ScheduledActionType action, String actionData) {
            return repeating(name, TimeUnit.MINUTES.toMillis(minutes), action, actionData);
        }
        
        /**
         * Create a daily task
         */
        public static ScheduledTask daily(String name, int hour, int minute, ScheduledActionType action, String actionData) {
            ScheduledTask task = new ScheduledTask(name, TaskType.DAILY, action, actionData);
            task.dailyTime = LocalTime.of(hour, minute);
            task.updateNextDailyExecution();
            return task;
        }
        
        private ScheduledTask(String name, TaskType type, ScheduledActionType actionType, String actionData) {
            this.name = name;
            this.type = type;
            this.actionType = actionType;
            this.actionData = actionData;
        }
        
        public boolean shouldExecute(long currentTime) {
            if (!enabled) return false;
            
            // Check max executions
            if (maxExecutions > 0 && executionCount >= maxExecutions) {
                enabled = false;
                return false;
            }
            
            return currentTime >= nextExecutionTime;
        }
        
        public void execute(MinecraftClient client) {
            if (client.player == null) return;
            
            executionCount++;
            
            // Execute action
            switch (actionType) {
                case SEND_COMMAND:
                    if (actionData != null && !actionData.isEmpty()) {
                        if (actionData.startsWith("/")) {
                            client.player.networkHandler.sendChatCommand(actionData.substring(1));
                        } else {
                            client.player.networkHandler.sendChatMessage(actionData);
                        }
                    }
                    break;
                    
                case SEND_MESSAGE:
                    if (actionData != null) {
                        client.player.sendMessage(Text.literal("§e[Scheduler] " + actionData), false);
                    }
                    break;
                    
                case PLAY_SOUND:
                    client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 1.5F);
                    break;
                    
                default:
                    break;
            }
            
            // Update next execution time
            if (type == TaskType.REPEATING || type == TaskType.INTERVAL) {
                nextExecutionTime = System.currentTimeMillis() + interval;
            } else if (type == TaskType.DAILY) {
                updateNextDailyExecution();
            }
            
            System.out.println("[Scheduler] Executed task: " + name + " (count: " + executionCount + ")");
        }
        
        private void updateNextDailyExecution() {
            LocalTime now = LocalTime.now();
            LocalTime target = dailyTime;
            
            // If target time has passed today, schedule for tomorrow
            if (now.isAfter(target)) {
                nextExecutionTime = System.currentTimeMillis() + 
                    TimeUnit.HOURS.toMillis(24) - 
                    TimeUnit.HOURS.toMillis(now.getHour() - target.getHour()) -
                    TimeUnit.MINUTES.toMillis(now.getMinute() - target.getMinute());
            } else {
                nextExecutionTime = System.currentTimeMillis() + 
                    TimeUnit.HOURS.toMillis(target.getHour() - now.getHour()) +
                    TimeUnit.MINUTES.toMillis(target.getMinute() - now.getMinute());
            }
        }
        
        public void setMaxExecutions(int max) {
            this.maxExecutions = max;
        }
        
        public String getName() { return name; }
        public TaskType getType() { return type; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getExecutionCount() { return executionCount; }
        
        public String getNextExecutionTime() {
            long msUntil = nextExecutionTime - System.currentTimeMillis();
            if (msUntil < 0) return "Ready";
            
            long seconds = TimeUnit.MILLISECONDS.toSeconds(msUntil);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(msUntil);
            long hours = TimeUnit.MILLISECONDS.toHours(msUntil);
            
            if (hours > 0) return String.format("%dh %dm", hours, minutes % 60);
            if (minutes > 0) return String.format("%dm %ds", minutes, seconds % 60);
            return String.format("%ds", seconds);
        }
        
        @Override
        public String toString() {
            return String.format("%s [%s] - Next: %s", 
                name, type.getDisplayName(), getNextExecutionTime());
        }
    }
}