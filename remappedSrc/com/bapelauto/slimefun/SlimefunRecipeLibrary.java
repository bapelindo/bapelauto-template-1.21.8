// ============================================
// FILE: SlimefunRecipeLibrary.java
// Path: src/main/java/com/bapelauto/slimefun/SlimefunRecipeLibrary.java
// ============================================
package com.bapelauto.slimefun;

import com.bapelauto.slimefun.RecipeFeeder.ItemMatcher;
import com.bapelauto.slimefun.RecipeFeeder.Recipe;

import java.util.*;

/**
 * Comprehensive library of Slimefun recipes
 * Pre-configured recipes for common items
 */
public class SlimefunRecipeLibrary {
    
    private static final Map<String, Recipe> RECIPES = new HashMap<>();
    
    static {
        initializeBasicComponents();
        initializeElectricComponents();
        initializeTools();
        initializeArmor();
        initializeMagicItems();
        initializeTechnicalComponents();
        initializeAndroidParts();
        initializeCargoComponents();
    }
    
    /**
     * Initialize basic component recipes
     */
    private static void initializeBasicComponents() {
        // Copper Wire (Enhanced Crafting Table)
        RECIPES.put("copper_wire", createRecipe(
            "Copper Wire",
            map(4, matcher("copper_ingot", 1)),
            24
        ));
        
        // Gold Wire
        RECIPES.put("gold_wire", createRecipe(
            "Gold Wire",
            map(4, matcher("gold_ingot", 1)),
            24
        ));
        
        // Steel Ingot
        RECIPES.put("steel_ingot", createRecipe(
            "Steel Ingot",
            map(
                1, matcher("iron_ingot", 1),
                3, matcher("iron_ingot", 1),
                5, matcher("iron_ingot", 1),
                7, matcher("iron_ingot", 1),
                4, matcher(Arrays.asList("coal", "charcoal"), 4)
            ),
            24
        ));
        
        // Hardened Metal
        RECIPES.put("hardened_metal", createRecipe(
            "Hardened Metal",
            map(
                0, matcher("steel_ingot", 1),
                2, matcher("steel_ingot", 1),
                6, matcher("steel_ingot", 1),
                8, matcher("steel_ingot", 1),
                4, matcher("glass", 4)
            ),
            24
        ));
        
        // Reinforced Alloy Ingot
        RECIPES.put("reinforced_alloy", createRecipe(
            "Reinforced Alloy Ingot",
            map(
                1, matcher("hardened_metal", 1),
                3, matcher("hardened_metal", 1),
                5, matcher("hardened_metal", 1),
                7, matcher("hardened_metal", 1),
                4, matcher("hardened_glass", 4)
            ),
            24
        ));
        
        // Carbonado
        RECIPES.put("carbonado", createRecipe(
            "Carbonado",
            map(
                0, matcher("coal", 1),
                1, matcher("coal", 1),
                2, matcher("coal", 1),
                3, matcher("coal", 1),
                4, matcher("glass", 1),
                5, matcher("coal", 1),
                6, matcher("coal", 1),
                7, matcher("coal", 1),
                8, matcher("coal", 1)
            ),
            24
        ));
        
        // Compressed Carbon
        RECIPES.put("compressed_carbon", createRecipe(
            "Compressed Carbon",
            map(4, matcher(Arrays.asList("coal", "charcoal", "carbon"), 4)),
            24
        ));
        
        // Carbon
        RECIPES.put("carbon", createRecipe(
            "Carbon",
            map(4, matcher(Arrays.asList("coal", "charcoal"), 8)),
            24
        ));
    }
    
