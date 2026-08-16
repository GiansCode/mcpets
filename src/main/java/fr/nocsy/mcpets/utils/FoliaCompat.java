package fr.nocsy.mcpets.utils;

import fr.nocsy.mcpets.MCPets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Compatibility layer for Folia and Paper/Spigot schedulers.
 * Uses reflection for Folia APIs so the plugin still compiles and runs on older Paper builds.
 */
public final class FoliaCompat {

    private static final boolean IS_FOLIA;

    private static final Object GLOBAL_REGION_SCHEDULER;
    private static final Object ASYNC_SCHEDULER;
    private static final Object REGION_SCHEDULER;

    private static final Method GLOBAL_EXECUTE;
    private static final Method GLOBAL_RUN_DELAYED;
    private static final Method GLOBAL_RUN_AT_FIXED_RATE;
    private static final Method ASYNC_RUN_NOW;
    private static final Method ASYNC_RUN_DELAYED;
    private static final Method ASYNC_RUN_AT_FIXED_RATE;
    private static final Method REGION_EXECUTE;
    private static final Method REGION_RUN_DELAYED;
    private static final Method ENTITY_EXECUTE;
    private static final Method ENTITY_RUN_DELAYED;
    private static final Method ENTITY_RUN_AT_FIXED_RATE;
    private static final Method ENTITY_GET_SCHEDULER;
    private static final Method TASK_CANCEL;

    static {
        boolean folia = false;
        Object global = null;
        Object async = null;
        Object region = null;
        Method globalExecute = null;
        Method globalDelayed = null;
        Method globalFixed = null;
        Method asyncNow = null;
        Method asyncDelayed = null;
        Method asyncFixed = null;
        Method regionExecute = null;
        Method regionDelayed = null;
        Method entityExecute = null;
        Method entityDelayed = null;
        Method entityFixed = null;
        Method entityGetScheduler = null;
        Method taskCancel = null;

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;

            global = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
            async = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
            region = Bukkit.class.getMethod("getRegionScheduler").invoke(null);

            final Class<?> pluginClass = Plugin.class;
            final Class<?> runnableClass = Runnable.class;
            final Class<?> consumerClass = Consumer.class;

            globalExecute = global.getClass().getMethod("execute", pluginClass, runnableClass);
            globalDelayed = global.getClass().getMethod("runDelayed", pluginClass, consumerClass, long.class);
            globalFixed = global.getClass().getMethod("runAtFixedRate", pluginClass, consumerClass, long.class, long.class);

            asyncNow = async.getClass().getMethod("runNow", pluginClass, consumerClass);
            asyncDelayed = async.getClass().getMethod("runDelayed", pluginClass, consumerClass, long.class, TimeUnit.class);
            asyncFixed = async.getClass().getMethod("runAtFixedRate", pluginClass, consumerClass, long.class, long.class, TimeUnit.class);

            regionExecute = region.getClass().getMethod("execute", pluginClass, Location.class, runnableClass);
            regionDelayed = region.getClass().getMethod("runDelayed", pluginClass, Location.class, consumerClass, long.class);

            entityGetScheduler = Entity.class.getMethod("getScheduler");
            final Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            entityExecute = entitySchedulerClass.getMethod("execute", pluginClass, runnableClass, runnableClass, long.class);
            entityDelayed = entitySchedulerClass.getMethod("runDelayed", pluginClass, consumerClass, runnableClass, long.class);
            entityFixed = entitySchedulerClass.getMethod("runAtFixedRate", pluginClass, consumerClass, runnableClass, long.class, long.class);

            final Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            taskCancel = scheduledTaskClass.getMethod("cancel");
        } catch (final Throwable ignored) {
            folia = false;
        }

