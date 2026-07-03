// ============================================
// FILE: InventoryScreenMixin.java (FIXED)
// Path: src/main/java/com/bapelauto/mixin/InventoryScreenMixin.java
// ============================================
package com.bapelauto.mixin;

import com.bapelauto.AutoBotMod;
import com.bapelauto.inventory.InventoryManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class InventoryScreenMixin extends Screen {

    @Shadow protected int x; 
    @Shadow protected int y; 
    @Shadow protected int backgroundWidth; 
    @Shadow protected int backgroundHeight;
    
    protected InventoryScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init()V") 
    private void bapelauto_injectButtons(CallbackInfo ci) {
        // Check if GUI buttons should be shown
        if (!AutoBotMod.isShowGuiButtons()) return;
        
        if (this.client == null || this.client.player == null) return;
        
        int xPos = this.x + this.backgroundWidth + 5; 
        int yPos = this.y + 5;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("§aSteal"), b -> {
            InventoryManager.performSingleSteal(this.client);
        }).dimensions(xPos, yPos, 50, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("§bStore"), b -> {
            InventoryManager.performSingleStore(this.client);
        }).dimensions(xPos, yPos + 25, 50, 20).build());
    }
}
