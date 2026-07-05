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
//   - actionBar == false -> Minecraft.gui.getChat().addClientSystemMessage(message)
//
// CONFIRMED by the real compiler: client.gui, gui.setOverlayMessage(...) and
// gui.getChat() all resolve correctly.
//
// CONFIRMED (real ChatComponent source pasted by the user): the 4-arg
// addMessage(Component, MessageSignature, GuiMessageSource, GuiMessageTag)
// is private. The public entry point for a locally generated message is
// ChatComponent.addClientSystemMessage(Component), which internally calls
// addMessage(message, null, GuiMessageSource.SYSTEM_CLIENT,
// GuiMessageTag.systemSinglePlayer()) - exactly the semantics this class
// needs, so no manual GuiMessageSource/GuiMessageTag handling is needed here
// at all.
// ============================================
package com.bapelauto.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ChatUtil {
    private ChatUtil() {}

    public static void displayClientMessage(Minecraft client, Component message, boolean actionBar) {
        if (client.gui == null) return;
        if (actionBar) {
            client.gui.setOverlayMessage(message, false);
        } else {
            client.gui.getChat().addClientSystemMessage(message);
        }
    }
}
