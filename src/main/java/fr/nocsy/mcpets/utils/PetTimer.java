package fr.nocsy.mcpets.utils;

import fr.nocsy.mcpets.MCPets;
import lombok.Getter;

import java.util.HashMap;

public class PetTimer {

    @Getter
    private static HashMap<PetTimer, Object> runningTimers = new HashMap<>();

    @Getter
    private int cooldown;
    @Getter
    private int remainingTime;
    private long frequency;

    private final Runnable endingRunnable;

    /**
     * Constructor
     * Frequency giving the tick when repeating the task
     */
    public PetTimer(int cooldown, long frequency, Runnable endingRunnable) {
        this.cooldown = cooldown;
        this.remainingTime = 0;
        this.frequency = frequency;
        this.endingRunnable = endingRunnable;
    }

    public void launch(Runnable runnable) {
        if (isRunning())
            stop(null);
        remainingTime = cooldown;

        Runnable taskLogic = () -> {
            if (cooldown != Integer.MAX_VALUE)
                remainingTime--;
            if (remainingTime <= 0)
                stop(endingRunnable);

            if (runnable != null)
                runnable.run();
        };

        Object task = FoliaCompat.runGlobalTimer(taskLogic, 1L, Math.max(1L, frequency));
        runningTimers.put(this, task);
    }

    public void stop(Runnable runnable) {
        Object task = runningTimers.get(this);
        FoliaCompat.cancel(task);
        runningTimers.remove(this);
        remainingTime = 0;
        if (runnable != null)
            runnable.run();
    }

    public boolean isRunning() {
        return remainingTime > 0;
    }
}
