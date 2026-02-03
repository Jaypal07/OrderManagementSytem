package com.jaypal.oms.shared.kernel;

/**
 * Represents a single business rule.
 * A rule must be explicit, testable, and self-contained.
 */
public interface BusinessRule {

    /**
     * @return true if the rule is satisfied
     */
    boolean isSatisfied();

    /**
     * @return exception to be thrown when rule is violated
     */
    DomainException getException();
}
