package com.nisovin.shopkeepers.util;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitWorker;

/**
 * Scheduler-related utilities.
 */
public final class SchedulerUtils {

	private SchedulerUtils() {
	}

	public static int getActiveAsyncTasks(Plugin plugin) {
		int workers = 0;
		for (BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
			if (worker.getOwner().equals(plugin)) {
				workers++;
			}
		}
		return workers;
	}

	private static void validatePluginTask(Plugin plugin, Runnable task) {
		Validate.notNull(plugin, "Plugin is null!");
		Validate.notNull(task, "Task is null!");
	}

	/**
	 * Checks if the current thread is the server's main thread.
	 * 
	 * @return <code>true</code> if currently running on the main thread
	 */
	public static boolean isMainThread() {
		return Bukkit.isPrimaryThread();
	}

	/**
	 * Schedules the given task to be run on the primary thread if required.
	 * <p>
	 * If the current thread is already the primary thread, the task will be run immediately. Otherwise it attempts to
	 * schedule the task to run on the server's primary thread. However, if the plugin is <code>null</code> or disabled,
	 * the task won't be scheduled.
	 * 
	 * @param plugin
	 *            the plugin to use for scheduling
	 * @param task
	 *            the task
	 * @return <code>true</code> if the task was run or successfully scheduled to be run, <code>false</code> otherwise
	 */
	public static boolean runOnMainThreadOrOmit(Plugin plugin, Runnable task) {
		if (isMainThread()) {
			task.run();
		} else {
			if (plugin == null) return false;
			if (!runTaskOrOmit(plugin, task)) return false;
		}
		return true;
	}

	public static boolean runTaskOrOmit(Plugin plugin, Runnable task) {
		return runTaskLaterOrOmit(plugin, task, 0L);
	}

	public static boolean runTaskLaterOrOmit(Plugin plugin, Runnable task, long delay) {
		validatePluginTask(plugin, task);
		// tasks can only be registered while enabled:
		if (plugin.isEnabled()) {
			try {
				Bukkit.getScheduler().runTaskLater(plugin, task, delay);
				return true;
			} catch (IllegalPluginAccessException e) {
				// couldn't register task: the plugin got disabled just now
			}
		}
		return false;
	}

	public static boolean runAsyncTaskOrOmit(Plugin plugin, Runnable task) {
		return runAsyncTaskLaterOrOmit(plugin, task, 0L);
	}

	public static boolean runAsyncTaskLaterOrOmit(Plugin plugin, Runnable task, long delay) {
		validatePluginTask(plugin, task);
		// tasks can only be registered while enabled:
		if (plugin.isEnabled()) {
			try {
				Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
				return true;
			} catch (IllegalPluginAccessException e) {
				// couldn't register task: the plugin got disabled just now
			}
		}
		return false;
	}

	/**
	 * Awaits the completion of async tasks of the specified plugin.
	 * <p>
	 * If a logger is specified, it will be used to print informational messages suited to the context of this method
	 * being called during disabling of the plugin.
	 * 
	 * @param plugin
	 *            the plugin
	 * @param asyncTasksTimeoutSeconds
	 *            the duration to wait for async tasks to finish in seconds (can be <code>0</code>)
	 * @param logger
	 *            the logger used for printing informational messages, can be <code>null</code>
	 * @return the number of remaining async tasks that are still running after waiting for the specified duration
	 */
	public static int awaitAsyncTasksCompletion(Plugin plugin, int asyncTasksTimeoutSeconds, Logger logger) {
		Validate.notNull(plugin, "Plugin is null!");
		Validate.isTrue(asyncTasksTimeoutSeconds >= 0, "asyncTasksTimeoutSeconds cannot be negative!");

		int activeAsyncTasks = getActiveAsyncTasks(plugin);
		if (activeAsyncTasks > 0 && asyncTasksTimeoutSeconds > 0) {
			if (logger != null) {
				logger.info("Waiting up to " + asyncTasksTimeoutSeconds + " seconds for " + activeAsyncTasks
						+ " remaining async tasks to finish ..");
			}

			final long asyncTasksTimeoutMillis = asyncTasksTimeoutSeconds * 1000L;
			final long waitingStart = System.nanoTime();
			long waitingDurationMillis = 0;
			do {
				// checking again every 5 milliseconds:
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// ignore, but reset interrupt flag:
					Thread.currentThread().interrupt();
				}
				// update the number of active async task before breaking from loop:
				activeAsyncTasks = getActiveAsyncTasks(plugin);

				// update waiting duration and compare to timeout:
				waitingDurationMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - waitingStart);
				if (waitingDurationMillis > asyncTasksTimeoutMillis) {
					// timeout reached, abort waiting..
					break;
				}
			} while (activeAsyncTasks > 0);

			if (waitingDurationMillis > 1 && logger != null) {
				logger.info("Waited " + waitingDurationMillis + " ms for async tasks to finish.");
			}
		}

		if (activeAsyncTasks > 0 && logger != null) {
			// severe, since this can potentially result in data loss, depending on what the tasks are doing:
			logger.severe("There are still " + activeAsyncTasks + " remaining async tasks active! Disabling anyways now ..");
		}
		return activeAsyncTasks;
	}
}
