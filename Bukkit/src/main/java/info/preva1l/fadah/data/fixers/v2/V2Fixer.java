package info.preva1l.fadah.data.fixers.v2;

import java.util.UUID;

public interface V2Fixer {
    void fixExpiredItems(UUID player);
    void fixCollectionBox(UUID player);
    boolean needsFixing(UUID player);
}
