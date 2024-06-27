package info.preva1l.fadah.commands.subcommands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.multiserver.CacheSync;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import info.preva1l.fadah.utils.guis.FastInvManager;
import info.preva1l.fadah.utils.guis.LayoutManager;
import org.jetbrains.annotations.NotNull;

public class ReloadSubCommand extends SubCommand {
    public ReloadSubCommand(Fadah plugin) {
        super(plugin);
    }

    @SubCommandArgs(name = "reload", aliases = {"rl"}, permission = "fadah.reload", description = "Reloads the plugin!")
    public void execute(@NotNull SubCommandArguments command) {
        if (Fadah.getINSTANCE().getCacheSync() != null) {
            CacheSync.send(CacheSync.CacheType.RELOAD);
            return;
        }
        FastInvManager.closeAll();
        Fadah.getINSTANCE().getConfigFile().load();
        Fadah.getINSTANCE().getLangFile().load();
        Fadah.getINSTANCE().getMenusFile().load();
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.MAIN);
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.NEW_LISTING);
        Fadah.getINSTANCE().getCategoriesFile().load();
        CategoryCache.update();
        Fadah.getINSTANCE().getDatabase().loadListings();
        command.sender().sendMessage(StringUtils.colorize(Lang.PREFIX.toFormattedString() + "&aConfig reloaded!"));
    }
}
