package info.preva1l.fadah.utils;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import lombok.experimental.UtilityClass;

/**
 * Easy creation of Bukkit Tasks
 */
@SuppressWarnings("unused")
@UtilityClass
public class TaskManager {
    /**
     * Synchronous Tasks
     */
    @UtilityClass
    public class Sync {
        /**
         * Run a synchronous task once. Helpful when needing to run some sync code in an async loop
         *
         * @param plugin   The current plugin typeof JavaPlugin. (Not Commons)
         * @param runnable The runnable, lambda supported yeh
         */
        public void run(Fadah plugin, Runnable runnable) {
            plugin.getServer().getScheduler().runTask(plugin, runnable);
        }

        /**
         * Run a synchronous task forever with a delay between runs.
         *
         * @param plugin   The current plugin typeof JavaPlugin. (Not Commons)
         * @param runnable The runnable, lambda supported yeh
         * @param interval Time between each run
         */
        public void runTask(Fadah plugin, Runnable runnable, long interval) {
            plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, 0L, interval);
        }

        /**
         * Run a synchronous task once with a delay. Helpful when needing to run some sync code in an async loop
         *
         * @param plugin   The current plugin typeof JavaPlugin. (Not Commons)
         * @param runnable The runnable, lambda supported yeh
         * @param delay    Time before running.
         */
        public void runLater(Fadah plugin, Runnable runnable, long delay) {
            plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    /**
     * Asynchronous tasks
     */
    @UtilityClass
    public class Async {
        /**
         * Run an asynchronous task once. Helpful when needing to run some sync code in an async loop
         *
         * @param plugin   The current plugin typeof JavaPlugin. (Not Commons)
         * @param runnable The runnable, lambda supported yeh
         */
        public static void run(Fadah plugin, Runnable runnable) {
            MultiLib.getAsyncScheduler().runNow(plugin, task -> runnable.run());
        }

        /**
         * Run an asynchronous task forever with a delay between runs.
         *
         * @param plugin   The current plugin typeof JavaPlugin. (Not Commons)
         * @param runnable The runnable, lambda supported yeh
         * @param interval Time between each run
         */
        public void runTask(Fadah plugin, Runnable runnable, long interval) {
            MultiLib.getAsyncScheduler().runAtFixedRate(plugin, task -> runnable.run(), 0L, interval);
        }

        /**
         * Run an asynchronous task once with a delay. Helpful when needing to run some sync code in an async loop
         *
         * @param plugin   The current plugin typeof JavaPlugin. (Not Commons)
         * @param runnable The runnable, lambda supported yeh
         * @param delay    Time before running.
         */
        public void runLater(Fadah plugin, Runnable runnable, long delay) {
            MultiLib.getAsyncScheduler().runDelayed(plugin, task -> runnable.run(), delay);
        }
    }
}