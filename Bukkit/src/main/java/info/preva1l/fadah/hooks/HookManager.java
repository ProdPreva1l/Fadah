package info.preva1l.fadah.hooks;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HookManager {
    private final List<Hook> registeredHooks = new ArrayList<>();

    public void registerHook(Hook hook) {
        hook.enable();
        registeredHooks.add(hook);
    }

    public int hookCount() {
        return registeredHooks.size();
    }

    /**
     * Get a hook
     * @param hook the class of the hook
     * @return an optional of the hook, empty if the hook is not registered
     * @since 1.6
     */
    @ApiStatus.Internal
    public <H extends Hook> Optional<H> getHook(Class<H> hook) {
        return (Optional<H>) registeredHooks.stream().filter(current -> current.getClass() == hook).findFirst();
    }
}
