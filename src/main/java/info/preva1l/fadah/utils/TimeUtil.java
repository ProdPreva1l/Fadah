package info.preva1l.fadah.utils;

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.Instant;

@UtilityClass
public class TimeUtil {
    public static String formatTimeSince(long added) {
        Instant now = Instant.now();
        Instant deletionInstant = Instant.ofEpochMilli(added);
        Duration durationSinceDeletion = Duration.between(deletionInstant, now);

        long days = durationSinceDeletion.toDays();
        long hours = durationSinceDeletion.toHours() % 24;
        long minutes = durationSinceDeletion.toMinutes() % 60;
        long seconds = durationSinceDeletion.getSeconds() % 60;

        if (days > 0) {
            return String.format("%dd, %dh, %dm, %ds", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%dh, %dm, %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm, %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    public static String formatTimeUntil(long deletionDate) {
        Instant now = Instant.now();
        Instant deletionInstant = Instant.ofEpochMilli(deletionDate);
        Duration durationUntilDeletion = Duration.between(now, deletionInstant);

        long days = durationUntilDeletion.toDays();
        long hours = durationUntilDeletion.toHours() % 24;
        long minutes = durationUntilDeletion.toMinutes() % 60;
        long seconds = durationUntilDeletion.getSeconds() % 60;

        if (days > 0) {
            return String.format("%dd, %dh, %dm, %ds", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%dh, %dm, %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm, %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
