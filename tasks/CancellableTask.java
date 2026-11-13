//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ains.tasks;

import java.util.concurrent.ScheduledFuture;

public interface CancellableTask {
	
    void setRunning(ScheduledFuture<?> running);

    void cancel();
}
