package com.jaypal.oms.shared.kernel;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base class for all value objects.
 * Value objects are immutable and compared by value.
 */
public abstract class ValueObject implements Serializable {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return equalsCore(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEqualityComponents());
    }

    /**
     * Compare actual value components.
     */
    protected abstract boolean equalsCore(Object other);

    /**
     * Components used for equality and hashcode.
     */
    protected abstract Object[] getEqualityComponents();
}
