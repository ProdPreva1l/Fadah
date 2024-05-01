package info.preva1l.fadah.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;

@UtilityClass
public final class CommandMapUtil {
    private static final Field COMMAND_MAP_FIELD;

    static {
        try {
            COMMAND_MAP_FIELD = SimplePluginManager.class.getDeclaredField("commandMap");
            COMMAND_MAP_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static CommandMap getCommandMap(Server server) {
        try {
            return (CommandMap) COMMAND_MAP_FIELD.get(server.getPluginManager());
        } catch (Exception e) {
            throw new RuntimeException("Could not get CommandMap", e);
        }
    }
}