// Path: src/main/java/com/bapelauto/SessionManager.java
package com.bapelauto;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages session sharding and prevents conflicts between multiple Minecraft instances
 */
public class SessionManager {
    
    private static final String SESSION_DIR = "config/bapelauto/sessions";
    private static final String LOCK_FILE = "config/bapelauto/.session.lock";
    private static final String REGISTRY_FILE = "config/bapelauto/session_registry.json";
    private static final long SESSION_TIMEOUT = 30000; // 30 detik timeout
    private static final long CLEANUP_INTERVAL = 5000; // 5 detik cleanup interval
    
    private final String sessionId;
    private final Path sessionFile;
    private final Path lockFile;
    private FileLock fileLock;
    private FileChannel lockChannel;
    private Thread cleanupThread;
    private volatile boolean running;
    
    // Session metadata
    private long sessionStartTime;
    private long lastHeartbeat;
    private String realmName;
    private Map<String, Object> sessionData;
    
    public SessionManager() {
        this.sessionId = generateSessionId();
        this.sessionFile = Paths.get(SESSION_DIR, sessionId + ".session");
        this.lockFile = Paths.get(LOCK_FILE);
        this.sessionData = new ConcurrentHashMap<>();
        this.sessionStartTime = System.currentTimeMillis();
        this.lastHeartbeat = System.currentTimeMillis();
        
        initialize();
    }
    
