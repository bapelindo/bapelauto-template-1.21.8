// ============================================
// FILE: AdvancedConfigScreen.java
// Path: src/main/java/com/bapelauto/AdvancedConfigScreen.java
// ============================================
package com.bapelauto;

import com.bapelauto.profile.ProfileManager;
import com.bapelauto.scheduler.Scheduler;
import com.bapelauto.hotkey.HotkeyManager;
import com.bapelauto.visual.VisualOverlay;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Advanced configuration screen with tabs for all new features
 */
public class AdvancedConfigScreen extends Screen {
    private final Screen parent;
    
    private enum Tab {
        PROFILES("§6Profiles", "Profile management"),
        SCHEDULER("§bScheduler", "Time-based tasks"),
        CONDITIONALS("§eConditionals", "Condition-based actions"),
        HOTKEYS("§aHotkeys", "Custom key bindings"),
        VISUAL("§dVisual", "Overlay settings");
        
        final String name;
        final String description;
        
        Tab(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
    
    private Tab currentTab = Tab.PROFILES;
    private final ProfileManager profileManager;
    private final Scheduler scheduler;
    private final HotkeyManager hotkeyManager;
    private final VisualOverlay visualOverlay;
    
    // Profile tab
    private TextFieldWidget profileNameField;
    private TextFieldWidget profileDescField;
    private int selectedProfileIndex = -1;
    
    // Scheduler tab
    private int selectedTaskIndex = -1;
    
    public AdvancedConfigScreen(Screen parent) {
        super(Text.literal("Advanced Settings"));
        this.parent = parent;
        
        this.profileManager = AutoBotMod.getProfileManager();
        this.scheduler = AutoBotMod.getScheduler();
        this.hotkeyManager = AutoBotMod.getHotkeyManager();
        this.visualOverlay = AutoBotMod.getVisualOverlay();
    }
    
    @Override
    protected void init() {
        int cx = this.width / 2;
        
        // Tab buttons at top
        int tabY = 5;
        int tabWidth = 80;
        int tabSpacing = 5;
        int totalTabWidth = (tabWidth + tabSpacing) * Tab.values().length - tabSpacing;
        int tabStartX = cx - (totalTabWidth / 2);
        
        for (int i = 0; i < Tab.values().length; i++) {
            Tab tab = Tab.values()[i];
            final Tab tabFinal = tab;
            
            ButtonWidget tabBtn = ButtonWidget.builder(
                Text.literal(tab.name + (tab == currentTab ? " §l»" : "")),
                b -> switchTab(tabFinal)
            ).dimensions(tabStartX + (tabWidth + tabSpacing) * i, tabY, tabWidth, 20).build();
            
            this.addDrawableChild(tabBtn);
        }
        
        // Initialize current tab content
        initTabContent();
        
        // Back button at bottom
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§c« Back"),
            b -> this.close()
        ).dimensions(cx - 100, this.height - 30, 90, 20).build());
        
