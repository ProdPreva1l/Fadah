package info.preva1l.fadah.hooks;


public interface Hook {
    default void enable() {
        setEnabled(true);
    }

    void setEnabled(boolean enabled);
    boolean isEnabled();
}
