package safepoint.two;

import net.minecraft.util.ResourceLocation;
import safepoint.two.core.event.EventListener;
import safepoint.two.core.initializers.*;
import safepoint.two.guis.hud.HudComponentInitializer;
import safepoint.two.core.module.ModuleInitializer;
import safepoint.two.core.settings.SettingInitializer;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import safepoint.two.utils.core.turok.render.font.TurokFont;

import java.awt.*;
import java.util.Date;

public class Safepoint {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static Safepoint INSTANCE = new Safepoint();
    public static long time;

    public static ConfigInitializer configInitializer;
    public static EventListener eventListener;
    public static ModuleInitializer moduleInitializer;
    public static SettingInitializer settingInitializer;
    public static FriendInitializer friendInitializer;
    public static HudComponentInitializer hudComponentInitializer;
    public static RotationInitializer rotationInitializer;
    public static ServerInitializer serverInitializer;
    public static ThreadInitializer threadInitializer;
    public static SoundInitializer soundInitializer;
    public static FontInitializer fontInitializer;

    public static TurokFont OVERLAY_FONT = new TurokFont(new Font("Arial", 0, 16), true, true);

    public void init() {
        Display.setTitle("Safepoint+2 1.0");
        settingInitializer = new SettingInitializer();
        eventListener = new EventListener();
        eventListener.init(true);
        moduleInitializer = new ModuleInitializer();
        friendInitializer = new FriendInitializer();
        hudComponentInitializer = new HudComponentInitializer();
        configInitializer = new ConfigInitializer();
        configInitializer.init();
        rotationInitializer = new RotationInitializer();
        serverInitializer = new ServerInitializer();
        threadInitializer = new ThreadInitializer();
        soundInitializer = new SoundInitializer();
        fontInitializer = new FontInitializer();
        RotationInitializer.init();
        time = System.currentTimeMillis();

    }

    public static final ResourceLocation LIGHTNING_TEXTURE = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    public static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");
}
