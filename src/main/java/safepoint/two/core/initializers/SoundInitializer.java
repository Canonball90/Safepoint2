package safepoint.two.core.initializers;

import net.minecraft.client.audio.ISound;
import safepoint.two.core.Core;
import safepoint.two.utils.core.songs.BackInBlood;
import safepoint.two.utils.core.songs.HardToChoose;
import safepoint.two.utils.core.songs.Zooties;

import java.util.*;

import static safepoint.two.Safepoint.mc;

public class SoundInitializer extends Core {

    Random random = new Random();

    private final List<ISound> songs = Arrays.asList(
            Zooties.sound,
            HardToChoose.sound
    );

    private final ISound menuSong;
    private ISound currentSong;

    public SoundInitializer() {
        super("SoundInitializer");
        this.menuSong = this.getRandomSong();
        this.currentSong = this.getRandomSong();
    }

    public ISound getMenuSong() {
        return this.menuSong;
    }

    public void skip() {
        boolean flag = isCurrentSongPlaying();
        if (flag) {
            this.stop();
        }
        this.currentSong = songs.get((songs.indexOf(currentSong) + 1) % songs.size());
        if (flag) {
            this.play();
        }
    }

    public void play() {
        if (!this.isCurrentSongPlaying()) {
            mc.soundHandler.playSound(currentSong);
        }
    }

    public void stop() {
        if (this.isCurrentSongPlaying()) {
            mc.soundHandler.stopSound(currentSong);
        }
    }

    private boolean isCurrentSongPlaying() {
        return mc.soundHandler.isSoundPlaying(currentSong);
    }

    public void shuffle() {
        this.stop();
        Collections.shuffle(this.songs);
    }

    private ISound getRandomSong() {
        return songs.get(random.nextInt(songs.size()));
    }

}
