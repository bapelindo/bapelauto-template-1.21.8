// Path: src/main/java/com/bapelauto/ShardedConfigManager.java
package com.bapelauto;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manages sharded configuration to prevent conflicts between multiple instances
 */
public class ShardedConfigManager {
    
    private static final String SHARD_DIR = "config/bapelauto/shards";
    private static final String GLOBAL_CONFIG = "config/bapelauto.properties";
    private static final String BACKUP_DIR = "config/bapelauto/backups";
    
    private final SessionManager sessionManager;
    private final String shardId;
    private final Path shardConfigPath;
    private final Path globalConfigPath;
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, String> configCache = new ConcurrentHashMap<>();
    
    private long lastLoadTime = 0;
    private static final long RELOAD_INTERVAL = 5000; // 5 detik
    
    public ShardedConfigManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.shardId = sessionManager.getSessionId();
        this.shardConfigPath = Paths.get(SHARD_DIR, shardId + ".properties");
        this.globalConfigPath = Paths.get(GLOBAL_CONFIG);
        
        initialize();
    }
    
    private void initialize() {
        try {
            // Create directories
            Files.createDirectories(shardConfigPath.getParent());
            Files.createDirectories(Paths.get(BACKUP_DIR));
            
            // Load initial config
            loadConfig();
            
            System.out.println("[ShardedConfig] Initialized shard: " + shardId);
            
        } catch (Exception e) {
            System.err.println("[ShardedConfig] Initialization failed: " + e.getMessage());
        }
    }
    
    /**
     * Load configuration with fallback strategy:
     * 1. Try shard-specific config
     * 2. Fall back to global config
     * 3. Use defaults
     */
    public void loadConfig() {
        lock.writeLock().lock();
        try {
            configCache.clear();
            
            // Try shard config first
            if (Files.exists(shardConfigPath)) {
                loadFromFile(shardConfigPath);
                System.out.println("[ShardedConfig] Loaded shard config: " + shardId);
            }
            // Fall back to global
            else if (Files.exists(globalConfigPath)) {
                loadFromFile(globalConfigPath);
                System.out.println("[ShardedConfig] Loaded global config (no shard found)");
            }
            // Use defaults
            else {
                loadDefaults();
                System.out.println("[ShardedConfig] Loaded default config");
            }
            
            lastLoadTime = System.currentTimeMillis();
            
        } catch (Exception e) {
            System.err.println("[ShardedConfig] Load failed: " + e.getMessage());
            loadDefaults();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void loadFromFile(Path path) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        }
        
        // Copy to cache
        for (String key : props.stringPropertyNames()) {
            configCache.put(key, props.getProperty(key));
        }
    }
    
    private void loadDefaults() {
        configCache.put("leftClickEnabled", "false");
        configCache.put("rightClickEnabled", "false");
        configCache.put("autoStealEnabled", "false");
        configCache.put("autoStoreEnabled", "false");
        configCache.put("commandEnabled", "false");
        
        configCache.put("leftClickDelay", "200");
        configCache.put("rightClickDelay", "200");
        configCache.put("targetClickDelay", "100");
        configCache.put("inventoryDelay", "150");
        configCache.put("commandDelay", "60000");
        
        configCache.put("command", "/sell all");
        configCache.put("enableAutoLoad", "true");
        configCache.put("enableResetPerRealm", "true");
    }
    
    /**
     * Save config to shard-specific file
     */
    public void saveConfig() {
        lock.readLock().lock();
        try {
            // Backup existing config
            if (Files.exists(shardConfigPath)) {
                backupConfig();
            }
            
            // Save to shard file
            Properties props = new Properties();
            configCache.forEach(props::setProperty);
            
            try (OutputStream out = Files.newOutputStream(shardConfigPath)) {
                props.store(out, "AutoBot Shard Config - Session: " + shardId);
            }
            
            System.out.println("[ShardedConfig] Saved shard config: " + shardId);
            
            // Also update global config if this is the only session
            if (sessionManager.isOnlyActiveSession()) {
                saveToGlobal();
            }
            
        } catch (Exception e) {
            System.err.println("[ShardedConfig] Save failed: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private void saveToGlobal() {
        try {
            Properties props = new Properties();
            configCache.forEach(props::setProperty);
            
            try (OutputStream out = Files.newOutputStream(globalConfigPath)) {
                props.store(out, "AutoBot Global Config");
            }
            
            System.out.println("[ShardedConfig] Updated global config");
            
        } catch (Exception e) {
            System.err.println("[ShardedConfig] Failed to update global: " + e.getMessage());
        }
    }
    
    private void backupConfig() {
        try {
            String timestamp = String.format("%d", System.currentTimeMillis());
            Path backupPath = Paths.get(BACKUP_DIR, shardId + "_" + timestamp + ".properties");
            Files.copy(shardConfigPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Keep only last 10 backups
            cleanupOldBackups();
            
        } catch (Exception e) {
            System.err.println("[ShardedConfig] Backup failed: " + e.getMessage());
        }
    }
    
    private void cleanupOldBackups() throws IOException {
        List<Path> backups = new ArrayList<>();
        
        try (var stream = Files.list(Paths.get(BACKUP_DIR))) {
            stream.filter(p -> p.toString().contains(shardId))
                  .forEach(backups::add);
        }
        
        // Sort by timestamp (newest first)
        backups.sort((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString()));
        
        // Delete old backups (keep 10 most recent)
        for (int i = 10; i < backups.size(); i++) {
            Files.deleteIfExists(backups.get(i));
        }
    }
    
    /**
     * Get config value with type safety
     */
    public String getString(String key, String defaultValue) {
        maybeReload();
        lock.readLock().lock();
        try {
            return configCache.getOrDefault(key, defaultValue);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getString(key, String.valueOf(defaultValue)));
    }
    
    public long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(getString(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getString(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Set config value
     */
    public void set(String key, String value) {
        lock.writeLock().lock();
        try {
            configCache.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void set(String key, boolean value) {
        set(key, String.valueOf(value));
    }
    
    public void set(String key, long value) {
        set(key, String.valueOf(value));
    }
    
    public void set(String key, int value) {
        set(key, String.valueOf(value));
    }
    
    /**
     * Auto-reload if config changed externally
     */
    private void maybeReload() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLoadTime > RELOAD_INTERVAL) {
            try {
                if (Files.exists(shardConfigPath)) {
                    long lastModified = Files.getLastModifiedTime(shardConfigPath).toMillis();
                    if (lastModified > lastLoadTime) {
                        System.out.println("[ShardedConfig] Detected external change, reloading...");
                        loadConfig();
                    }
                }
            } catch (Exception e) {
                // Ignore reload errors
            }
        }
    }
    
    /**
     * Clean up this shard's files
     */
    public void cleanup() {
        try {
            Files.deleteIfExists(shardConfigPath);
            System.out.println("[ShardedConfig] Cleaned up shard: " + shardId);
            
            // Update global if this was the last session
            if (sessionManager.isOnlyActiveSession()) {
                saveToGlobal();
            }
            
        } catch (Exception e) {
            System.err.println("[ShardedConfig] Cleanup failed: " + e.getMessage());
        }
    }
    
    /**
     * Import from global config (useful for migration)
     */
    public void importFromGlobal() {
        lock.writeLock().lock();
        try {
            if (Files.exists(globalConfigPath)) {
                loadFromFile(globalConfigPath);
                saveConfig(); // Save to shard
                System.out.println("[ShardedConfig] Imported from global config");
            }
        } catch (Exception e) {
            System.err.println("[ShardedConfig] Import failed: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Export to global config
     */
    public void exportToGlobal() {
        saveToGlobal();
    }
    
    /**
     * Get all config as map (for display/debug)
     */
    public Map<String, String> getAllConfig() {
        lock.readLock().lock();
        try {
            return new HashMap<>(configCache);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Reset to defaults
     */
    public void resetToDefaults() {
        lock.writeLock().lock();
        try {
            configCache.clear();
            loadDefaults();
            saveConfig();
            System.out.println("[ShardedConfig] Reset to defaults");
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public String getShardId() {
        return shardId;
    }
}