    /**
     * Initialize electric component recipes
     */
    private static void initializeElectricComponents() {
        // Basic Circuit Board
        RECIPES.put("basic_circuit", createRecipe(
            "Basic Circuit Board",
            map(
                1, matcher("copper_wire", 1),
                3, matcher("copper_wire", 1),
                4, matcher("redstone", 1),
                5, matcher("copper_wire", 1),
                7, matcher("copper_wire", 1)
            ),
            24
        ));
        
        // Advanced Circuit Board
        RECIPES.put("advanced_circuit", createRecipe(
            "Advanced Circuit Board",
            map(
                1, matcher("gold_wire", 1),
                3, matcher("gold_wire", 1),
                4, matcher("redstone_block", 1),
                5, matcher("gold_wire", 1),
                7, matcher("gold_wire", 1)
            ),
            24
        ));
        
        // Electric Motor
        RECIPES.put("electric_motor", createRecipe(
            "Electric Motor",
            map(
                1, matcher("copper_wire", 1),
                3, matcher("copper_wire", 1),
                4, matcher("iron_ingot", 1),
                5, matcher("copper_wire", 1),
                7, matcher("copper_wire", 1)
            ),
            24
        ));
        
        // Heating Coil
        RECIPES.put("heating_coil", createRecipe(
            "Heating Coil",
            map(
                1, matcher("copper_wire", 1),
                3, matcher("copper_wire", 1),
                4, matcher("lava_bucket", 1),
                5, matcher("copper_wire", 1),
                7, matcher("copper_wire", 1)
            ),
            24
        ));
        
        // Cooling Unit
        RECIPES.put("cooling_unit", createRecipe(
            "Cooling Unit",
            map(
                1, matcher("aluminum_ingot", 1),
                3, matcher("aluminum_ingot", 1),
                4, matcher("ice", 1),
                5, matcher("aluminum_ingot", 1),
                7, matcher("aluminum_ingot", 1)
            ),
            24
        ));
        
        // Small Capacitor
        RECIPES.put("small_capacitor", createRecipe(
            "Small Capacitor",
            map(
                1, matcher("copper_wire", 1),
                3, matcher("copper_wire", 1),
                4, matcher("redstone", 1),
                5, matcher("copper_wire", 1),
                7, matcher("sulfate", 1)
            ),
            24
        ));
        
        // Medium Capacitor
        RECIPES.put("medium_capacitor", createRecipe(
            "Medium Capacitor",
            map(
                1, matcher("gold_wire", 1),
                3, matcher("gold_wire", 1),
                4, matcher("redstone_block", 1),
                5, matcher("gold_wire", 1),
                7, matcher("small_capacitor", 1)
            ),
            24
        ));
        
        // Large Capacitor
        RECIPES.put("large_capacitor", createRecipe(
            "Large Capacitor",
            map(
                1, matcher("aluminum_wire", 1),
                3, matcher("aluminum_wire", 1),
                4, matcher("medium_capacitor", 1),
                5, matcher("aluminum_wire", 1),
                7, matcher("redstone_block", 1)
            ),
            24
        ));
    }
    
    /**
     * Initialize tool recipes
     */
    private static void initializeTools() {
        // Smeltery Pickaxe
        RECIPES.put("smeltery_pickaxe", createRecipe(
            "Smeltery Pickaxe",
            map(
                0, matcher("hardened_metal", 1),
                1, matcher("hardened_metal", 1),
                2, matcher("hardened_metal", 1),
                4, matcher("stick", 1),
                7, matcher("stick", 1)
            ),
            24
        ));
        
        // Lumber Axe
        RECIPES.put("lumber_axe", createRecipe(
            "Lumber Axe",
            map(
                0, matcher("hardened_metal", 1),
                1, matcher("hardened_metal", 1),
                3, matcher("hardened_metal", 1),
                4, matcher("stick", 1),
                7, matcher("stick", 1)
            ),
            24
        ));
        
        // Explosive Pickaxe
        RECIPES.put("explosive_pickaxe", createRecipe(
            "Explosive Pickaxe",
            map(
                0, matcher("smeltery_pickaxe", 1),
                1, matcher("tnt", 1),
                2, matcher("smeltery_pickaxe", 1),
                3, matcher("tnt", 1),
                4, matcher("redstone_block", 1),
                5, matcher("tnt", 1)
            ),
            24
        ));
        
        // Explosive Shovel
        RECIPES.put("explosive_shovel", createRecipe(
            "Explosive Shovel",
            map(
                1, matcher("hardened_metal", 1),
                4, matcher("stick", 1),
                6, matcher("tnt", 1),
                7, matcher("stick", 1),
                8, matcher("tnt", 1)
            ),
            24
        ));
    }
    
