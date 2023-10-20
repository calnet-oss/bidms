/*
 * Copyright (c) 2023, Regents of the University of California and
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
package edu.berkeley.bidms.common.json.mod.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.POJOPropertyBuilder;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import edu.berkeley.bidms.common.json.mod.AddSerializationPropertyModification;
import edu.berkeley.bidms.common.json.mod.SerializationModification;

import java.util.List;

/**
 * Occasionally it is necessary to modify serialization behavior that
 * overrides the Jackson annotations on a class.  For example, this can be
 * used to add serialization of a particular object property that may be
 * otherwise disabled by the Jackson annotations on the class.
 *
 * Usage:
 * <pre>
 *     static final List<Class<?>> INCLUDE_ID_FOR_CLASSES = [Email.class];
 *     var objectMapper = JsonMapper.builder()
 *                 .addModule(new SimpleModule().setSerializerModifier(new ObjectSerializerPropertiesModifier(
 *                         new AddSerializationPropertyModification("id", INCLUDE_ID_FOR_CLASSES)
 *                 )))
 *                 .build();
 *     // objectMapper can then be used as usual, such as to convert the bean to a map
 *     objectMapper.convertValue(bean, Map.class);
 * </pre>
 */
public class ObjectSerializerPropertiesModifier extends BeanSerializerModifier {

    private final SerializationModification mod;

    public ObjectSerializerPropertiesModifier(SerializationModification mod) {
        this.mod = mod;
    }

    /**
     * Modifies serialization configuration based on what is indicated in {@link #mod}
     */
    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        if (mod instanceof AddSerializationPropertyModification &&
                ((AddSerializationPropertyModification) mod).getClassesWithProperty().contains(beanDesc.getType().getRawClass())) {
            addProperty(config, beanDesc, ((AddSerializationPropertyModification) mod).getPropertyName(), beanProperties);
        }
        return beanProperties;
    }

    /**
     * This is for the purposes of configuring Jackson to serialize an
     * additional POJO property.
     *
     * @param config         Jackson serialization config from the object mapper
     * @param beanDesc       A Jackson object that describes the POJO class.
     * @param propertyName   The name of the property in the POJO class to serialize.
     * @param beanProperties A mutable list for Jackson that will be appended with the additional property to serialize.
     */
    @SuppressWarnings("GrMethodMayBeStatic")
    private void addProperty(SerializationConfig config, BeanDescription beanDesc, String propertyName, List<BeanPropertyWriter> beanProperties) {
        var classDef = AnnotatedClassResolver.resolve(config, beanDesc.getType(), config);
        final String getterMethodName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        AnnotatedMethod propertyGetter = classDef.findMethod(getterMethodName, null);
        if (propertyGetter == null) {
            throw new RuntimeException("Class " + beanDesc.getType().getTypeName() + " does not have method " + getterMethodName);
        }

        var pname = new PropertyName(propertyName);
        var propDef = new POJOPropertyBuilder(config, config.getAnnotationIntrospector(), true, pname);
        propDef.addGetter(propertyGetter, pname, false, true, false);

        ((BasicBeanDescription) beanDesc).addProperty(propDef);

        beanProperties.add(
                new BeanPropertyWriter(propDef, propDef.getAccessor(), beanDesc.getClassAnnotations(), config.constructType(propertyGetter.getRawReturnType()), null, null, null, false, null, null)
        );
    }
}
