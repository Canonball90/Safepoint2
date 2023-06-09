package safepoint.two.mixin.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import safepoint.two.Safepoint;
import safepoint.two.core.event.events.DisplayGuiScreenEvent;
import safepoint.two.core.event.events.LeftClickBlockEvent;
import safepoint.two.core.event.events.RootEvent;
import safepoint.two.guis.mainmenu.MainMenu;
import safepoint.two.utils.world.BlockUtil;

import javax.annotation.Nullable;
import java.util.Objects;

import static safepoint.two.Safepoint.mc;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Redirect(method = {"run"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V"))
    public void onCrashReport(Minecraft minecraft, CrashReport crashReport) {
        this.saveNekoConfiguration();
    }

    @Inject(method = {"shutdown"}, at = @At(value = "HEAD"))
    public void shutdown(CallbackInfo info) {
        this.saveNekoConfiguration();
    }

    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    public void displayGuiScreen(GuiScreen guiScreenIn, CallbackInfo info) {
        DisplayGuiScreenEvent.Closed closeEvent = new DisplayGuiScreenEvent.Closed(Minecraft.getMinecraft().currentScreen);
        MinecraftForge.EVENT_BUS.post(closeEvent);
        DisplayGuiScreenEvent.Displayed displayEvent = new DisplayGuiScreenEvent.Displayed(guiScreenIn);
        MinecraftForge.EVENT_BUS.post(displayEvent);

        if (guiScreenIn instanceof GuiMainMenu) {
            this.displayGuiScreen(new MainMenu());
        }
    }

    @Inject(method={"runGameLoop"}, at={@At(value="HEAD")})
    private void onRunGameLoop(CallbackInfo callbackInfo) {
        RootEvent event = new RootEvent();
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Shadow
    public abstract void displayGuiScreen(@Nullable GuiScreen var1);

    public void saveNekoConfiguration() {
        Objects.requireNonNull(Safepoint.configInitializer).save();
    }
}
