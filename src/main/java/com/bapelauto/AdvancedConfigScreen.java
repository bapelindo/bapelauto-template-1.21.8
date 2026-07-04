// ============================================
// FILE: AdvancedConfigScreen.java
// Path: src/main/java/com/bapelauto/AdvancedConfigScreen.java
//
// Ported to Minecraft 26.1.2 / Fabric (official Mojang mappings).
// See AutoBotConfigScreen.java header comment for the full list of API
// changes applied consistently across this project's Screen classes.
// ============================================
package com.bapelauto;

import com.bapelauto.profile.ProfileManager;
import com.bapelauto.scheduler.Scheduler;
import com.bapelauto.hotkey.HotkeyManager;
import com.bapelauto.visual.VisualOverlay;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

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
    private EditBox profileNameField;
    private EditBox profileDescField;
    private int selectedProfileIndex = -1;

    // Scheduler tab
    private int selectedTaskIndex = -1;

    public AdvancedConfigScreen(Screen parent) {
        super(Component.literal("Advanced Settings"));
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

            Button tabBtn = Button.builder(
                Component.literal(tab.name + (tab == currentTab ? " §l»" : "")),
                b -> switchTab(tabFinal)
            ).bounds(tabStartX + (tabWidth + tabSpacing) * i, tabY, tabWidth, 20).build();

            this.addRenderableWidget(tabBtn);
        }

        // Initialize current tab content
        initTabContent();

        // Back button at bottom
        this.addRenderableWidget(Button.builder(
            Component.literal("§c« Back"),
            b -> this.onClose()
        ).bounds(cx - 100, this.height - 30, 90, 20).build());

        // Main config button
        this.addRenderableWidget(Button.builder(
            Component.literal("§eMain Config"),
            b -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new AutoBotConfigScreen(this.parent));
                }
            }
        ).bounds(cx + 10, this.height - 30, 90, 20).build());
    }

    private void switchTab(Tab newTab) {
        currentTab = newTab;
        this.clearWidgets();
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
        this.addRenderableWidget(Button.builder(
            Component.literal("§aCurrent: §f" + currentProfile),
            b -> {}
        ).bounds(cx - 150, startY, 300, 20).build());

        startY += 30;

        // Profile name input
        profileNameField = new EditBox(
            this.font, cx - 150, startY, 140, 20, Component.literal("Profile Name")
        );
        profileNameField.setHint(Component.literal("Profile name..."));
        this.addRenderableWidget(profileNameField);

        // Profile description input
        profileDescField = new EditBox(
            this.font, cx, startY, 150, 20, Component.literal("Description")
        );
        profileDescField.setHint(Component.literal("Description..."));
        this.addRenderableWidget(profileDescField);

        startY += 30;

        // Save new profile button
        this.addRenderableWidget(Button.builder(
            Component.literal("§a💾 Save Current Config"),
            b -> {
                String name = profileNameField.getValue();
                String desc = profileDescField.getValue();
                if (name != null && !name.trim().isEmpty()) {
                    profileManager.saveProfile(name, desc);
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.sendSystemMessage(
                            Component.literal("§a[Profile] Saved: " + name), true
                        );
                    }
                    this.clearWidgets();
                    this.init();
                }
            }
        ).bounds(cx - 150, startY, 300, 20).build());

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
                this.addRenderableWidget(Button.builder(
                    Component.literal(btnText),
                    b -> {
                        selectedProfileIndex = index;
                        this.clearWidgets();
                        this.init();
                    }
                ).bounds(cx - 150, listY, 200, 18).build());

                // Load button
                this.addRenderableWidget(Button.builder(
                    Component.literal("§aLoad"),
                    b -> {
                        profileManager.loadProfile(profile.name, this.minecraft);
                        this.clearWidgets();
                        this.init();
                    }
                ).bounds(cx + 55, listY, 45, 18).build());

                // Delete button
                this.addRenderableWidget(Button.builder(
                    Component.literal("§cX"),
                    b -> {
                        profileManager.deleteProfile(profile.name, this.minecraft);
                        selectedProfileIndex = -1;
                        this.clearWidgets();
                        this.init();
                    }
                ).bounds(cx + 105, listY, 45, 18).build());

                listY += 20;
            }
        }
    }

    private void initSchedulerTab() {
        int cx = this.width / 2;
        int startY = 40;

        // Scheduler toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("Scheduler: " + (scheduler.isEnabled() ? "§aON" : "§cOFF")),
            b -> {
                scheduler.setEnabled(!scheduler.isEnabled());
                b.setMessage(Component.literal("Scheduler: " + (scheduler.isEnabled() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx - 150, startY, 300, 20).build());

        startY += 30;

        // Active tasks count (kept for parity with original; not rendered here)
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
                this.addRenderableWidget(Button.builder(
                    Component.literal(btnText),
                    b -> {
                        selectedTaskIndex = index;
                        this.clearWidgets();
                        this.init();
                    }
                ).bounds(cx - 150, listY, 200, 18).build());

                // Toggle button
                this.addRenderableWidget(Button.builder(
                    Component.literal(task.isEnabled() ? "§cDisable" : "§aEnable"),
                    b -> {
                        task.setEnabled(!task.isEnabled());
                        this.clearWidgets();
                        this.init();
                    }
                ).bounds(cx + 55, listY, 50, 18).build());

                // Remove button
                this.addRenderableWidget(Button.builder(
                    Component.literal("§cX"),
                    b -> {
                        scheduler.removeTask(task);
                        selectedTaskIndex = -1;
                        this.clearWidgets();
                        this.init();
                    }
                ).bounds(cx + 110, listY, 40, 18).build());

                listY += 20;
            }
        }

        // Add task button (simplified)
        this.addRenderableWidget(Button.builder(
            Component.literal("§a+ Add Task (See Guide)"),
            b -> {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendSystemMessage(
                        Component.literal("§e[Scheduler] Use code to add tasks - see Quick Start Guide"), false
                    );
                }
            }
        ).bounds(cx - 150, this.height - 60, 300, 20).build());
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
                this.addRenderableWidget(Button.builder(
                    Component.literal("§7" + desc),
                    b -> {}
                ).bounds(cx - 150, listY, 250, 18).build());

                // Remove button
                final int index = i;
                this.addRenderableWidget(Button.builder(
                    Component.literal("§cX"),
                    b -> {
                        conditionals.remove(index);
                        this.clearWidgets();
                        this.init();
                    }
                ).bounds(cx + 105, listY, 45, 18).build());

                listY += 20;
            }
        }

        // Add conditional button (simplified)
        this.addRenderableWidget(Button.builder(
            Component.literal("§a+ Add Conditional (See Guide)"),
            b -> {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendSystemMessage(
                        Component.literal("§e[Conditional] Use code to add - see Quick Start Guide"), false
                    );
                }
            }
        ).bounds(cx - 150, this.height - 60, 300, 20).build());
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
                this.addRenderableWidget(Button.builder(
                    Component.literal(text),
                    b -> {}
                ).bounds(cx - 150, listY, 250, 18).build());

                // Remove button
                final String id = hotkey.getId();
                this.addRenderableWidget(Button.builder(
                    Component.literal("§cX"),
                    b -> {
                        hotkeyManager.unregisterHotkey(id);
                        this.clearWidgets();
                        this.init();
                    }
                ).bounds(cx + 105, listY, 45, 18).build());

                listY += 20;
            }
        }

        // Add hotkey button (simplified)
        this.addRenderableWidget(Button.builder(
            Component.literal("§a+ Add Hotkey (See Guide)"),
            b -> {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendSystemMessage(
                        Component.literal("§e[Hotkey] Use code to add - see Quick Start Guide"), false
                    );
                }
            }
        ).bounds(cx - 150, this.height - 60, 300, 20).build());
    }

    private void initVisualTab() {
        int cx = this.width / 2;
        int startY = 40;

        // Overlay toggle
        this.addRenderableWidget(Button.builder(
            Component.literal("Overlay: " + (visualOverlay.isEnabled() ? "§aON" : "§cOFF")),
            b -> {
                visualOverlay.setEnabled(!visualOverlay.isEnabled());
                b.setMessage(Component.literal("Overlay: " + (visualOverlay.isEnabled() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx - 150, startY, 300, 20).build());

        startY += 25;

        // Show targets
        this.addRenderableWidget(Button.builder(
            Component.literal("Show Targets: " + (visualOverlay.isShowTargets() ? "§aON" : "§cOFF")),
            b -> {
                visualOverlay.setShowTargets(!visualOverlay.isShowTargets());
                b.setMessage(Component.literal("Show Targets: " + (visualOverlay.isShowTargets() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx - 150, startY, 300, 20).build());

        startY += 25;

        // Show stats
        this.addRenderableWidget(Button.builder(
            Component.literal("Show Stats: " + (visualOverlay.isShowStats() ? "§aON" : "§cOFF")),
            b -> {
                visualOverlay.setShowStats(!visualOverlay.isShowStats());
                b.setMessage(Component.literal("Show Stats: " + (visualOverlay.isShowStats() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx - 150, startY, 300, 20).build());

        startY += 25;

        // Show click indicators
        this.addRenderableWidget(Button.builder(
            Component.literal("Click Indicators: " + (visualOverlay.isShowClickIndicator() ? "§aON" : "§cOFF")),
            b -> {
                visualOverlay.setShowClickIndicator(!visualOverlay.isShowClickIndicator());
                b.setMessage(Component.literal("Click Indicators: " + (visualOverlay.isShowClickIndicator() ? "§aON" : "§cOFF")));
            }
        ).bounds(cx - 150, startY, 300, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // Background
        context.fillGradient(0, 0, this.width, this.height, 0xEE000000, 0xEE101010);
        super.extractRenderState(context, mouseX, mouseY, delta);

        // Title (centered)
        Component title = Component.literal("§6§lAdvanced Settings");
        context.text(this.font, title, this.width / 2 - this.font.width(title) / 2, 15, 0xFFFFFFFF, true);

        // Tab description (centered)
        Component desc = Component.literal("§7" + currentTab.description);
        context.text(this.font, desc, this.width / 2 - this.font.width(desc) / 2, 30, 0xFFAAAAAA, true);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) { // ESC
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}