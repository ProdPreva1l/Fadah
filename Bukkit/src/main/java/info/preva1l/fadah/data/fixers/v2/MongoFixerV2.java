package info.preva1l.fadah.data.fixers.v2;

import java.util.UUID;

public class MongoFixerV2 implements V2Fixer {

    @Override
    public void fixExpiredItems(UUID player) {
        // do nothing
    }

    @Override
    public void fixCollectionBox(UUID player) {
        // do nothing
    }

    @Override
    public boolean needsFixing(UUID player) {
        return false;
    }
}
