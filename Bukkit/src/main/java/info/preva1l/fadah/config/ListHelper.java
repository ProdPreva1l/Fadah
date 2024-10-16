package info.preva1l.fadah.config;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class ListHelper {
    /**
     * Replaces the target with the replacement.
     * Updates the object and returns the updated object.
     *
     * @param base the list to replace in
     * @param replacements the replacements
     * @return the new list
     */
    @SafeVarargs
    public List<String> replace(List<String> base, Tuple<CharSequence, CharSequence>... replacements) {
        List<String> result = new ArrayList<>(base);
        for (Tuple<CharSequence, CharSequence> replacement : replacements) {
            String string = String.join("\n", result);
            string = string.replace(replacement.first, replacement.second);
            List<String> returnValue = Arrays.stream(string.split("\n")).toList();
            result.clear();
            result.addAll(returnValue);
        }
        return result;
    }
}