        IS_FOLIA = folia;
        GLOBAL_REGION_SCHEDULER = global;
        ASYNC_SCHEDULER = async;
        REGION_SCHEDULER = region;
        GLOBAL_EXECUTE = globalExecute;
        GLOBAL_RUN_DELAYED = globalDelayed;
        GLOBAL_RUN_AT_FIXED_RATE = globalFixed;
        ASYNC_RUN_NOW = asyncNow;
        ASYNC_RUN_DELAYED = asyncDelayed;
        ASYNC_RUN_AT_FIXED_RATE = asyncFixed;
        REGION_EXECUTE = regionExecute;
        REGION_RUN_DELAYED = regionDelayed;
        ENTITY_EXECUTE = entityExecute;
        ENTITY_RUN_DELAYED = entityDelayed;
        ENTITY_RUN_AT_FIXED_RATE = entityFixed;
        ENTITY_GET_SCHEDULER = entityGetScheduler;
        TASK_CANCEL = taskCancel;
    }

    private FoliaCompat() {
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void runGlobal(Runnable runnable) {
        if (IS_FOLIA) {
            invoke(GLOBAL_EXECUTE, GLOBAL_REGION_SCHEDULER, MCPets.getInstance(), runnable);
        } else {
            Bukkit.getScheduler().runTask(MCPets.getInstance(), runnable);
        }
    }

    public static Object runGlobalLater(Runnable runnable, long delay) {
        long d = Math.max(1L, delay);
        if (IS_FOLIA) {
            return invoke(GLOBAL_RUN_DELAYED, GLOBAL_REGION_SCHEDULER, MCPets.getInstance(), (Consumer<Object>) task -> runnable.run(), d);
        }
        return Bukkit.getScheduler().runTaskLater(MCPets.getInstance(), runnable, delay);
    }

    public static Object runGlobalTimer(Runnable runnable, long delay, long period) {
        long d = Math.max(1L, delay);
        long p = Math.max(1L, period);
        if (IS_FOLIA) {
            return invoke(GLOBAL_RUN_AT_FIXED_RATE, GLOBAL_REGION_SCHEDULER, MCPets.getInstance(), (Consumer<Object>) task -> runnable.run(), d, p);
        }
        return Bukkit.getScheduler().runTaskTimer(MCPets.getInstance(), runnable, delay, period);
    }

    public static void runEntity(Entity entity, Runnable runnable) {
        if (entity == null || !entity.isValid() || !IS_FOLIA) {
            runGlobal(runnable);
            return;
        }
        try {
            final Object scheduler = ENTITY_GET_SCHEDULER.invoke(entity);
            invoke(ENTITY_EXECUTE, scheduler, MCPets.getInstance(), runnable, null, 1L);
        } catch (final Throwable ex) {
            runGlobal(runnable);
        }
    }

    public static Object runEntityLater(Entity entity, Runnable runnable, long delay) {
        if (entity == null || !entity.isValid() || !IS_FOLIA) {
            return runGlobalLater(runnable, delay);
        }
        long d = Math.max(1L, delay);
        try {
            final Object scheduler = ENTITY_GET_SCHEDULER.invoke(entity);
            return invoke(ENTITY_RUN_DELAYED, scheduler, MCPets.getInstance(), (Consumer<Object>) task -> runnable.run(), null, d);
        } catch (final Throwable ex) {
            return runGlobalLater(runnable, delay);
        }
    }

    public static Object runEntityTimer(Entity entity, Runnable runnable, long delay, long period) {
        if (entity == null || !entity.isValid() || !IS_FOLIA) {
            return runGlobalTimer(runnable, delay, period);
        }
        long d = Math.max(1L, delay);
        long p = Math.max(1L, period);
        try {
            final Object scheduler = ENTITY_GET_SCHEDULER.invoke(entity);
            return invoke(ENTITY_RUN_AT_FIXED_RATE, scheduler, MCPets.getInstance(), (Consumer<Object>) task -> runnable.run(), null, d, p);
        } catch (final Throwable ex) {
            return runGlobalTimer(runnable, delay, period);
        }
    }

    public static void runLocation(Location location, Runnable runnable) {
        if (location == null || location.getWorld() == null || !IS_FOLIA) {
            runGlobal(runnable);
            return;
        }
        invoke(REGION_EXECUTE, REGION_SCHEDULER, MCPets.getInstance(), location, runnable);
    }

    public static Object runLocationLater(Location location, Runnable runnable, long delay) {
        if (location == null || location.getWorld() == null || !IS_FOLIA) {
            return runGlobalLater(runnable, delay);
        }
        long d = Math.max(1L, delay);
        return invoke(REGION_RUN_DELAYED, REGION_SCHEDULER, MCPets.getInstance(), location, (Consumer<Object>) task -> runnable.run(), d);
    }

    public static void runAsync(Runnable runnable) {
        if (IS_FOLIA) {
            invoke(ASYNC_RUN_NOW, ASYNC_SCHEDULER, MCPets.getInstance(), (Consumer<Object>) task -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(MCPets.getInstance(), runnable);
        }
    }

    public static Object runAsyncLater(Runnable runnable, long delayTicks) {
        if (IS_FOLIA) {
            long delayMs = Math.max(1L, delayTicks) * 50L;
            return invoke(ASYNC_RUN_DELAYED, ASYNC_SCHEDULER, MCPets.getInstance(), (Consumer<Object>) task -> runnable.run(), delayMs, TimeUnit.MILLISECONDS);
        }
        return Bukkit.getScheduler().runTaskLaterAsynchronously(MCPets.getInstance(), runnable, delayTicks);
    }

    public static Object runAsyncTimer(Runnable runnable, long delayTicks, long periodTicks) {
        if (IS_FOLIA) {
            long delayMs = Math.max(1L, delayTicks) * 50L;
            long periodMs = Math.max(1L, periodTicks) * 50L;
            return invoke(ASYNC_RUN_AT_FIXED_RATE, ASYNC_SCHEDULER, MCPets.getInstance(), (Consumer<Object>) task -> runnable.run(), delayMs, periodMs, TimeUnit.MILLISECONDS);
        }
        return Bukkit.getScheduler().runTaskTimerAsynchronously(MCPets.getInstance(), runnable, delayTicks, periodTicks);
    }

    public static void cancel(Object task) {
        if (task == null) {
            return;
        }
        if (task instanceof BukkitTask bukkitTask) {
            bukkitTask.cancel();
            return;
        }
        if (TASK_CANCEL != null) {
            try {
                TASK_CANCEL.invoke(task);
            } catch (final Throwable ignored) {
            }
        }
    }

    private static Object invoke(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (final Throwable ex) {
            throw new IllegalStateException("FoliaCompat invoke failed: " + method, ex);
        }
    }
}
