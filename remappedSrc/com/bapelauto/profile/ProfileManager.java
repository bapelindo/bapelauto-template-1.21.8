// ============================================
// FILE: ProfileManager.java
// Path: src/main/java/com/bapelauto/profile/ProfileManager.java
// ============================================
package com.bapelauto.profile;

import com.bapelauto.ShardedConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages multiple configuration profiles for different scenarios
 */
public class ProfileManager {
    private static final String PROFILES_DIR = "config/bapelauto/profiles";
    private static final String ACTIVE_PROFILE_FILE = "config/bapelauto/active_profile.txt";
    
    private final ShardedConfigManager configManager;
    private String currentProfile = "default";
    private final Map<String, Profile> loadedProfiles = new HashMap<>();
    
    public ProfileManager(ShardedConfigManager configManager) {
        this.configManager = configManager;
        initialize();
    }
    
    private void initialize() {
        try {
            Files.createDirectories(Paths.get(PROFILES_DIR));
            loadActiveProfile();
            scanProfiles();
        } catch (Exception e) {
            System.err.println("[ProfileManager] Init failed: " + e.getMessage());
        }
    }
    
    private void loadActiveProfile() {
        Path activePath = Paths.get(ACTIVE_PROFILE_FILE);
        if (Files.exists(activePath)) {
            try {
                currentProfile = Files.readString(activePath).trim();
            } catch (IOException e) {
                currentProfile = "default";
            }
        }
    }
    
    private void saveActiveProfile() {
        try {
            Files.writeString(Paths.get(ACTIVE_PROFILE_FILE), currentProfile);
        } catch (IOException e) {
            System.err.println("[ProfileManager] Failed to save active profile");
        }
    }
    
    public void saveProfile(String profileName, String description) {
        try {
            Profile profile = new Profile(profileName, description);
            profile.config = configManager.getAllConfig();
            
            Path profilePath = Paths.get(PROFILES_DIR, profileName + ".json");
            String json = serializeProfile(profile);
            Files.writeString(profilePath, json);
            
            loadedProfiles.put(profileName, profile);
            
            System.out.println("[ProfileManager] Saved profile: " + profileName);
        } catch (Exception e) {
            System.err.println("[ProfileManager] Save failed: " + e.getMessage());
        }
    }
    
    public boolean loadProfile(String profileName, MinecraftClient client) {
        try {
            Path profilePath = Paths.get(PROFILES_DIR, profileName + ".json");
            if (!Files.exists(profilePath)) {
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§c[Profile] Not found: " + profileName), false);
                }
                return false;
            }
            
            String json = Files.readString(profilePath);
            Profile profile = deserializeProfile(json);
            
            // Apply config
            profile.config.forEach(configManager::set);
            configManager.saveConfig();
            
            currentProfile = profileName;
            saveActiveProfile();
            loadedProfiles.put(profileName, profile);
            
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§a[Profile] Loaded: " + profileName), true);
                client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.0F, 1.5F);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("[ProfileManager] Load failed: " + e.getMessage());
            return false;
        }
    }
    
    public void deleteProfile(String profileName, MinecraftClient client) {
        try {
            Path profilePath = Paths.get(PROFILES_DIR, profileName + ".json");
            Files.deleteIfExists(profilePath);
            loadedProfiles.remove(profileName);
            
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§e[Profile] Deleted: " + profileName), false);
            }
        } catch (Exception e) {
            System.err.println("[ProfileManager] Delete failed: " + e.getMessage());
        }
    }
    
    public void scanProfiles() {
        loadedProfiles.clear();
        try (var stream = Files.list(Paths.get(PROFILES_DIR))) {
            stream.filter(p -> p.toString().endsWith(".json"))
                  .forEach(path -> {
                      try {
                          String json = Files.readString(path);
                          Profile profile = deserializeProfile(json);
                          loadedProfiles.put(profile.name, profile);
                      } catch (Exception e) {
                          System.err.println("[ProfileManager] Failed to load: " + path.getFileName());
                      }
                  });
        } catch (Exception e) {
            System.err.println("[ProfileManager] Scan failed: " + e.getMessage());
        }
    }
    
    public List<Profile> getAvailableProfiles() {
        return new ArrayList<>(loadedProfiles.values());
    }
    
    public String getCurrentProfile() {
        return currentProfile;
    }
    
    // Quick switch profiles (cycle through)
    public void cycleProfile(MinecraftClient client) {
        List<Profile> profiles = getAvailableProfiles();
        if (profiles.isEmpty()) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§c[Profile] No profiles available"), false);
            }
            return;
        }
        
        int currentIndex = -1;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).name.equals(currentProfile)) {
                currentIndex = i;
                break;
            }
        }
        
        int nextIndex = (currentIndex + 1) % profiles.size();
        loadProfile(profiles.get(nextIndex).name, client);
    }
    
    private String serializeProfile(Profile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"name\": \"").append(profile.name).append("\",\n");
        sb.append("  \"description\": \"").append(profile.description).append("\",\n");
        sb.append("  \"created\": ").append(profile.createdTime).append(",\n");
        sb.append("  \"modified\": ").append(profile.modifiedTime).append(",\n");
        sb.append("  \"config\": {\n");
        
        List<Map.Entry<String, String>> entries = new ArrayList<>(profile.config.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<String, String> entry = entries.get(i);
            sb.append("    \"").append(entry.getKey()).append("\": \"")
              .append(entry.getValue()).append("\"");
            if (i < entries.size() - 1) sb.append(",");
            sb.append("\n");
        }
        
        sb.append("  }\n");
        sb.append("}\n");
        return sb.toString();
    }
    
    private Profile deserializeProfile(String json) {
        // Simple JSON parser (for production, use a proper JSON library)
        Profile profile = new Profile("", "");
        
        String[] lines = json.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("\"name\":")) {
                profile.name = extractValue(line);
            } else if (line.startsWith("\"description\":")) {
                profile.description = extractValue(line);
            } else if (line.startsWith("\"created\":")) {
                profile.createdTime = Long.parseLong(extractValue(line));
            } else if (line.startsWith("\"modified\":")) {
                profile.modifiedTime = Long.parseLong(extractValue(line));
            } else if (line.contains(": \"") && !line.startsWith("\"config\":") && !line.startsWith("\"name\":") && !line.startsWith("\"description\":")) {
                String[] parts = line.split(": \"");
                if (parts.length == 2) {
                    String key = parts[0].replace("\"", "").trim();
                    String value = parts[1].replace("\"", "").replace(",", "").trim();
                    profile.config.put(key, value);
                }
            }
        }
        
        return profile;
    }
    
    private String extractValue(String line) {
        int start = line.indexOf('"', line.indexOf(':')) + 1;
        int end = line.lastIndexOf('"');
        if (start > 0 && end > start) {
            return line.substring(start, end);
        }
        // For numbers
        String[] parts = line.split(": ");
        if (parts.length == 2) {
            return parts[1].replace(",", "").trim();
        }
        return "";
    }
    
    public static class Profile {
        public String name;
        public String description;
        public long createdTime;
        public long modifiedTime;
        public Map<String, String> config;
        
        public Profile(String name, String description) {
            this.name = name;
            this.description = description;
            this.createdTime = System.currentTimeMillis();
            this.modifiedTime = System.currentTimeMillis();
            this.config = new HashMap<>();
        }
        
        @Override
        public String toString() {
            return name + " - " + description;
        }
    }
}