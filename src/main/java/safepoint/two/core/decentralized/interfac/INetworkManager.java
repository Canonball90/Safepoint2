package safepoint.two.core.decentralized.interfac;

import net.minecraft.network.Packet;

public interface INetworkManager {

    Packet<?> sendPacketNoEvent(Packet<?> packetIn);

}
