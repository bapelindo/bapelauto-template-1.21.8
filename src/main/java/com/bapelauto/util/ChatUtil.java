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
// UNVERIFIED: this could not be compiled against the real 26.1.2 jar, so the
// `gui` field name and Gui/ChatComponent method names are a best-confidence
// guess based on long-standing (pre-26.1) Mojang mappings, not a confirmed
// fix. If the build still fails here, check what your IDE's autocomplete
// offers on Minecraft.getInstance() for showing a HUD/chat message.
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
            client.gui.getChat().addMessage(message);
        }
    }
}
