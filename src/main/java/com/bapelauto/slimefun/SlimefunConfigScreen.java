// ============================================
// FILE: SlimefunConfigScreen.java (MODERN CLEAN UI)
// Path: src/main/java/com/bapelauto/slimefun/SlimefunConfigScreen.java
// ============================================
package com.bapelauto.slimefun;

import com.bapelauto.AutoBotMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Modern, Clean Slimefun Configuration UI with Tab System
 */
public class SlimefunConfigScreen extends Screen {
    private final Screen parent;
    private final SlimefunAutoManager slimefunManager;
    
    // Tab system
    private enum Tab {
        MAIN("§6⚡ Main", "Main Slimefun controls"),
        AUTOMATION("§a🤖 Automation", "Auto-Input & Recipe systems"),
        RECIPES("§e📖 Recipes", "Browse and manage recipes"),
        PRESETS("§b⚙ Presets", "Quick configuration presets"),
        STATS("§d📊 Stats", "Statistics and monitoring");
        
        final String name;
        final String description;
        
        Tab(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
    
    private Tab currentTab = Tab.MAIN;
    
    // Recipe browsing
    private int recipeScrollOffset = 0;
    private static final int RECIPES_PER_PAGE = 8;
    private String recipeSearchQuery = "";
    private String selectedCategory = "all";
    
    // Component fields
    private EditBox recipeNameField;
    private EditBox recipeSearchField;
    
    public SlimefunConfigScreen(Screen parent) {
        super(Component.literal("Slimefun Automation"));
        this.parent = parent;
        this.slimefunManager = AutoBotMod.getSlimefunManager();
    }
    
    @Override
    protected void init() {
        int cx = this.width / 2;
        int tabY = 30;
        
        // ==========================================
        // TAB BUTTONS
        // ==========================================
        int tabWidth = 90;
        int tabSpacing = 2;
        int totalTabWidth = (tabWidth + tabSpacing) * Tab.values().length - tabSpacing;
        int tabStartX = cx - (totalTabWidth / 2);
        
        for (int i = 0; i < Tab.values().length; i++) {
            Tab tab = Tab.values()[i];
            final Tab tabFinal = tab;
            
            boolean isActive = (tab == currentTab);
            
            Button tabBtn = Button.builder(
                Component.literal(tab.name),
                b -> switchTab(tabFinal)
            ).bounds(
                tabStartX + (tabWidth + tabSpacing) * i, 
                tabY, 
                tabWidth, 
                22
            ).build();
            
            this.addRenderableWidget(tabBtn);
        }
        
        // ==========================================
        // TAB CONTENT
        // ==========================================
        switch (currentTab) {
            case MAIN:
                initMainTab();
                break;
            case AUTOMATION:
                initAutomationTab();
                break;
            case RECIPES:
                initRecipesTab();
                break;
            case PRESETS:
                initPresetsTab();
                break;
            case STATS:
                initStatsTab();
                break;
        }
        
        // ==========================================
        // FOOTER BUTTONS (Always visible)
        // ==========================================
        int footerY = this.height - 28;
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§c✖ Close"),
            b -> this.close()
        ).bounds(cx - 160, footerY, 70, 20).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§eMain Config"),
            b -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new com.bapelauto.AutoBotConfigScreen(this.parent));
                }
            }
        ).bounds(cx - 85, footerY, 80, 20).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§b? Help"),
            b -> showHelp()
        ).bounds(cx + 5, footerY, 75, 20).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§6⚡ Quick"),
            b -> slimefunManager.quickSetup(this.minecraft)
        ).bounds(cx + 85, footerY, 75, 20).build());
    }
    
    private void switchTab(Tab newTab) {
        currentTab = newTab;
        this.clearWidgets();
        this.init();
    }
    
    // ==========================================
    // MAIN TAB
    // ==========================================
    private void initMainTab() {
        int cx = this.width / 2;
        int startY = 70;
        
        // Slimefun Mode Toggle - Big Button
        this.addRenderableWidget(Button.builder(
            Component.literal(slimefunManager.isSlimefunModeEnabled() ? 
                "§a§l✓ SLIMEFUN MODE: ON" : "§c§l✗ SLIMEFUN MODE: OFF"),
            b -> {
                if (slimefunManager.isSlimefunModeEnabled()) {
                    slimefunManager.disable(this.minecraft);
                } else {
                    slimefunManager.enable(this.minecraft);
                }
                this.clearWidgets();
                this.init();
            }
        ).bounds(cx - 150, startY, 300, 30).build());
        
        startY += 40;
        
        // Current Machine Display
        SlimefunDetector.SlimefunMachine current = slimefunManager.getCurrentMachine();
        if (current != SlimefunDetector.SlimefunMachine.UNKNOWN) {
            this.addRenderableWidget(Button.builder(
                Component.literal("§7Machine: §f" + current.getDisplayName()),
                b -> {}
            ).bounds(cx - 150, startY, 300, 20).build());
            
            startY += 25;
            
            // Machine Instructions
            String instructions = SlimefunDetector.getMachineInstructions(current);
            // Display as non-clickable info (we'll render as text)
        }
        
        startY += 10;
        
        // Safety Mode
        this.addRenderableWidget(Button.builder(
            Component.literal("Safety Mode: " + (slimefunManager.isSafetyMode() ? "§a✓ ON" : "§c✗ OFF")),
            b -> {
                slimefunManager.setSafetyMode(!slimefunManager.isSafetyMode());
                b.setMessage(Component.literal("Safety Mode: " + 
                    (slimefunManager.isSafetyMode() ? "§a✓ ON" : "§c✗ OFF")));
            }
        ).bounds(cx - 150, startY, 145, 22).build());
        
        // Reset Statistics
        this.addRenderableWidget(Button.builder(
            Component.literal("§e🔄 Reset Stats"),
            b -> {
                slimefunManager.resetStatistics();
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendMessage(
                        Component.literal("§e[Slimefun] Statistics reset"), false
                    );
                }
            }
        ).bounds(cx + 5, startY, 145, 22).build());
        
        startY += 35;
        
        // Quick Actions Section
        this.addRenderableWidget(Button.builder(
            Component.literal("§a★ Quick Setup"),
            b -> slimefunManager.quickSetup(this.minecraft)
        ).bounds(cx - 150, startY, 145, 24).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§d📋 Full Status"),
            b -> slimefunManager.showDetailedStatus(this.minecraft)
        ).bounds(cx + 5, startY, 145, 24).build());
    }
    
    // ==========================================
    // AUTOMATION TAB
    // ==========================================
    private void initAutomationTab() {
        int cx = this.width / 2;
        int startY = 70;
        
        // ===== AUTO-INPUT SECTION =====
        
        this.addRenderableWidget(Button.builder(
            Component.literal(slimefunManager.isAutoInputEnabled() ? 
                "§a✓ Auto-Input: ON" : "§7○ Auto-Input: OFF"),
            b -> {
                slimefunManager.toggleAutoInput(this.minecraft);
                this.clearWidgets();
                this.init();
            }
        ).bounds(cx - 150, startY, 200, 24).build());
        
        // Delay control (only show if enabled)
        if (slimefunManager.isAutoInputEnabled()) {
            this.addRenderableWidget(Button.builder(
                Component.literal("§7⏱ " + slimefunManager.getInputFeeder().getFeedDelay() + "ms"),
                b -> {
                    long current = slimefunManager.getInputFeeder().getFeedDelay();
                    long next = current == 200 ? 300 : 
                               current == 300 ? 500 :
                               current == 500 ? 750 :
                               current == 750 ? 1000 : 200;
                    slimefunManager.getInputFeeder().setFeedDelay(next);
                    b.setMessage(Component.literal("§7⏱ " + next + "ms"));
                }
            ).bounds(cx + 55, startY, 95, 24).build());
        }
        
        startY += 35;
        
        // ===== AUTO-RECIPE SECTION =====
        
        this.addRenderableWidget(Button.builder(
            Component.literal(slimefunManager.isAutoRecipeEnabled() ? 
                "§a✓ Auto-Recipe: ON" : "§7○ Auto-Recipe: OFF"),
            b -> {
                slimefunManager.toggleAutoRecipe(this.minecraft);
                this.clearWidgets();
                this.init();
            }
        ).bounds(cx - 150, startY, 200, 24).build());
        
        // Delay control
        if (slimefunManager.isAutoRecipeEnabled()) {
            this.addRenderableWidget(Button.builder(
                Component.literal("§7⏱ " + slimefunManager.getRecipeFeeder().getFeedDelay() + "ms"),
                b -> {
                    long current = slimefunManager.getRecipeFeeder().getFeedDelay();
                    long next = current == 100 ? 200 : 
                               current == 200 ? 300 :
                               current == 300 ? 500 : 100;
                    slimefunManager.getRecipeFeeder().setFeedDelay(next);
                    b.setMessage(Component.literal("§7⏱ " + next + "ms"));
                }
            ).bounds(cx + 55, startY, 95, 24).build());
        }
        
        startY += 35;
        
        // ===== RECIPE LEARNING =====
        
        recipeNameField = new EditBox(
            this.font, cx - 150, startY, 200, 20, Component.literal("Recipe Name")
        );
        recipeNameField.setPlaceholder(Component.literal("Enter recipe name..."));
        this.addRenderableWidget(recipeNameField);
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§aLearn"),
            b -> {
                String name = recipeNameField.getText();
                if (name != null && !name.trim().isEmpty()) {
                    slimefunManager.learnRecipe(this.minecraft, name);
                    recipeNameField.setText("");
                } else {
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.sendMessage(
                            Component.literal("§c[Recipe] Enter a name first!"), false
                        );
                    }
                }
            }
        ).bounds(cx + 55, startY, 95, 20).build());
        
        startY += 35;
        
        // Current Recipe Display
        var currentRecipe = slimefunManager.getRecipeFeeder().getCurrentRecipe();
        if (currentRecipe != null) {
            this.addRenderableWidget(Button.builder(
                Component.literal("§7Active: §f" + currentRecipe.getName()),
                b -> {}
            ).bounds(cx - 150, startY, 300, 20).build());
        }
    }
    
    // ==========================================
    // RECIPES TAB
    // ==========================================
    private void initRecipesTab() {
        int cx = this.width / 2;
        int startY = 70;
        
        // Search field
        recipeSearchField = new EditBox(
            this.font, cx - 150, startY, 200, 20, Component.literal("Search")
        );
        recipeSearchField.setPlaceholder(Component.literal("Search recipes..."));
        recipeSearchField.setText(recipeSearchQuery);
        this.addRenderableWidget(recipeSearchField);
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§b🔍"),
            b -> {
                recipeSearchQuery = recipeSearchField.getText();
                recipeScrollOffset = 0;
                this.clearWidgets();
                this.init();
            }
        ).bounds(cx + 55, startY, 45, 20).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§c✖"),
            b -> {
                recipeSearchQuery = "";
                recipeSearchField.setText("");
                recipeScrollOffset = 0;
                this.clearWidgets();
                this.init();
            }
        ).bounds(cx + 105, startY, 45, 20).build());
        
        startY += 30;
        
        // Category buttons (compact)
        String[] categories = {"all", "basic", "electric", "tool", "armor", "magic"};
        String[] categoryLabels = {"All", "Basic", "Electric", "Tool", "Armor", "Magic"};
        
        int catX = cx - 150;
        for (int i = 0; i < categories.length; i++) {
            final String cat = categories[i];
            boolean isSelected = selectedCategory.equals(cat);
            
            this.addRenderableWidget(Button.builder(
                Component.literal((isSelected ? "§e" : "§7") + categoryLabels[i]),
                b -> {
                    selectedCategory = cat;
                    recipeScrollOffset = 0;
                    this.clearWidgets();
                    this.init();
                }
            ).bounds(catX, startY, 48, 18).build());
            
            catX += 50;
        }
        
        startY += 28;
        
        // Recipe list
        List<RecipeFeeder.Recipe> recipes;
        
        if (!recipeSearchQuery.isEmpty()) {
            recipes = SlimefunRecipeLibrary.searchRecipes(recipeSearchQuery);
        } else if (selectedCategory.equals("all")) {
            recipes = (List<RecipeFeeder.Recipe>) SlimefunRecipeLibrary.getAllRecipes();
        } else {
            recipes = SlimefunRecipeLibrary.getRecipesByCategory(selectedCategory);
        }
        
        // Convert to list if needed
        if (!(recipes instanceof List)) {
            recipes = new java.util.ArrayList<>(recipes);
        }
        
        int totalRecipes = recipes.size();
        int maxOffset = Math.max(0, totalRecipes - RECIPES_PER_PAGE);
        recipeScrollOffset = Math.min(recipeScrollOffset, maxOffset);
        
        // Display recipes
        int listY = startY;
        int displayCount = Math.min(RECIPES_PER_PAGE, totalRecipes - recipeScrollOffset);
        
        for (int i = 0; i < displayCount; i++) {
            int index = recipeScrollOffset + i;
            if (index >= recipes.size()) break;
            
            final RecipeFeeder.Recipe recipe = recipes.get(index);
            
            // Recipe button
            this.addRenderableWidget(Button.builder(
                Component.literal("§f" + recipe.getName()),
                b -> {
                    slimefunManager.getRecipeFeeder().setRecipe(recipe);
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.sendMessage(
                            Component.literal("§a[Recipe] Selected: " + recipe.getName()), true
                        );
                    }
                }
            ).bounds(cx - 150, listY, 240, 18).build());
            
            // Load button
            this.addRenderableWidget(Button.builder(
                Component.literal("§aUse"),
                b -> {
                    slimefunManager.getRecipeFeeder().setRecipe(recipe);
                    slimefunManager.enableAutoRecipe(this.minecraft);
                }
            ).bounds(cx + 95, listY, 55, 18).build());
            
            listY += 20;
        }
        
        // Scroll buttons
        if (totalRecipes > RECIPES_PER_PAGE) {
            int scrollY = this.height - 65;
            
            this.addRenderableWidget(Button.builder(
                Component.literal("§7▲ Previous"),
                b -> {
                    recipeScrollOffset = Math.max(0, recipeScrollOffset - RECIPES_PER_PAGE);
                    this.clearWidgets();
                    this.init();
                }
            ).bounds(cx - 150, scrollY, 145, 20).build());
            
            this.addRenderableWidget(Button.builder(
                Component.literal("§7Next ▼"),
                b -> {
                    recipeScrollOffset = Math.min(maxOffset, recipeScrollOffset + RECIPES_PER_PAGE);
                    this.clearWidgets();
                    this.init();
                }
            ).bounds(cx + 5, scrollY, 145, 20).build());
        }
    }
    
    // ==========================================
    // PRESETS TAB
    // ==========================================
    private void initPresetsTab() {
        int cx = this.width / 2;
        int startY = 70;
        
        SlimefunProfilePresets.SlimefunPreset[] presets = SlimefunProfilePresets.SlimefunPreset.values();
        
        int listY = startY;
        int displayCount = Math.min(10, presets.length);
        
        for (int i = 0; i < displayCount; i++) {
            final SlimefunProfilePresets.SlimefunPreset preset = presets[i];
            
            // Preset button
            this.addRenderableWidget(Button.builder(
                Component.literal("§f" + preset.getName()),
                b -> {
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.sendMessage(
                            Component.literal("§7" + preset.getDescription()), false
                        );
                    }
                }
            ).bounds(cx - 150, listY, 240, 20).build());
            
            // Apply button
            this.addRenderableWidget(Button.builder(
                Component.literal("§aApply"),
                b -> slimefunManager.applyPreset(preset, this.minecraft)
            ).bounds(cx + 95, listY, 55, 20).build());
            
            listY += 22;
        }
    }
    
    // ==========================================
    // STATS TAB
    // ==========================================
    private void initStatsTab() {
        int cx = this.width / 2;
        int startY = 70;
        
        // Compact stats display with buttons
        this.addRenderableWidget(Button.builder(
            Component.literal("§7Clicks: §f" + slimefunManager.getTotalSlimefunClicks()),
            b -> {}
        ).bounds(cx - 150, startY, 145, 20).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§7Collected: §f" + slimefunManager.getTotalItemsCollected()),
            b -> {}
        ).bounds(cx + 5, startY, 145, 20).build());
        
        startY += 25;
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§7Items Fed: §f" + slimefunManager.getInputFeeder().getTotalItemsFed()),
            b -> {}
        ).bounds(cx - 150, startY, 145, 20).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§7Recipe Items: §f" + slimefunManager.getRecipeFeeder().getTotalItemsPlaced()),
            b -> {}
        ).bounds(cx + 5, startY, 145, 20).build());
        
        startY += 35;
        
        // Action buttons
        this.addRenderableWidget(Button.builder(
            Component.literal("§b📊 Full Statistics"),
            b -> slimefunManager.showStatistics(this.minecraft)
        ).bounds(cx - 150, startY, 145, 24).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§d📋 Status Report"),
            b -> slimefunManager.showDetailedStatus(this.minecraft)
        ).bounds(cx + 5, startY, 145, 24).build());
        
        startY += 30;
        
        this.addRenderableWidget(Button.builder(
            Component.literal("§e🔄 Reset All Stats"),
            b -> {
                slimefunManager.resetStatistics();
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendMessage(
                        Component.literal("§e[Slimefun] All statistics reset"), false
                    );
                }
                this.clearWidgets();
                this.init();
            }
        ).bounds(cx - 150, startY, 300, 24).build());
    }
    
    // ==========================================
    // HELP
    // ==========================================
    private void showHelp() {
        if (this.minecraft == null || this.minecraft.player == null) return;
        
        this.minecraft.player.sendMessage(
            Component.literal("§e§l=== Slimefun Quick Guide ==="), false
        );
        this.minecraft.player.sendMessage(
            Component.literal("§6Main Tab:"), false
        );
        this.minecraft.player.sendMessage(
            Component.literal("  §7• Enable Slimefun Mode"), false
        );
        this.minecraft.player.sendMessage(
            Component.literal("  §7• Use Quick Setup for auto-config"), false
        );
        this.minecraft.player.sendMessage(
            Component.literal("§6Automation Tab:"), false
        );
        this.minecraft.player.sendMessage(
            Component.literal("  §7• Auto-Input: Feeds items to machines"), false
        );
        this.minecraft.player.sendMessage(
            Component.literal("  §7• Auto-Recipe: Maintains crafting recipes"), false
        );
        this.minecraft.player.sendMessage(
            Component.literal("§6Recipes Tab:"), false
        );
        this.minecraft.player.sendMessage(
            Component.literal("  §7• Browse " + SlimefunRecipeLibrary.getRecipeCount() + "+ pre-made recipes"), false
        );
        this.minecraft.player.sendMessage(
            Component.literal("  §7• Search and load instantly"), false
        );
    }
    
    // ==========================================
    // RENDER
    // ==========================================
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // Clean gradient background
        context.fillGradient(0, 0, this.width, this.height, 0xF0101010, 0xF0181818);
        
        super.render(context, mouseX, mouseY, delta);
        
        int cx = this.width / 2;
        
        // Modern title
        context.drawCenteredTextWithShadow(
            this.font,
            Component.literal("§2§lSLIMEFUN §6§lAUTOMATION §e§l+"),
            cx, 10, 0xFFFFFF
        );
        
        // Tab description
        context.drawCenteredTextWithShadow(
            this.font,
            Component.literal("§7" + currentTab.description),
            cx, 58, 0x888888
        );
        
        // Tab-specific rendering
        switch (currentTab) {
            case MAIN:
                renderMainTabInfo(context, cx);
                break;
            case RECIPES:
                renderRecipesTabInfo(context, cx);
                break;
        }
        
        // Footer info
        String statusInfo = slimefunManager.getStatusInfo();
        context.drawCenteredTextWithShadow(
            this.font,
            Component.literal(statusInfo),
            cx, this.height - 45, 0x666666
        );
    }
    
    private void renderMainTabInfo(GuiGraphics context, int cx) {
        SlimefunDetector.SlimefunMachine current = slimefunManager.getCurrentMachine();
        
        if (current != SlimefunDetector.SlimefunMachine.UNKNOWN) {
            String instructions = SlimefunDetector.getMachineInstructions(current);
            
            // Wrap long instructions
            int maxWidth = 280;
            List<String> lines = wrapText(instructions, maxWidth);
            
            int y = 140;
            for (String line : lines) {
                context.drawCenteredTextWithShadow(
                    this.font,
                    Component.literal("§7" + line),
                    cx, y, 0xAAAAAA
                );
                y += 10;
            }
        }
    }
    
    private void renderRecipesTabInfo(GuiGraphics context, int cx) {
        List<RecipeFeeder.Recipe> recipes;
        
        if (!recipeSearchQuery.isEmpty()) {
            recipes = SlimefunRecipeLibrary.searchRecipes(recipeSearchQuery);
        } else if (selectedCategory.equals("all")) {
            recipes = new java.util.ArrayList<>(SlimefunRecipeLibrary.getAllRecipes());
        } else {
            recipes = SlimefunRecipeLibrary.getRecipesByCategory(selectedCategory);
        }
        
        String info = String.format("§7Showing %d recipes", recipes.size());
        context.drawCenteredTextWithShadow(
            this.font,
            Component.literal(info),
            cx, this.height - 80, 0x666666
        );
    }
    
    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        
        for (String word : words) {
            if (this.font.getWidth(currentLine + " " + word) < maxWidth) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines;
    }
    
    @Override
    public void close() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.close();
            return true;
        }
        
        // Quick tab switching with numbers
        if (keyCode >= 49 && keyCode <= 53) { // 1-5
            int tabIndex = keyCode - 49;
            if (tabIndex < Tab.values().length) {
                switchTab(Tab.values()[tabIndex]);
                return true;
            }
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}