    /**
     * Initialize armor recipes
     */
    private static void initializeArmor() {
        // Gilded Iron Helmet
        RECIPES.put("gilded_iron_helmet", createRecipe(
            "Gilded Iron Helmet",
            map(
                0, matcher("gold_ingot", 1),
                1, matcher("iron_helmet", 1),
                2, matcher("gold_ingot", 1)
            ),
            24
        ));
        
        // Gilded Iron Chestplate
        RECIPES.put("gilded_iron_chestplate", createRecipe(
            "Gilded Iron Chestplate",
            map(
                0, matcher("gold_ingot", 1),
                1, matcher("iron_chestplate", 1),
                2, matcher("gold_ingot", 1),
                4, matcher("gold_ingot", 1),
                6, matcher("gold_ingot", 1)
            ),
            24
        ));
        
        // Ender Helmet
        RECIPES.put("ender_helmet", createRecipe(
            "Ender Helmet",
            map(
                0, matcher("ender_pearl", 1),
                1, matcher("ender_pearl", 1),
                2, matcher("ender_pearl", 1),
                3, matcher("ender_pearl", 1),
                5, matcher("ender_pearl", 1)
            ),
            24
        ));
        
        // Slime Boots
        RECIPES.put("slime_boots", createRecipe(
            "Slime Boots",
            map(
                0, matcher("slime_ball", 1),
                2, matcher("slime_ball", 1),
                3, matcher("slime_ball", 1),
                5, matcher("slime_ball", 1),
                6, matcher("slime_ball", 1),
                8, matcher("slime_ball", 1)
            ),
            24
        ));
    }
    
    /**
     * Initialize magic item recipes
     */
    private static void initializeMagicItems() {
        // Talisman of Miner
        RECIPES.put("talisman_miner", createRecipe(
            "Talisman of Miner",
            map(
                0, matcher("emerald", 1),
                1, matcher("magic_lump_2", 1),
                2, matcher("emerald", 1),
                3, matcher("redstone", 1),
                4, matcher("diamond_pickaxe", 1),
                5, matcher("redstone", 1),
                6, matcher("emerald", 1),
                7, matcher("magic_lump_2", 1),
                8, matcher("emerald", 1)
            ),
            24
        ));
        
        // Talisman of Hunter
        RECIPES.put("talisman_hunter", createRecipe(
            "Talisman of Hunter",
            map(
                0, matcher("emerald", 1),
                1, matcher("magic_lump_2", 1),
                2, matcher("emerald", 1),
                3, matcher("redstone", 1),
                4, matcher("diamond_sword", 1),
                5, matcher("redstone", 1),
                6, matcher("emerald", 1),
                7, matcher("magic_lump_2", 1),
                8, matcher("emerald", 1)
            ),
            24
        ));
        
        // Magic Lump (Tier 1)
        RECIPES.put("magic_lump_1", createRecipe(
            "Magic Lump 1",
            map(
                0, matcher("gold_nugget", 1),
                1, matcher("redstone", 1),
                2, matcher("gold_nugget", 1),
                3, matcher("redstone", 1),
                4, matcher("nether_wart", 1),
                5, matcher("redstone", 1),
                6, matcher("gold_nugget", 1),
                7, matcher("redstone", 1),
                8, matcher("gold_nugget", 1)
            ),
            24
        ));
        
        // Magic Lump (Tier 2)
        RECIPES.put("magic_lump_2", createRecipe(
            "Magic Lump 2",
            map(
                0, matcher("gold_ingot", 1),
                1, matcher("redstone", 1),
                2, matcher("gold_ingot", 1),
                3, matcher("redstone", 1),
                4, matcher("magic_lump_1", 1),
                5, matcher("redstone", 1),
                6, matcher("gold_ingot", 1),
                7, matcher("redstone", 1),
                8, matcher("gold_ingot", 1)
            ),
            24
        ));
    }
    
