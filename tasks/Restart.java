package ains.tasks;

import ains.world.World;
//import it.gotoandplay.smartfoxserver.SmartFoxServer;
import ains.network.sfs.User;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static ains.network.manager.UserManager.userManager;

public class Restart implements Runnable, CancellableTask {

    private final World world;
    private final User user;
    private ScheduledFuture<?> running;
    private boolean cancelled;

    public Restart(World world, User user) {
        this.world = world;
        this.user = user;
        this.cancelled = false;
    }

    @Override
    public void run() {
        try {
            for (int i = 5; i > 0; i--)
            {
                if (cancelled)
                {
                    world.shutdown = null;
                    System.out.println("Shutdown task was cancelled. 1");
                    return;
                }

                world.send(new String[]{ (i == 5 ? "server" : "warning"), "Server restarting in " + i + " minutes. Please logout to prevent data loss." }, userManager().getChannelList());
                Thread.sleep(TimeUnit.MINUTES.toMillis(1));
            }

            this.world.send(new String[]{"logoutWarning", "", "60"}, userManager().getChannelList());
            this.world.shutdown();
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            Runtime.getRuntime().exec("cmd /c start start.bat");
            System.exit(0);
        } catch (IOException | InterruptedException ignored) {
        }
    }

    @Override
    public void cancel() {
        this.cancelled = true;
        this.running.cancel(false);
    }

    @Override
    public void setRunning(ScheduledFuture<?> running) {
        this.running = running;
    }

}
