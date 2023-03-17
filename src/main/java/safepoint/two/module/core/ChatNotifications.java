package safepoint.two.module.core;

import com.mojang.realmsclient.gui.ChatFormatting;
import safepoint.two.core.event.events.DeathEvent;
import safepoint.two.core.event.events.ModuleToggleEvent;
import safepoint.two.core.event.events.PacketEvent;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.BooleanSetting;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import safepoint.two.utils.core.ChatUtil;

@ModuleInfo(name = "Chat Notifications", category = Module.Category.Core, description = "Send Notifications in chat when certain things happen.")
public class ChatNotifications extends Module {
    public BooleanSetting modules = new BooleanSetting("Modules", false, this);
 //   public BooleanSetting totemPops = new BooleanSetting("Totem Pops", false, this);
    public BooleanSetting deaths = new BooleanSetting("Deaths", false, this);

    @SubscribeEvent
    public void onModuleEnableEvent(ModuleToggleEvent.Enable event){
        if(modules.getValue())
            ChatUtil.send(ChatFormatting.BOLD + event.getModule().getName() + ChatFormatting.RESET + " has been " + ChatFormatting.GREEN + "Enabled" + ChatFormatting.RESET + ".");
    }

    @SubscribeEvent
    public void onModuleDisableEvent(ModuleToggleEvent.Disable event){
        if(modules.getValue())
            ChatUtil.send(ChatFormatting.BOLD + event.getModule().getName() + ChatFormatting.RESET + " has been " + ChatFormatting.RED + "Disabled" + ChatFormatting.RESET + ".");
    }

    @SubscribeEvent
    public void onDeathEvent(DeathEvent event){
        if(deaths.getValue())
            ChatUtil.send(ChatFormatting.BOLD + event.getPlayer().getName() + ChatFormatting.RESET + " has just died.");

    }
}
