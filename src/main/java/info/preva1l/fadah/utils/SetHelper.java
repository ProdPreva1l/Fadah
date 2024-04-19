package info.preva1l.fadah.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SetHelper {
    public Set<Material> stringSetToMaterialSet(Set<String> strings) {
        if (strings.contains("*")) {
            return listToSet(Arrays.stream(Material.values()).toList());
        }
        return strings.stream().flatMap(s -> Arrays.stream(Material.values()).filter(key -> {
                    if (s.startsWith("*_")) return key.name().endsWith(s.replace("*", ""));
                    else if (s.endsWith("_*")) return key.name().startsWith(s.replace("*", ""));
                    else return key.name().equals(s);
                }))
                .collect(Collectors.toSet());
    }

    public <T> Set<T> listToSet(List<T> list) {
        return list == null ? null : Set.copyOf(list);
    }
}
