package xyz.vsngamer.elevatorid.init;

import java.util.function.Supplier;

/** Sustituto mínimo del RegistryObject de Forge: en Fabric se registra al momento. */
public final class RegistryObject<T> implements Supplier<T> {
    private final T value;

    RegistryObject(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }
}
