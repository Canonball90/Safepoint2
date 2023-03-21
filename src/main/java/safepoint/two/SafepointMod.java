package safepoint.two;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import safepoint.two.guis.mainmenu.onGuiOpenEvent;

@Mod(modid = SafepointMod.MOD_ID, name = SafepointMod.MOD_NAME, version = SafepointMod.VERSION)
public class SafepointMod {

    public static final String MOD_ID = "safepoint";
    public static final String MOD_NAME = "Safepoint";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Safepoint.INSTANCE.init();
    }

//    @Mod.EventHandler
//    public void postInit(FMLPostInitializationEvent event){
//        MinecraftForge.EVENT_BUS.register(new onGuiOpenEvent());
//    }
}