        // Main config button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§eMain Config"),
            b -> {
                if (this.client != null) {
                    this.client.setScreen(new AutoBotConfigScreen(this.parent));
                }
            }
        ).dimensions(cx + 10, this.height - 30, 90, 20).build());
    }
    
    private void switchTab(Tab newTab) {
        currentTab = newTab;
        this.clearChildren();
        this.init();
    }
    
    private void initTabContent() {
        switch (currentTab) {
            case PROFILES:
                initProfilesTab();
                break;
            case SCHEDULER:
                initSchedulerTab();
                break;
            case CONDITIONALS:
                initConditionalsTab();
                break;
            case HOTKEYS:
                initHotkeysTab();
                break;
            case VISUAL:
                initVisualTab();
                break;
        }
    }
    
    private void initProfilesTab() {
        int cx = this.width / 2;
        int startY = 40;
        
        // Current profile display
        String currentProfile = profileManager.getCurrentProfile();
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§aCurrent: §f" + currentProfile),
            b -> {}
        ).dimensions(cx - 150, startY, 300, 20).build()); // FIXED
        
        startY += 30;
        
        // Profile name input
        profileNameField = new TextFieldWidget(
            this.textRenderer, cx - 150, startY, 140, 20, Text.literal("Profile Name")
        );
        profileNameField.setPlaceholder(Text.literal("Profile name..."));
        this.addDrawableChild(profileNameField);
        
        // Profile description input
        profileDescField = new TextFieldWidget(
            this.textRenderer, cx, startY, 150, 20, Text.literal("Description")
        );
        profileDescField.setPlaceholder(Text.literal("Description..."));
        this.addDrawableChild(profileDescField);
        
        startY += 30;
        
        // Save new profile button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a💾 Save Current Config"),
            b -> {
                String name = profileNameField.getText();
                String desc = profileDescField.getText();
                if (name != null && !name.trim().isEmpty()) {
                    profileManager.saveProfile(name, desc);
                    if (this.client != null && this.client.player != null) {
                        this.client.player.sendMessage(
                            Text.literal("§a[Profile] Saved: " + name), true
                        );
                    }
                    this.clearChildren();
                    this.init();
                }
            }
        ).dimensions(cx - 150, startY, 300, 20).build()); // FIXED
        
        startY += 30;
        
        // Profile list
        List<ProfileManager.Profile> profiles = profileManager.getAvailableProfiles();
        
        if (profiles.isEmpty()) {
            // No profiles message
        } else {
            int listY = startY;
            for (int i = 0; i < Math.min(profiles.size(), 6); i++) {
                final int index = i;
                ProfileManager.Profile profile = profiles.get(i);
                
                boolean isSelected = (i == selectedProfileIndex);
                boolean isCurrent = profile.name.equals(currentProfile);
                
                String btnText = (isCurrent ? "§a▶ " : "§7  ") + profile.name;
                if (isSelected) btnText = "§e" + btnText;
                
                // Profile button
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(btnText),
                    b -> {
                        selectedProfileIndex = index;
                        this.clearChildren();
                        this.init();
                    }
                ).dimensions(cx - 150, listY, 200, 18).build()); // FIXED
                
                // Load button
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§aLoad"),
                    b -> {
                        profileManager.loadProfile(profile.name, this.client);
                        this.clearChildren();
                        this.init();
                    }
                ).dimensions(cx + 55, listY, 45, 18).build()); // FIXED
                
                // Delete button
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§cX"),
                    b -> {
                        profileManager.deleteProfile(profile.name, this.client);
                        selectedProfileIndex = -1;
                        this.clearChildren();
                        this.init();
                    }
                ).dimensions(cx + 105, listY, 45, 18).build()); // FIXED
                
                listY += 20;
            }
        }
    }
    
    private void initSchedulerTab() {
        int cx = this.width / 2;
        int startY = 40;
        
        // Scheduler toggle
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Scheduler: " + (scheduler.isEnabled() ? "§aON" : "§cOFF")),
            b -> {
                scheduler.setEnabled(!scheduler.isEnabled());
                b.setMessage(Text.literal("Scheduler: " + (scheduler.isEnabled() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 300, 20).build()); // FIXED
        
        startY += 30;
        
        // Active tasks count
        int activeCount = scheduler.getActiveTaskCount();
        int totalCount = scheduler.getTasks().size();
        
        startY += 30;
        
        // Task list
        List<Scheduler.ScheduledTask> tasks = scheduler.getTasks();
        
        if (tasks.isEmpty()) {
            // No tasks message
        } else {
            int listY = startY;
            for (int i = 0; i < Math.min(tasks.size(), 8); i++) {
                final int index = i;
                Scheduler.ScheduledTask task = tasks.get(i);
                
                boolean isSelected = (i == selectedTaskIndex);
                String btnText = (task.isEnabled() ? "§a✓ " : "§7✗ ") + task.getName();
                if (isSelected) btnText = "§e" + btnText;
                
                // Task button
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(btnText),
                    b -> {
                        selectedTaskIndex = index;
                        this.clearChildren();
                        this.init();
                    }
                ).dimensions(cx - 150, listY, 200, 18).build()); // FIXED
                
                // Next execution time
                String nextExec = task.getNextExecutionTime();
                
                // Toggle button
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(task.isEnabled() ? "§cDisable" : "§aEnable"),
                    b -> {
                        task.setEnabled(!task.isEnabled());
                        this.clearChildren();
                        this.init();
                    }
                ).dimensions(cx + 55, listY, 50, 18).build()); // FIXED
                
                // Remove button
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§cX"),
                    b -> {
                        scheduler.removeTask(task);
                        selectedTaskIndex = -1;
                        this.clearChildren();
                        this.init();
                    }
                ).dimensions(cx + 110, listY, 40, 18).build()); // FIXED
                
                listY += 20;
            }
        }
        
        // Add task button (simplified)
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a+ Add Task (See Guide)"),
            b -> {
                if (this.client != null && this.client.player != null) {
                    this.client.player.sendMessage(
                        Text.literal("§e[Scheduler] Use code to add tasks - see Quick Start Guide"), false
                    );
                }
            }
        ).dimensions(cx - 150, this.height - 60, 300, 20).build()); // FIXED
    }
    
    private void initConditionalsTab() {
        int cx = this.width / 2;
        int startY = 40;
        
        var conditionals = AutoBotMod.getConditionalActions();
        
        if (conditionals.isEmpty()) {
            // Info message
        } else {
            int listY = startY;
            for (int i = 0; i < Math.min(conditionals.size(), 10); i++) {
                var conditional = conditionals.get(i);
                
                String desc = conditional.getDescription();
                
                // Conditional display
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§7" + desc),
                    b -> {}
                ).dimensions(cx - 150, listY, 250, 18).build()); // FIXED
                
                // Remove button
                final int index = i;
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§cX"),
                    b -> {
                        conditionals.remove(index);
                        this.clearChildren();
                        this.init();
                    }
                ).dimensions(cx + 105, listY, 45, 18).build()); // FIXED
                
                listY += 20;
            }
        }
        
        // Add conditional button (simplified)
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a+ Add Conditional (See Guide)"),
            b -> {
                if (this.client != null && this.client.player != null) {
                    this.client.player.sendMessage(
                        Text.literal("§e[Conditional] Use code to add - see Quick Start Guide"), false
                    );
                }
            }
        ).dimensions(cx - 150, this.height - 60, 300, 20).build()); // FIXED
    }
    
    private void initHotkeysTab() {
        int cx = this.width / 2;
        int startY = 40;
        
        List<HotkeyManager.CustomHotkey> hotkeys = hotkeyManager.getAllHotkeys();
        
        if (hotkeys.isEmpty()) {
            // Info message
        } else {
            int listY = startY;
            for (int i = 0; i < Math.min(hotkeys.size(), 10); i++) {
                HotkeyManager.CustomHotkey hotkey = hotkeys.get(i);
                
                String text = "§7[" + hotkey.getKeyName() + "] §f" + 
                             hotkey.getAction().getDisplayName();
                
                // Hotkey display
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(text),
                    b -> {}
                ).dimensions(cx - 150, listY, 250, 18).build()); // FIXED
                
                // Remove button
                final String id = hotkey.getId();
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("§cX"),
                    b -> {
                        hotkeyManager.unregisterHotkey(id);
                        this.clearChildren();
                        this.init();
                    }
                ).dimensions(cx + 105, listY, 45, 18).build()); // FIXED
                
                listY += 20;
            }
        }
        
        // Add hotkey button (simplified)
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("§a+ Add Hotkey (See Guide)"),
            b -> {
                if (this.client != null && this.client.player != null) {
                    this.client.player.sendMessage(
                        Text.literal("§e[Hotkey] Use code to add - see Quick Start Guide"), false
                    );
                }
            }
        ).dimensions(cx - 150, this.height - 60, 300, 20).build()); // FIXED
    }
    
    private void initVisualTab() {
        int cx = this.width / 2;
        int startY = 40;
        
        // Overlay toggle
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Overlay: " + (visualOverlay.isEnabled() ? "§aON" : "§cOFF")),
            b -> {
                visualOverlay.setEnabled(!visualOverlay.isEnabled());
                b.setMessage(Text.literal("Overlay: " + (visualOverlay.isEnabled() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 300, 20).build()); // FIXED
        
        startY += 25;
        
        // Show targets
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Show Targets: " + (visualOverlay.isShowTargets() ? "§aON" : "§cOFF")),
            b -> {
                visualOverlay.setShowTargets(!visualOverlay.isShowTargets());
                b.setMessage(Text.literal("Show Targets: " + (visualOverlay.isShowTargets() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 300, 20).build()); // FIXED
        
        startY += 25;
        
        // Show stats
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Show Stats: " + (visualOverlay.isShowStats() ? "§aON" : "§cOFF")),
            b -> {
                visualOverlay.setShowStats(!visualOverlay.isShowStats());
                b.setMessage(Text.literal("Show Stats: " + (visualOverlay.isShowStats() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 300, 20).build()); // FIXED
        
        startY += 25;
        
        // Show click indicators
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Click Indicators: " + (visualOverlay.isShowClickIndicator() ? "§aON" : "§cOFF")),
            b -> {
                visualOverlay.setShowClickIndicator(!visualOverlay.isShowClickIndicator());
                b.setMessage(Text.literal("Click Indicators: " + (visualOverlay.isShowClickIndicator() ? "§aON" : "§cOFF")));
            }
        ).dimensions(cx - 150, startY, 300, 20).build()); // FIXED
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Background
        context.fillGradient(0, 0, this.width, this.height, 0xEE000000, 0xEE101010);
        super.render(context, mouseX, mouseY, delta);
        
        // Title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§6§lAdvanced Settings"),
            this.width / 2, 15, 0xFFFFFF
        );
        
        // Tab description
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("§7" + currentTab.description),
            this.width / 2, 30, 0xAAAAAA
        );
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
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