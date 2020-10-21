/*
 * Copyright (c) 2020, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.berkeley.bidms.common.constraints;

import edu.berkeley.bidms.common.validation.BidmsConstraintViolationDynamicPayload;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class ConstraintsUtil {

    /**
     * This adds a dynamic payload map to a constraint violation.  this is
     * typically used by a property-level or method-level validator.
     * <p>
     * The dynamic payload (see {@link BidmsConstraintViolationDynamicPayload})
     * contains the following properties: <code>code</code> and
     * <code>message</code>.
     * <code>code</code> is the short form of the error and
     * <code>message</code>
     * is the longer description of the error.
     * <p>
     * To access the payload: {@code violation.unwrap(HibernateConstraintViolation).getDynamicPayload(BidmsConstraintViolationDynamicPayload.class)}
     * where <code>violation</code> is an instance of {@link
     * javax.validation.ConstraintViolation}.
     * <p>
     * See <a href="https://docs.jboss.org/hibernate/validator/6.0/reference/en-US/html_single/#section-dynamic-payload">Hibernate
     * Validator reference documentation: Dynamic payload as part of
     * ConstraintViolation</a>.
     *
     * @param context An instance of {@link ConstraintValidatorContext}
     *                accessible from implementations of {@link
     *                javax.validation.ConstraintValidator#isValid(Object,
     *                ConstraintValidatorContext)}.
     * @param code    Short form of the error description.
     * @param message Long form of the error description.
     * @return Always returns false as a convenience so that {@code return
     * violation(...)} may be used within implementations of {@link
     * javax.validation.ConstraintValidator#isValid(Object,
     * ConstraintValidatorContext)}.
     */
    public static boolean violation(ConstraintValidatorContext context, String code, String message) {
        HibernateConstraintValidatorContext ctx = context.unwrap(HibernateConstraintValidatorContext.class);
        ctx.disableDefaultConstraintViolation();
        ctx.withDynamicPayload(new BidmsConstraintViolationDynamicPayload(code, message))
                .buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
        return false;
    }

    /**
     * Same as {@link #violation(ConstraintValidatorContext, String, String)}
     * but additionally sets the <code>field</code> property in the dynamic
     * payload.
     *
     * @param context      An instance of {@link ConstraintValidatorContext}
     *                     accessible from implementations of {@link
     *                     javax.validation.ConstraintValidator#isValid(Object,
     *                     ConstraintValidatorContext)}.
     * @param propertyName Name of property that has failed validation.
     * @param code         Short form of the error description.
     * @param message      Long form of the error description.
     * @return Always returns false as a convenience so that {@code return
     * violation(...)} may be used within implementations of {@link
     * javax.validation.ConstraintValidator#isValid(Object,
     * ConstraintValidatorContext)}.
     */
    public static boolean violation(ConstraintValidatorContext context, String propertyName, String code, String message) {
        return violation(context, propertyName, code, message, null);
    }

    /**
     * Same as {@link #violation(ConstraintValidatorContext, String, String,
     * String)} but a list of additional attributes can be added to the
     * payload.
     */
    public static boolean violation(ConstraintValidatorContext context, String propertyName, String code, String message, List<Object> additionalPayloadAttributes) {
        HibernateConstraintValidatorContext ctx = context.unwrap(HibernateConstraintValidatorContext.class);
        ctx.disableDefaultConstraintViolation();
        ctx.withDynamicPayload(new BidmsConstraintViolationDynamicPayload(code, message, additionalPayloadAttributes))
                .buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyName)
                .addConstraintViolation();

        return false;
    }
}
