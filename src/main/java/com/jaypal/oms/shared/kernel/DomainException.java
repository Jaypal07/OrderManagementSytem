package com.jaypal.oms.shared.kernel;

/**
 * Base class for all domain-level exceptions.
 * Represents a business rule violation.
 *
 * Domain exceptions:
 * - Are expected
 * - Are meaningful
 * - Must NOT wrap technical exceptions
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