    /**
     * Initialize technical component recipes
     */
    private static void initializeTechnicalComponents() {
        // Solar Panel
        RECIPES.put("solar_panel", createRecipe(
            "Solar Panel",
            map(
                0, matcher("glass", 1),
                1, matcher("glass", 1),
                2, matcher("glass", 1),
                3, matcher("silicon", 1),
                4, matcher("silicon", 1),
                5, matcher("silicon", 1),
                6, matcher("copper_wire", 1),
                7, matcher("copper_wire", 1),
                8, matcher("copper_wire", 1)
            ),
            24
        ));
        
        // GPS Transmitter
        RECIPES.put("gps_transmitter", createRecipe(
            "GPS Transmitter",
            map(
                1, matcher("redstone", 1),
                3, matcher("iron_ingot", 1),
                4, matcher("redstone_torch", 1),
                5, matcher("iron_ingot", 1),
                7, matcher("iron_ingot", 1)
            ),
            24
        ));
        
        // Pressure Chamber
        RECIPES.put("pressure_chamber", createRecipe(
            "Pressure Chamber",
            map(
                0, matcher("glass", 1),
                1, matcher("piston", 1),
                2, matcher("glass", 1),
                3, matcher("glass", 1),
                4, matcher("cauldron", 1),
                5, matcher("glass", 1),
                6, matcher("glass", 1),
                7, matcher("dispenser", 1),
                8, matcher("glass", 1)
            ),
            24
        ));
        
        // Electromagnet
        RECIPES.put("electromagnet", createRecipe(
            "Electromagnet",
            map(
                1, matcher("iron_ingot", 1),
                3, matcher("copper_wire", 1),
                4, matcher("iron_block", 1),
                5, matcher("copper_wire", 1),
                7, matcher("iron_ingot", 1)
            ),
            24
        ));
        
        // Hologram Projector
        RECIPES.put("hologram_projector", createRecipe(
            "Hologram Projector",
            map(
                1, matcher("glass", 1),
                3, matcher("redstone", 1),
                4, matcher("eye_of_ender", 1),
                5, matcher("redstone", 1),
                6, matcher("quartz_block", 1),
                7, matcher("iron_block", 1),
                8, matcher("quartz_block", 1)
            ),
            24
        ));
    }
    
    /**
     * Initialize android part recipes
     */
    private static void initializeAndroidParts() {
        // Android Memory Core
        RECIPES.put("android_memory", createRecipe(
            "Android Memory Core",
            map(
                0, matcher("redstone", 1),
                1, matcher("redstone", 1),
                2, matcher("redstone", 1),
                3, matcher("steel_ingot", 1),
                4, matcher("basic_circuit", 1),
                5, matcher("steel_ingot", 1),
                6, matcher("redstone", 1),
                7, matcher("redstone", 1),
                8, matcher("redstone", 1)
            ),
            24
        ));
        
        // Android Fuel
        RECIPES.put("android_fuel", createRecipe(
            "Android Fuel",
            map(
                1, matcher("coal", 1),
                3, matcher("redstone", 1),
                4, matcher("lava_bucket", 1),
                5, matcher("redstone", 1),
                7, matcher("coal", 1)
            ),
            24
        ));
    }
    
    /**
     * Initialize cargo component recipes
     */
    private static void initializeCargoComponents() {
        // Cargo Motor
        RECIPES.put("cargo_motor", createRecipe(
            "Cargo Motor",
            map(
                1, matcher("iron_ingot", 1),
                3, matcher("copper_wire", 1),
                4, matcher("hardened_glass", 1),
                5, matcher("copper_wire", 1),
                7, matcher("iron_ingot", 1)
            ),
            24
        ));
        
        // Cargo Manager
        RECIPES.put("cargo_manager", createRecipe(
            "Cargo Manager",
            map(
                1, matcher("iron_ingot", 1),
                3, matcher("hologram_projector", 1),
                4, matcher("advanced_circuit", 1),
                5, matcher("hologram_projector", 1),
                7, matcher("iron_ingot", 1)
            ),
            24
        ));
    }
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    private static Recipe createRecipe(String name, Map<Integer, ItemMatcher> slots, int outputSlot) {
        return new Recipe(name, slots, outputSlot);
    }
    