    private void initialize() {
        try {
            // Create directories
            Files.createDirectories(Paths.get(SESSION_DIR));
            Files.createDirectories(lockFile.getParent());
            
            // Acquire file lock
            acquireLock();
            
            // Clean up stale sessions
            cleanupStaleSessions();
            
            // Register this session
            registerSession();
            
            // Start heartbeat thread
            startHeartbeat();
            
            // Add shutdown hook
            addShutdownHook();
            
            System.out.println("[SessionManager] Initialized with ID: " + sessionId);
            
        } catch (Exception e) {
            System.err.println("[SessionManager] Initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + 
               UUID.randomUUID().toString().substring(0, 8);
    }
    
    private void acquireLock() throws IOException {
        lockChannel = FileChannel.open(
            lockFile,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        );
        
        fileLock = lockChannel.tryLock();
        if (fileLock == null) {
            System.out.println("[SessionManager] Lock already held by another instance");
            // Continue anyway - we use soft locking
        }
    }
    
    private void registerSession() throws IOException {
        SessionInfo info = new SessionInfo(
            sessionId,
            sessionStartTime,
            lastHeartbeat,
            ProcessHandle.current().pid(),
            realmName
        );
        
        Files.write(sessionFile, serializeSession(info));
        updateRegistry();
        
        System.out.println("[SessionManager] Session registered: " + sessionId);
    }
    
    private void updateRegistry() throws IOException {
        Path registryPath = Paths.get(REGISTRY_FILE);
        List<SessionInfo> activeSessions = getActiveSessions();
        
        // Add current session
        activeSessions.add(new SessionInfo(
            sessionId,
            sessionStartTime,
            lastHeartbeat,
            ProcessHandle.current().pid(),
            realmName
        ));
        
        // Write registry
        String json = serializeRegistry(activeSessions);
        Files.write(registryPath, json.getBytes());
    }
    
    private void startHeartbeat() {
        running = true;
        cleanupThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(CLEANUP_INTERVAL);
                    
                    // Update heartbeat
                    lastHeartbeat = System.currentTimeMillis();
                    updateSessionFile();
                    
                    // Clean stale sessions
                    cleanupStaleSessions();
                    
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("[SessionManager] Heartbeat error: " + e.getMessage());
                }
            }
        }, "SessionManager-Heartbeat");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }
    
    private void updateSessionFile() {
        try {
            SessionInfo info = new SessionInfo(
                sessionId,
                sessionStartTime,
                lastHeartbeat,
                ProcessHandle.current().pid(),
                realmName
            );
            Files.write(sessionFile, serializeSession(info));
        } catch (IOException e) {
            System.err.println("[SessionManager] Failed to update session file: " + e.getMessage());
        }
    }
    
    private void cleanupStaleSessions() {
        try {
            Path sessionDir = Paths.get(SESSION_DIR);
            if (!Files.exists(sessionDir)) return;
            
            long currentTime = System.currentTimeMillis();
            
            Files.list(sessionDir)
                .filter(p -> p.toString().endsWith(".session"))
                .forEach(sessionPath -> {
                    try {
                        SessionInfo info = deserializeSession(Files.readAllBytes(sessionPath));
                        
                        // Check if session is stale
                        if (currentTime - info.lastHeartbeat > SESSION_TIMEOUT) {
                            // Verify process is not running
                            if (!isProcessAlive(info.pid)) {
                                Files.deleteIfExists(sessionPath);
                                System.out.println("[SessionManager] Cleaned stale session: " + info.sessionId);
                            }
                        }
                    } catch (Exception e) {
                        // Corrupted session file - delete it
                        try {
                            Files.deleteIfExists(sessionPath);
                        } catch (IOException ex) {
                            // Ignore
                        }
                    }
                });
            
            updateRegistry();
            
        } catch (Exception e) {
            System.err.println("[SessionManager] Cleanup error: " + e.getMessage());
        }
    }
    
    private boolean isProcessAlive(long pid) {
        try {
            return ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
    
    private List<SessionInfo> getActiveSessions() {
        List<SessionInfo> sessions = new ArrayList<>();
        
        try {
            Path sessionDir = Paths.get(SESSION_DIR);
            if (!Files.exists(sessionDir)) return sessions;
            
            long currentTime = System.currentTimeMillis();
            
            sessions = Files.list(sessionDir)
                .filter(p -> p.toString().endsWith(".session"))
                .map(path -> {
                    try {
                        SessionInfo info = deserializeSession(Files.readAllBytes(path));
                        // Only return if not stale
                        if (currentTime - info.lastHeartbeat <= SESSION_TIMEOUT) {
                            return info;
                        }
                    } catch (Exception e) {
                        // Skip corrupted files
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("[SessionManager] Failed to get active sessions: " + e.getMessage());
        }
        
        return sessions;
    }
    
    public void updateRealmName(String newRealmName) {
        this.realmName = newRealmName;
        updateSessionFile();
    }
    
    public void setSessionData(String key, Object value) {
        sessionData.put(key, value);
    }
    
    public Object getSessionData(String key) {
        return sessionData.get(key);
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public List<SessionInfo> listActiveSessions() {
        return getActiveSessions();
    }
    
    public boolean isOnlyActiveSession() {
        return getActiveSessions().size() == 1;
    }
    
    public void shutdown() {
        System.out.println("[SessionManager] Shutting down session: " + sessionId);
        running = false;
        
        if (cleanupThread != null) {
            cleanupThread.interrupt();
        }
        
        // Clean up session file
        try {
            Files.deleteIfExists(sessionFile);
            updateRegistry();
        } catch (IOException e) {
            System.err.println("[SessionManager] Failed to delete session file: " + e.getMessage());
        }
        
        // Release lock
        try {
            if (fileLock != null) {
                fileLock.release();
            }
            if (lockChannel != null) {
                lockChannel.close();
            }
        } catch (IOException e) {
            System.err.println("[SessionManager] Failed to release lock: " + e.getMessage());
        }
        
        System.out.println("[SessionManager] Session terminated: " + sessionId);
    }
    
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }, "SessionManager-Shutdown"));
    }
    
    // Simple serialization (could use JSON library for production)
    private byte[] serializeSession(SessionInfo info) {
        String data = String.format("%s|%d|%d|%d|%s",
            info.sessionId,
            info.startTime,
            info.lastHeartbeat,
            info.pid,
            info.realmName != null ? info.realmName : ""
        );
        return data.getBytes();
    }
    
    private SessionInfo deserializeSession(byte[] data) {
        String str = new String(data);
        String[] parts = str.split("\\|");
        
        return new SessionInfo(
            parts[0],
            Long.parseLong(parts[1]),
            Long.parseLong(parts[2]),
            Long.parseLong(parts[3]),
            parts.length > 4 ? parts[4] : null
        );
    }
    
    private String serializeRegistry(List<SessionInfo> sessions) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"sessions\": [\n");
        
        for (int i = 0; i < sessions.size(); i++) {
            SessionInfo s = sessions.get(i);
            sb.append("    {\n");
            sb.append("      \"id\": \"").append(s.sessionId).append("\",\n");
            sb.append("      \"startTime\": ").append(s.startTime).append(",\n");
            sb.append("      \"lastHeartbeat\": ").append(s.lastHeartbeat).append(",\n");
            sb.append("      \"pid\": ").append(s.pid).append(",\n");
            sb.append("      \"realm\": \"").append(s.realmName != null ? s.realmName : "").append("\"\n");
            sb.append("    }");
            if (i < sessions.size() - 1) sb.append(",");
            sb.append("\n");
        }
        
        sb.append("  ],\n");
        sb.append("  \"lastUpdate\": ").append(System.currentTimeMillis()).append("\n");
        sb.append("}\n");
        
        return sb.toString();
    }
    
    // Inner class for session info
    public static class SessionInfo {
        public final String sessionId;
        public final long startTime;
        public final long lastHeartbeat;
        public final long pid;
        public final String realmName;
        
        public SessionInfo(String sessionId, long startTime, long lastHeartbeat, long pid, String realmName) {
            this.sessionId = sessionId;
            this.startTime = startTime;
            this.lastHeartbeat = lastHeartbeat;
            this.pid = pid;
            this.realmName = realmName;
        }
        
        public boolean isAlive() {
            return System.currentTimeMillis() - lastHeartbeat <= SESSION_TIMEOUT;
        }
        
        @Override
        public String toString() {
            return String.format("Session[id=%s, pid=%d, realm=%s, alive=%b]",
                sessionId, pid, realmName, isAlive());
        }
    }
}