package info.preva1l.fadah.config;

public class Tuple<F,S> {
    public F first;
    public S second;

    public Tuple(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Tuple<F,S> of(F first, S second) {
        return new Tuple<>(first, second);
    }
}
