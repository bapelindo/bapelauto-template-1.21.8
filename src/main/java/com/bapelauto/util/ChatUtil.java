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
// CONFIRMED: net.minecraft.client.multiplayer.chat.GuiMessageSource is an
// enum with exactly PLAYER, SYSTEM_SERVER, SYSTEM_CLIENT (real source
// pasted by the user). This message is generated locally by the mod, not
// relayed from the server, so SYSTEM_CLIENT is the correct source - not a
// guess. null is passed for MessageSignature/GuiMessageTag since this isn't
// a signed player message and needs no badge.
//
// STILL BROKEN: the compiler now reports that exact 4-arg addMessage
// overload is PRIVATE in ChatComponent, so there must be a different public
// entry point for outside callers to add a chat message. GuiMessageTag's
// import was also wrong (net.minecraft.client.GuiMessageTag doesn't exist;
// moved to net.minecraft.client.multiplayer.chat.GuiMessageTag, guessed by
// analogy with GuiMessageSource's real location - unconfirmed). Need
// ChatComponent's real source to find the actual public addMessage method.
// ============================================
package com.bapelauto.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;

public final class ChatUtil {
    private ChatUtil() {}

    public static void displayClientMessage(Minecraft client, Component message, boolean actionBar) {
        if (client.gui == null) return;
        if (actionBar) {
            client.gui.setOverlayMessage(message, false);
        } else {
            client.gui.getChat().addMessage(message, null, GuiMessageSource.SYSTEM_CLIENT, null);
        }
    }
}
