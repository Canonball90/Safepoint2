package safepoint.two.module.player;

import net.minecraft.network.play.client.CPacketPlayer;
import safepoint.two.core.module.Module;
import safepoint.two.core.module.ModuleInfo;
import safepoint.two.core.settings.impl.DoubleSetting;

@ModuleInfo(name = "VClip", description = "", category = Module.Category.Player)
public class VClip extends Module {

    double x,y,z;

    DoubleSetting height = new DoubleSetting("Height", 0.05, -20, 20, this);

    @Override
    public void onTick() {
        x = mc.player.posX;
        y = mc.player.posY;
        z = mc.player.posZ;

        mc.player.setPosition(x + 0,y + height.getValue(),z + 0);
        mc.getConnection().sendPacket(new CPacketPlayer.Position());
        super.onTick();
    }
}
