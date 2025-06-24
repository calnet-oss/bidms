package edu.berkeley.bidms.common.validation;

import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Payload;
import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.ValidateUnwrappedValue;
import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.lang.Nullable;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DynamicPayloadAwareValidator extends SpringValidatorAdapter {
    /**
     * Create a new SpringValidatorAdapter for the given JSR-303 Validator.
     *
     * @param targetValidator the JSR-303 Validator to wrap
     */
    public DynamicPayloadAwareValidator(Validator targetValidator) {
        super(targetValidator);
    }

    @Override
    protected void processConstraintViolations(Set<ConstraintViolation<Object>> violations, Errors errors) {
        Set<ConstraintViolation<Object>> wrappedViolations = violations.stream().map(WrappedConstraintViolation::new).collect(Collectors.toSet());
        super.processConstraintViolations(wrappedViolations, errors);
    }

    @Override
    protected String determineErrorCode(ConstraintDescriptor<?> descriptor) {
        WrappedConstraintDescriptor<?, ?> wrappedConstraintDescriptor = (WrappedConstraintDescriptor<?, ?>) descriptor;
        ConstraintViolationDynamicPayload dynamicPayload = wrappedConstraintDescriptor.getHibernateConstraintViolationBidmsDynamicPayload();
        if (dynamicPayload != null && dynamicPayload.getCode() != null) {
            return dynamicPayload.getCode();
        } else {
            return super.determineErrorCode(descriptor);
        }
    }

    @Override
    protected Object[] getArgumentsForConstraint(String objectName, String field, ConstraintDescriptor<?> descriptor) {
        LinkedList<Object> superArguments = Arrays.stream(super.getArgumentsForConstraint(objectName, field, descriptor)).collect(Collectors.toCollection(LinkedList::new));

        WrappedConstraintDescriptor<?, ?> wrappedConstraintDescriptor = (WrappedConstraintDescriptor<?, ?>) descriptor;
        ConstraintViolationDynamicPayload dynamicPayload = wrappedConstraintDescriptor.getHibernateConstraintViolationBidmsDynamicPayload();

        /*
         * The documentation for this method in the super class states:
         * <blockquote>...returns a first argument indicating the field name
         * (see {@link #getResolvableField}). Afterwards, it adds all actual
         * constraint annotation attributes (i.e. excluding "message",
         * "groups" and "payload") in alphabetical order of their attribute
         * names.</blockquote>
         *
         * We CHANGE the returned arguments array to be:
         * <pre>
         * Index     Description
         * 0         The field name.
         * 1         If a constraint violation leaf bean is present (see
         *           {@link ConstraintViolation#getLeafBean()}) then this
         *           element is the leaf bean class.  If the leaf bean is not
         *           present, then this element is not added.
         * the rest  all actual constraint annotation attributes (i.e.
         *           excluding "message", "groups" and "payload") in
         *           alphabetical order of their attribute names PLUS
         *           additional arguments from
         *           {@link BidmsConstraintViolationDynamicPayload#getArguments()}.
         * </pre>
         */
        LinkedList<Object> newArguments = new LinkedList<>();

        // the field name
        newArguments.add(superArguments.pop());

        // the leaf bean class, if present
        Object leafBean = wrappedConstraintDescriptor.getConstraintViolation().getLeafBean();
        if (leafBean != null) {
            newArguments.add(leafBean.getClass());
        }

        // The super arguments (after the first element, the field name, was popped off the list).
        newArguments.addAll(superArguments);

        // If there is a dynamic payload with additional arguments, add them.
        if (dynamicPayload != null && dynamicPayload.getArguments() != null) {
            List<?> transformedArguments = dynamicPayload.getArguments().stream().map(element -> {
                if (element instanceof String) {
                    return new ResolvableAttribute(element.toString());
                } else {
                    return element;
                }
            }).collect(Collectors.toList());
            newArguments.addAll(transformedArguments);
        }

        return newArguments.toArray();
    }

    // purpose of this is to override getConstraintDescriptor() to return a
    // WrappedConstraintDescriptor that links the descriptor to the violation
    private static class WrappedConstraintViolation<T> implements ConstraintViolation<T> {

        private final ConstraintViolation<T> delegate;
        private WrappedConstraintDescriptor<?, T> wrappedConstraintDescriptor;

        public WrappedConstraintViolation(ConstraintViolation<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getMessage() {
            return delegate.getMessage();
        }

        @Override
        public String getMessageTemplate() {
            return delegate.getMessageTemplate();
        }

        @Override
        public T getRootBean() {
            return delegate.getRootBean();
        }

        @Override
        public Class<T> getRootBeanClass() {
            return delegate.getRootBeanClass();
        }

        @Override
        public Object getLeafBean() {
            return delegate.getLeafBean();
        }

        @Override
        public Object[] getExecutableParameters() {
            return delegate.getExecutableParameters();
        }

        @Override
        public Object getExecutableReturnValue() {
            return delegate.getExecutableReturnValue();
        }

        @Override
        public Path getPropertyPath() {
            return delegate.getPropertyPath();
        }

        @Override
        public Object getInvalidValue() {
            return delegate.getInvalidValue();
        }

        @Override
        public ConstraintDescriptor<?> getConstraintDescriptor() {
            if (wrappedConstraintDescriptor == null) {
                this.wrappedConstraintDescriptor = new WrappedConstraintDescriptor<>(this, delegate.getConstraintDescriptor());
            }
            return wrappedConstraintDescriptor;
        }

        @Override
        public <U> U unwrap(Class<U> type) {
            return delegate.unwrap(type);
        }
    }

    // purpose of this is to link the descriptor to the violation it came from
    private static class WrappedConstraintDescriptor<A extends Annotation, T> implements ConstraintDescriptor<A> {

        private final ConstraintViolation<T> violation;
        private final ConstraintDescriptor<A> delegate;

        public WrappedConstraintDescriptor(ConstraintViolation<T> violation, ConstraintDescriptor<A> delegate) {
            this.violation = violation;
            this.delegate = delegate;
        }

        @Override
        public A getAnnotation() {
            return delegate.getAnnotation();
        }

        @Override
        public String getMessageTemplate() {
            return delegate.getMessageTemplate();
        }

        @Override
        public Set<Class<?>> getGroups() {
            return delegate.getGroups();
        }

        @Override
        public Set<Class<? extends Payload>> getPayload() {
            return delegate.getPayload();
        }

        @Override
        public ConstraintTarget getValidationAppliesTo() {
            return delegate.getValidationAppliesTo();
        }

        @Override
        public List<Class<? extends ConstraintValidator<A, ?>>> getConstraintValidatorClasses() {
            return delegate.getConstraintValidatorClasses();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return delegate.getAttributes();
        }

        @Override
        public Set<ConstraintDescriptor<?>> getComposingConstraints() {
            return delegate.getComposingConstraints();
        }

        @Override
        public boolean isReportAsSingleViolation() {
            return delegate.isReportAsSingleViolation();
        }

        @Override
        public ValidateUnwrappedValue getValueUnwrapping() {
            return delegate.getValueUnwrapping();
        }

        @Override
        public <U> U unwrap(Class<U> type) {
            return delegate.unwrap(type);
        }

        public ConstraintViolation<T> getConstraintViolation() {
            return violation;
        }

        public <U extends ConstraintViolation<?>> U getUnwrappedConstraintViolation(Class<U> type) {
            return violation.unwrap(type);
        }

        public HibernateConstraintViolation<?> getHibernateConstraintViolation() {
            return getUnwrappedConstraintViolation(HibernateConstraintViolation.class);
        }

        public <P> P getHibernateConstraintViolationDynamicPayload(Class<P> payloadType) {
            return getHibernateConstraintViolation().getDynamicPayload(payloadType);
        }

        public ConstraintViolationDynamicPayload getHibernateConstraintViolationBidmsDynamicPayload() {
            return getHibernateConstraintViolation().getDynamicPayload(ConstraintViolationDynamicPayload.class);
        }
    }

    /**
     * Wrapper for a String attribute which can be resolved via a {@code
     * MessageSource}, falling back to the original attribute as a default
     * value otherwise.
     */
    private static class ResolvableAttribute implements MessageSourceResolvable, Serializable {

        private final String resolvableString;

        public ResolvableAttribute(String resolvableString) {
            this.resolvableString = resolvableString;
        }

        @Override
        public String[] getCodes() {
            return new String[]{this.resolvableString};
        }

        @Override
        @Nullable
        public Object[] getArguments() {
            return null;
        }

        @Override
        public String getDefaultMessage() {
            return this.resolvableString;
        }

        @Override
        public String toString() {
            return this.resolvableString;
        }
    }
}
