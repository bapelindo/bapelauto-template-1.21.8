// ============================================
// FILE: Notify.java
// Path: src/main/java/com/bapelauto/util/Notify.java
//
// Consistent, less spammy status feedback for the mod, built on top of the
// already-confirmed-working ChatUtil.displayClientMessage. Everything here
// goes to the action bar (not chat) so routine status updates don't clutter
// the player's chat log, with a uniform color/icon per severity instead of
// every call site hand-rolling its own "§a[Tag] ..." string.
//
// NOTE: Minecraft has a native toast/notification popup API
// (SystemToast/ToastManager) that would look even better here, but this
// project has no confirmed real source for that API in the 26.1 build (the
// class/method names have moved before in this version - see ChatUtil's
// history). Rather than guess and risk another broken build, this sticks to
// the action bar. If you paste SystemToast's real source, this can be
// upgraded to use it.
// ============================================
package com.bapelauto.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class Notify {
    private Notify() {}

    public static void success(Minecraft client, String message) {
        ChatUtil.displayClientMessage(client, Component.literal("§a✔ " + message), true);
    }

    public static void info(Minecraft client, String message) {
        ChatUtil.displayClientMessage(client, Component.literal("§e" + message), true);
    }

    public static void warning(Minecraft client, String message) {
        ChatUtil.displayClientMessage(client, Component.literal("§6⚠ " + message), true);
    }

    public static void error(Minecraft client, String message) {
        ChatUtil.displayClientMessage(client, Component.literal("§c✖ " + message), true);
    }
}
