// ============================================
// FILE: InventoryScreenMixin.java (FIXED)
// Path: src/main/java/com/bapelauto/mixin/InventoryScreenMixin.java
// ============================================
package com.bapelauto.mixin;

import com.bapelauto.AutoBotMod;
import com.bapelauto.inventory.InventoryManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class InventoryScreenMixin extends Screen {

    @Shadow protected int x; 
    @Shadow protected int y; 
    @Shadow protected int backgroundWidth; 
    @Shadow protected int backgroundHeight;
    
    protected InventoryScreenMixin(Component title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init()V") 
    private void bapelauto_injectButtons(CallbackInfo ci) {
        // Check if GUI buttons should be shown
        if (!AutoBotMod.isShowGuiButtons()) return;
        
        if (this.minecraft == null || this.minecraft.player == null) return;
        
        int xPos = this.x + this.backgroundWidth + 5; 
        int yPos = this.y + 5;

        this.addRenderableWidget(Button.builder(Component.literal("§aSteal"), b -> {
            InventoryManager.performSingleSteal(this.minecraft);
        }).bounds(xPos, yPos, 50, 20).build());
        
        this.addRenderableWidget(Button.builder(Component.literal("§bStore"), b -> {
            InventoryManager.performSingleStore(this.minecraft);
        }).bounds(xPos, yPos + 25, 50, 20).build());
    }
}
