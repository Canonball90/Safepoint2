package safepoint.two.core.initializers;
import net.minecraftforge.common.MinecraftForge;
import safepoint.two.core.Core;

import java.util.ArrayList;

public class FriendInitializer extends Core {
    public ArrayList<FriendPlayer> friendList = new ArrayList<>();

    public FriendInitializer() {
        super("FriendInitializer");
    }

    public void addFriend(String name){
        friendList.add(new FriendPlayer(name));
    }
    public ArrayList<FriendPlayer> getFriendList() {
        return friendList;
    }

    public boolean isFriend(String name){
        return friendList.contains(new FriendPlayer(name));
    }

    public static class FriendPlayer {
        String name;
        public FriendPlayer(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
