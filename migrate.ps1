# ================== FQN (import path lengkap) — proses dulu ==================
$fqnReplacements = [ordered]@{
    'net\.minecraft\.client\.gui\.screen\.ingame\.HandledScreen' = 'net.minecraft.client.gui.screens.inventory.AbstractContainerScreen'
    'net\.minecraft\.client\.gui\.screen\.Screen'                = 'net.minecraft.client.gui.screens.Screen'
    'net\.minecraft\.client\.gui\.widget\.ButtonWidget'          = 'net.minecraft.client.gui.components.Button'
    'net\.minecraft\.client\.gui\.widget\.TextFieldWidget'       = 'net.minecraft.client.gui.components.EditBox'
    'net\.minecraft\.client\.gui\.DrawContext'                   = 'net.minecraft.client.gui.GuiGraphics'
    'net\.minecraft\.client\.option\.KeyBinding'                 = 'net.minecraft.client.KeyMapping'
    'net\.minecraft\.client\.util\.InputUtil'                    = 'net.minecraft.client.InputConstants'
    'net\.minecraft\.client\.MinecraftClient'                    = 'net.minecraft.client.Minecraft'
    'net\.minecraft\.screen\.slot\.SlotActionType'               = 'net.minecraft.world.inventory.ClickType'
    'net\.minecraft\.screen\.slot\.Slot'                         = 'net.minecraft.world.inventory.Slot'
    'net\.minecraft\.screen\.ScreenHandler'                      = 'net.minecraft.world.inventory.AbstractContainerMenu'
    'net\.minecraft\.entity\.player\.PlayerEntity'               = 'net.minecraft.world.entity.player.Player'
    'net\.minecraft\.util\.hit\.BlockHitResult'                  = 'net.minecraft.world.phys.BlockHitResult'
    'net\.minecraft\.util\.hit\.EntityHitResult'                 = 'net.minecraft.world.phys.EntityHitResult'
    'net\.minecraft\.util\.hit\.HitResult'                       = 'net.minecraft.world.phys.HitResult'
    'net\.minecraft\.util\.Hand'                                 = 'net.minecraft.world.InteractionHand'
    'net\.minecraft\.item\.ItemStack'                            = 'net.minecraft.world.item.ItemStack'
    'net\.minecraft\.text\.Text'                                 = 'net.minecraft.network.chat.Component'
    'net\.minecraft\.sound\.SoundEvents'                         = 'net.minecraft.sounds.SoundEvents'
}

# ================== Bare class name (whole word, huruf besar di awal) ==================
$bareReplacements = [ordered]@{
    'HandledScreen'   = 'AbstractContainerScreen'
    'ButtonWidget'    = 'Button'
    'TextFieldWidget' = 'EditBox'
    'DrawContext'     = 'GuiGraphics'
    'KeyBinding'      = 'KeyMapping'
    'InputUtil'       = 'InputConstants'
    'MinecraftClient' = 'Minecraft'
    'SlotActionType'  = 'ClickType'
    'ScreenHandler'   = 'AbstractContainerMenu'
    'PlayerEntity'    = 'Player'
    'MutableText'     = 'MutableComponent'
    'Formatting'      = 'ChatFormatting'
    'Hand'            = 'InteractionHand'
    'Text'            = 'Component'
}

# ================== Field/method rename di dalam Screen subclass ==================
$memberReplacements = [ordered]@{
    'this\.client\b'      = 'this.minecraft'
    'textRenderer'        = 'font'
    'clearChildren\(\)'   = 'clearWidgets()'
    'addDrawableChild\('  = 'addRenderableWidget('
    '\.dimensions\('      = '.bounds('
}

$files = Get-ChildItem -Path "src\main\java" -Filter *.java -Recurse
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $original = $content

    foreach ($pattern in $fqnReplacements.Keys) {
        $content = $content -creplace $pattern, $fqnReplacements[$pattern]
    }
    foreach ($pattern in $bareReplacements.Keys) {
        $content = $content -creplace "\b$pattern\b", $bareReplacements[$pattern]
    }
    foreach ($pattern in $memberReplacements.Keys) {
        $content = $content -creplace $pattern, $memberReplacements[$pattern]
    }

    if ($content -ne $original) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        Write-Host "Updated: $($file.Name)"
    }
}
Write-Host "`nSelesai. Jalankan 'git diff' untuk review sebelum compile."