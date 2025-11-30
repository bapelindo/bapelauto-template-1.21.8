// ============================================
// FILE: RecipeFeeder.java
// Path: src/main/java/com/bapelauto/slimefun/RecipeFeeder.java
// ============================================
package com.bapelauto.slimefun;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.*;

/**
 * Recipe Feeder System for Auto-Crafters
 * Automatically fills crafting recipe slots with items from player inventory
 */
public class RecipeFeeder {
    
    /**
     * Recipe definition for a machine
     */
    public static class Recipe {
        private final String name;
        private final Map<Integer, ItemMatcher> slotRequirements; // slot -> required item
        private final int outputSlot;
        
        public Recipe(String name, Map<Integer, ItemMatcher> slotRequirements, int outputSlot) {
            this.name = name;
            this.slotRequirements = slotRequirements;
            this.outputSlot = outputSlot;
        }
        
        public String getName() { return name; }
        public Map<Integer, ItemMatcher> getSlotRequirements() { return slotRequirements; }
        public int getOutputSlot() { return outputSlot; }
        
        /**
         * Check if recipe is complete
         */
        public boolean isComplete(ScreenHandler handler) {
            for (Map.Entry<Integer, ItemMatcher> entry : slotRequirements.entrySet()) {
                int slotId = entry.getKey();
                ItemMatcher matcher = entry.getValue();
                
                if (slotId >= handler.slots.size()) return false;
                
                Slot slot = handler.getSlot(slotId);
                if (!matcher.matches(slot.getStack())) {
                    return false;
                }
            }
            return true;
        }
        
        /**
         * Get missing items for this recipe
         */
        public List<MissingItem> getMissingItems(ScreenHandler handler) {
            List<MissingItem> missing = new ArrayList<>();
            
            for (Map.Entry<Integer, ItemMatcher> entry : slotRequirements.entrySet()) {
                int slotId = entry.getKey();
                ItemMatcher matcher = entry.getValue();
                
                if (slotId >= handler.slots.size()) continue;
                
                Slot slot = handler.getSlot(slotId);
                ItemStack currentStack = slot.getStack();
                
                if (!matcher.matches(currentStack)) {
                    int needed = matcher.getRequiredCount();
                    int current = currentStack.isEmpty() ? 0 : currentStack.getCount();
                    missing.add(new MissingItem(slotId, matcher, needed - current));
                }
            }
            
            return missing;
        }
    }
    
    /**
     * Item matcher for flexible recipe matching
     */
    public static class ItemMatcher {
        private final List<String> acceptedItems; // item names or IDs
        private final int requiredCount;
        
        public ItemMatcher(String itemName, int count) {
            this.acceptedItems = Arrays.asList(itemName.toLowerCase());
            this.requiredCount = count;
        }
        
        public ItemMatcher(List<String> itemNames, int count) {
            this.acceptedItems = new ArrayList<>();
            for (String name : itemNames) {
                this.acceptedItems.add(name.toLowerCase());
            }
            this.requiredCount = count;
        }
        
        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (stack.getCount() < requiredCount) return false;
            
            String itemName = stack.getItem().getName().getString().toLowerCase();
            String itemId = stack.getItem().toString().toLowerCase();
            
            for (String accepted : acceptedItems) {
                if (itemName.contains(accepted) || itemId.contains(accepted)) {
                    return true;
                }
            }
            
            return false;
        }
        
        public boolean matchesPartial(ItemStack stack) {
            if (stack.isEmpty()) return false;
            
            String itemName = stack.getItem().getName().getString().toLowerCase();
            String itemId = stack.getItem().toString().toLowerCase();
            
            for (String accepted : acceptedItems) {
                if (itemName.contains(accepted) || itemId.contains(accepted)) {
                    return true;
                }
            }
            
            return false;
        }
        