    private static Map<Integer, ItemMatcher> map(Object... pairs) {
        Map<Integer, ItemMatcher> result = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put((Integer) pairs[i], (ItemMatcher) pairs[i + 1]);
        }
        return result;
    }
    
    private static ItemMatcher matcher(String itemName, int count) {
        return new ItemMatcher(itemName, count);
    }
    
    private static ItemMatcher matcher(List<String> itemNames, int count) {
        return new ItemMatcher(itemNames, count);
    }
    
    // ========================================
    // PUBLIC API
    // ========================================
    
    /**
     * Get recipe by name
     */
    public static Recipe getRecipe(String name) {
        return RECIPES.get(name.toLowerCase().replace(" ", "_"));
    }
    
    /**
     * Get all recipe names
     */
    public static List<String> getAllRecipeNames() {
        List<String> names = new ArrayList<>(RECIPES.keySet());
        Collections.sort(names);
        return names;
    }
    
    /**
     * Get all recipes
     */
    public static Collection<Recipe> getAllRecipes() {
        return RECIPES.values();
    }
    
    /**
     * Get recipes by category
     */
    public static List<Recipe> getRecipesByCategory(String category) {
        List<Recipe> result = new ArrayList<>();
        String cat = category.toLowerCase();
        
        for (Map.Entry<String, Recipe> entry : RECIPES.entrySet()) {
            String key = entry.getKey();
            
            if (cat.equals("basic") && isBasicComponent(key)) {
                result.add(entry.getValue());
            } else if (cat.equals("electric") && isElectricComponent(key)) {
                result.add(entry.getValue());
            } else if (cat.equals("tool") && isTool(key)) {
                result.add(entry.getValue());
            } else if (cat.equals("armor") && isArmor(key)) {
                result.add(entry.getValue());
            } else if (cat.equals("magic") && isMagic(key)) {
                result.add(entry.getValue());
            } else if (cat.equals("technical") && isTechnical(key)) {
                result.add(entry.getValue());
            } else if (cat.equals("android") && isAndroid(key)) {
                result.add(entry.getValue());
            } else if (cat.equals("cargo") && isCargo(key)) {
                result.add(entry.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Search recipes by name
     */
    public static List<Recipe> searchRecipes(String query) {
        List<Recipe> result = new ArrayList<>();
        String q = query.toLowerCase();
        
        for (Map.Entry<String, Recipe> entry : RECIPES.entrySet()) {
            if (entry.getKey().contains(q) || entry.getValue().getName().toLowerCase().contains(q)) {
                result.add(entry.getValue());
            }
        }
        
        return result;
    }
    
    /**
     * Get recipe count
     */
    public static int getRecipeCount() {
        return RECIPES.size();
    }
    
    /**
     * Get categories
     */
    public static List<String> getCategories() {
        return Arrays.asList(
            "Basic Components",
            "Electric Components",
            "Tools",
            "Armor",
            "Magic Items",
            "Technical",
            "Android Parts",
            "Cargo"
        );
    }
    
    // Category checkers
    private static boolean isBasicComponent(String key) {
        return key.contains("wire") || key.contains("steel") || key.contains("hardened") || 
               key.contains("alloy") || key.contains("carbon") || key.equals("carbonado");
    }
    
    private static boolean isElectricComponent(String key) {
        return key.contains("circuit") || key.contains("motor") || key.contains("heating") ||
               key.contains("cooling") || key.contains("capacitor");
    }
    
    private static boolean isTool(String key) {
        return key.contains("pickaxe") || key.contains("axe") || key.contains("shovel") ||
               key.contains("hoe") || key.contains("sword");
    }
    
    private static boolean isArmor(String key) {
        return key.contains("helmet") || key.contains("chestplate") || key.contains("leggings") ||
               key.contains("boots") || key.contains("gilded") || key.contains("ender");
    }
    
    private static boolean isMagic(String key) {
        return key.contains("talisman") || key.contains("magic") || key.contains("lump");
    }
    
    private static boolean isTechnical(String key) {
        return key.contains("solar") || key.contains("gps") || key.contains("pressure") ||
               key.contains("electromagnet") || key.contains("hologram");
    }
    
    private static boolean isAndroid(String key) {
        return key.contains("android");
    }
    
    private static boolean isCargo(String key) {
        return key.contains("cargo");
    }
    
    /**
     * Register custom recipe
     */
    public static void registerRecipe(String key, Recipe recipe) {
        RECIPES.put(key.toLowerCase().replace(" ", "_"), recipe);
    }
}