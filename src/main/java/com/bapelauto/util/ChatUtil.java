// ============================================
// FILE: ChatUtil.java
// Path: src/main/java/com/bapelauto/util/ChatUtil.java
//
// Ported to Minecraft 26.1.2 / Fabric (official Mojang mappings).
//
// LocalPlayer no longer has a sendMessage/displayClientMessage convenience
// method in this build (confirmed by the compiler: "cannot find symbol" for
// both names, meaning neither exists under any signature). Displaying a
// client-side message is now done directly through the HUD, matching how
// the action bar / chat overlay have long worked in Minecraft's Gui class:
//   - actionBar == true  -> Minecraft.gui.setOverlayMessage(message, false)
//   - actionBar == false -> Minecraft.gui.getChat().addMessage(message)
//
// CONFIRMED by the real compiler: client.gui, gui.setOverlayMessage(...) and
// gui.getChat() all resolve correctly. The only remaining gap is that
// ChatComponent.addMessage in this build only has a 4-arg overload
// (Component, MessageSignature, GuiMessageSource, GuiMessageTag) - there is
// no 1-arg convenience overload anymore.
//
// UNVERIFIED (guessed, not compiler-confirmed): passing null for
// MessageSignature/GuiMessageTag (no signature/badge needed for a locally
// generated message) and GuiMessageSource.SYSTEM for the source, following
// the same SYSTEM-for-non-player-messages convention used elsewhere in
// Minecraft (e.g. ChatType.SYSTEM). If GuiMessageSource has no SYSTEM
// constant, check what values your IDE offers for that enum.
// ============================================
package com.bapelauto.util;

import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.GuiMessageSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ChatUtil {
    private ChatUtil() {}

    public static void displayClientMessage(Minecraft client, Component message, boolean actionBar) {
        if (client.gui == null) return;
        if (actionBar) {
            client.gui.setOverlayMessage(message, false);
        } else {
            client.gui.getChat().addMessage(message, null, GuiMessageSource.SYSTEM, null);
        }
    }
}
