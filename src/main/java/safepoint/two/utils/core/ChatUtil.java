package safepoint.two.utils.core;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.TextComponentString;

import static safepoint.two.Safepoint.mc;

public class ChatUtil {

    public static void send(String message){
        mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString("[SafePoint] " + message), 1);
    }
}