        public int getRequiredCount() { return requiredCount; }
        public List<String> getAcceptedItems() { return acceptedItems; }
    }
    
    /**
     * Missing item info
     */
    public static class MissingItem {
        public final int slotId;
        public final ItemMatcher matcher;
        public final int countNeeded;
        
        public MissingItem(int slotId, ItemMatcher matcher, int countNeeded) {
            this.slotId = slotId;
            this.matcher = matcher;
            this.countNeeded = countNeeded;
        }
    }
    
    // Preset recipes for common Slimefun machines
    private static final Map<String, Recipe> PRESET_RECIPES = new HashMap<>();
    
    static {
        // Enhanced Crafting Table - Example recipes
        // Slot layout: 0-8 = crafting grid (3x3), 24 = output
        
        // Example: Basic Circuit Board (placeholder recipe)
        Map<Integer, ItemMatcher> circuitRecipe = new HashMap<>();
        circuitRecipe.put(1, new ItemMatcher("copper", 1));
        circuitRecipe.put(3, new ItemMatcher("copper", 1));
        circuitRecipe.put(4, new ItemMatcher("redstone", 1));
        circuitRecipe.put(5, new ItemMatcher("copper", 1));
        circuitRecipe.put(7, new ItemMatcher("copper", 1));
        PRESET_RECIPES.put("circuit_board", new Recipe("Circuit Board", circuitRecipe, 24));
    }
    
    private boolean enabled = false;
    private Recipe currentRecipe = null;
    private long lastFeedTime = 0;
    private long feedDelay = 300; // 300ms between item placements
    private int totalItemsPlaced = 0;
    
    private boolean autoDetectMode = true; // Try to detect and maintain recipe automatically
    
    /**
     * Main tick - check and feed recipe ingredients
     */
    public void tick(MinecraftClient client) {
        if (!enabled) return;
        if (client.currentScreen == null || !(client.currentScreen instanceof HandledScreen)) return;
        if (client.interactionManager == null || client.player == null) return;
        if (currentRecipe == null) return;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFeedTime < feedDelay) return;
        
        HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;
        ScreenHandler handler = screen.getScreenHandler();
        
        // Check if recipe is complete
        if (currentRecipe.isComplete(handler)) {
            // Recipe is ready, nothing to do
            return;
        }
        
        // Get missing items
        List<MissingItem> missingItems = currentRecipe.getMissingItems(handler);
        
        if (missingItems.isEmpty()) return;
        
        // Try to feed one missing item
        for (MissingItem missing : missingItems) {
            if (feedMissingItem(client, handler, missing)) {
                lastFeedTime = currentTime;
                return; // Feed one at a time
            }
        }
        
        // If we get here, we couldn't feed any items (probably missing from inventory)
        if (autoDetectMode && client.player != null) {
            client.player.sendMessage(
                Text.literal("§c[Recipe Feeder] Missing required items for recipe!"),
                true
            );
        }
    }
    
    /**
     * Feed a missing item to recipe slot
     */
    private boolean feedMissingItem(MinecraftClient client, ScreenHandler handler, MissingItem missing) {
        // Get player inventory range
        int totalSlots = handler.slots.size();
        int playerStart = Math.max(0, totalSlots - 36);
        
        // Search player inventory for matching item
        for (int i = playerStart; i < totalSlots; i++) {
            if (i >= handler.slots.size()) break;
            
            Slot playerSlot = handler.getSlot(i);
            if (!playerSlot.hasStack()) continue;
            
            ItemStack stack = playerSlot.getStack();
            
            // Check if this item matches what we need
            if (missing.matcher.matchesPartial(stack)) {
                // Calculate how many to take
                int toTake = Math.min(missing.countNeeded, stack.getCount());
                
                try {
                    // Pick up items from player inventory
                    if (toTake == stack.getCount()) {
                        // Take all
                        client.interactionManager.clickSlot(
                            handler.syncId, i, 0, SlotActionType.PICKUP, client.player
                        );
                    } else {
                        // Take partial (right-click to take half, or shift-click logic)
                        // For simplicity, take all and put back extra
                        client.interactionManager.clickSlot(
                            handler.syncId, i, 0, SlotActionType.PICKUP, client.player
                        );
                    }
                    
                    // Place in recipe slot
                    int targetSlot = missing.slotId;
                    Slot recipeSlot = handler.getSlot(targetSlot);
                    
                    if (recipeSlot.getStack().isEmpty()) {
                        // Slot is empty, place item
                        client.interactionManager.clickSlot(
                            handler.syncId, targetSlot, 0, SlotActionType.PICKUP, client.player
                        );
                    } else {
                        // Slot has items, add to stack
                        client.interactionManager.clickSlot(
                            handler.syncId, targetSlot, 0, SlotActionType.PICKUP, client.player
                        );
                    }
                    
                    // Put back any remaining items
                    if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                        client.interactionManager.clickSlot(
                            handler.syncId, i, 0, SlotActionType.PICKUP, client.player
                        );
                    }
                    
                    totalItemsPlaced++;
                    
                    if (client.player != null) {
                        client.player.sendMessage(
                            Text.literal("§a[Recipe] Placed " + stack.getItem().getName().getString() + 
                                       " in slot " + targetSlot),
                            true
                        );
                    }
                    
                    return true;
                    
                } catch (Exception e) {
                    System.err.println("[RecipeFeeder] Error placing item: " + e.getMessage());
                }
            }
        }
        
        return false;
    }
    
    /**
     * Learn recipe from current crafting grid state
     */
    public Recipe learnRecipe(MinecraftClient client, String recipeName) {
        if (!(client.currentScreen instanceof HandledScreen)) {
            return null;
        }
        
        HandledScreen<?> screen = (HandledScreen<?>) client.currentScreen;
        ScreenHandler handler = screen.getScreenHandler();
        
        Map<Integer, ItemMatcher> learned = new HashMap<>();
        
        // Detect crafting grid slots (typically 0-8 for 3x3, or other layouts)
        // For Enhanced Crafting Table, assume slots 0-8 are input
        int[] craftingSlots = detectCraftingSlots(handler);
        
        for (int slotId : craftingSlots) {
            if (slotId >= handler.slots.size()) continue;
            
            Slot slot = handler.getSlot(slotId);
            if (slot.hasStack()) {
                ItemStack stack = slot.getStack();
                String itemName = stack.getItem().toString().toLowerCase();
                learned.put(slotId, new ItemMatcher(itemName, stack.getCount()));
            }
        }
        
        if (learned.isEmpty()) {
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§c[Recipe] No items in crafting grid to learn!"),
                    false
                );
            }
            return null;
        }
        
        // Detect output slot
        int outputSlot = detectOutputSlot(handler);
        
        Recipe recipe = new Recipe(recipeName, learned, outputSlot);
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("§a[Recipe] Learned recipe: " + recipeName + " (" + learned.size() + " ingredients)"),
                true
            );
        }
        
        return recipe;
    }
    
    /**
     * Detect crafting grid slots
     */
    private int[] detectCraftingSlots(ScreenHandler handler) {
        // For Enhanced Crafting Table: typically slots 0-8
        // For standard Crafting Table: slots 1-9
        // This is a simplified version - may need adjustment per machine
        
        int totalSlots = handler.slots.size();
        
        if (totalSlots >= 54) {
            // Large GUI, probably Enhanced Crafting Table
            return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        } else {
            // Standard crafting
            return new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        }
    }
    
    /**
     * Detect output slot
     */
    private int detectOutputSlot(ScreenHandler handler) {
        // Common output slots: 0, 24, or right side of GUI
        // This is simplified - may need adjustment
        
        int totalSlots = handler.slots.size();
        
        if (totalSlots >= 54) {
            return 24; // Enhanced Crafting Table
        } else {
            return 0; // Standard crafting
        }
    }
    
    /**
     * Save recipe to presets
     */
    public void saveRecipe(Recipe recipe) {
        PRESET_RECIPES.put(recipe.getName().toLowerCase().replace(" ", "_"), recipe);
    }
    
    /**
     * Load recipe from presets
     */
    public Recipe loadRecipe(String recipeName) {
        return PRESET_RECIPES.get(recipeName.toLowerCase().replace(" ", "_"));
    }
    
    /**
     * Set current active recipe
     */
    public void setRecipe(Recipe recipe) {
        this.currentRecipe = recipe;
        
        if (recipe != null) {
            System.out.println("[RecipeFeeder] Active recipe: " + recipe.getName());
        }
    }
    
    /**
     * Get status info
     */
    public String getStatusInfo() {
        if (!enabled) return "§7Recipe Feeder: §cOFF";
        if (currentRecipe == null) return "§7Recipe Feeder: §eNo Recipe";
        
        return String.format("§7Recipe: §f%s §7| Placed: §f%d",
            currentRecipe.getName(), totalItemsPlaced);
    }
    
    public void reset() {
        totalItemsPlaced = 0;
    }
    
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public Recipe getCurrentRecipe() { return currentRecipe; }
    
    public long getFeedDelay() { return feedDelay; }
    public void setFeedDelay(long delay) { this.feedDelay = Math.max(100, delay); }
    
    public boolean isAutoDetectMode() { return autoDetectMode; }
    public void setAutoDetectMode(boolean enabled) { this.autoDetectMode = enabled; }
    
    public int getTotalItemsPlaced() { return totalItemsPlaced; }
    
    public Collection<Recipe> getAllRecipes() { return PRESET_RECIPES.values(); }
}