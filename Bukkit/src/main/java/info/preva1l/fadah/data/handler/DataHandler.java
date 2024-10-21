package info.preva1l.fadah.data.handler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataHandler {

    UUID DUMMY_ID = new UUID(0, 0);

    <T> List<T> getAll(Class<T> clazz);
    <T> Optional<T> get(Class<T> clazz, UUID id);
    <T> void save(Class<T> clazz, T t);
    <T> void update(Class<T> clazz, T t, String[] params);
    <T> void delete(Class<T> clazz, T t);
    <T> void deleteSpecific(Class<T> clazz, T t, Object o);
}