package info.preva1l.fadah.hooks;

import info.preva1l.fadah.Fadah;

public interface Hook {
    default void enable() {
        Fadah.getINSTANCE().getHookManager().registerHook(this);
    }

    void setEnabled(boolean enabled);
    boolean isEnabled();
}
