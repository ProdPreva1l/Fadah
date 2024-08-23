package info.preva1l.fadah.utils;

import info.preva1l.fadah.Fadah;
import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

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
            plugin.getPaperLib().scheduling().asyncScheduler().run(runnable);
        }

        /**
         * Run an asynchronous task forever with a delay between runs.
         *
         * @param plugin   The current plugin typeof JavaPlugin. (Not Commons)
         * @param runnable The runnable, lambda supported yeh
         * @param interval Time between each run
         */
        public void runTask(Fadah plugin, Runnable runnable, long interval) {
            plugin.getPaperLib().scheduling().asyncScheduler().runAtFixedRate(runnable, Duration.of(0, ChronoUnit.MILLIS), Duration.of(interval * 20, ChronoUnit.SECONDS));
        }

        /**
         * Run an asynchronous task once with a delay. Helpful when needing to run some sync code in an async loop
         *
         * @param plugin   The current plugin typeof JavaPlugin. (Not Commons)
         * @param runnable The runnable, lambda supported yeh
         * @param delay    Time before running.
         */
        public void runLater(Fadah plugin, Runnable runnable, long delay) {
            plugin.getPaperLib().scheduling().asyncScheduler().runDelayed(runnable, Duration.of(delay * 20, ChronoUnit.SECONDS));
        }
    }
}