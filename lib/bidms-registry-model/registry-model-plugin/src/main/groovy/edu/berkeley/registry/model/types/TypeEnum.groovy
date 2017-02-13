package edu.berkeley.registry.model.types

interface TypeEnum<T> {
    T get()

    String getName()

    Integer getId()
}
