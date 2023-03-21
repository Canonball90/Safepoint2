package safepoint.two.core.decentralized.forge;

import net.minecraft.network.Packet;

public interface INetworkManager {

    Packet<?> sendPacketNoEvent(Packet<?> packetIn);

}
