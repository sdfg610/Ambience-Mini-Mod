package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;
import gsto.ambience_mini.utils.GameStateChecker;
import gsto.ambience_mini.utils.PlayerStateChecker;
import javazoom.jl.player.advanced.AdvancedPlayer;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class MusicPlayerThread extends Thread
{
    public static final float MIN_GAIN = -50F;
    public static final float MAX_GAIN = 0F;
    public volatile static float realGain = 0;


    private static boolean kill = false;
    @Nullable private static AdvancedPlayer player = null;


    public MusicPlayerThread() {
        setDaemon(true);
        setName("Ambience Mini - Music Player Thread");
        start();
    }

    @Override
    public void run()
    {
        boolean playing = false;

        try {
            while (!kill)
            {
                TimeUnit.MILLISECONDS.sleep(100);
                Minecraft mc = Minecraft.getInstance();

                var inMenu = GameStateChecker.inMainMenu(mc);
                var inGame = GameStateChecker.inGame(mc);

                if (inGame)
                {
                    assert mc.player != null;
                    var dim = PlayerStateChecker.currentDimensionName(mc.player);
                    var sleep = PlayerStateChecker.isSleeping(mc.player);

                    if (PlayerStateChecker.isDead(mc.player) && !playing)
                    {
                        InputStream stream = MusicLoader.getMusicStream("Dead.m4a");

                        player = new AdvancedPlayer(stream);
                        player.play();

                        playing = true;
                    }

                    //if (player != null)
                        //player.play();
                }

            }
        }
        catch (Exception ex)
        {
            AmbienceMini.LOGGER.error("Error in MusicPlayerThread.run()", ex);
        }
    }




    public void closeMusicPlayer()
    {
        //playing = false;
        if(player != null)
            player.close();

        //currentSong = null;
        player = null;
    }

    public void kill()
    {
        try {
            closeMusicPlayer();
            interrupt();
            kill = true;
        } catch(Throwable ex) {
            AmbienceMini.LOGGER.error("Error in MusicPlayerThread.kill()", ex);
        }
    }
}
