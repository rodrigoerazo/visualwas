package com.github.veithen.visualwas.connector;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.github.veithen.visualwas.connector.feature.ConnectorConfigurator;
import com.github.veithen.visualwas.connector.loader.AlternateClass;

final class ConnectorConfiguratorImpl implements ConnectorConfigurator {
    private final Set<Class<?>> processedClasses = new HashSet<>();
    private ClassMapper classMapper;

    ConnectorConfiguratorImpl(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    @Override
    public void addAlternateClasses(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            scan(clazz, true);
        }
    }

    private void scan(Class<?> clazz, boolean requireAnnotation) {
        if (!processedClasses.add(clazz)) {
            return;
        }
        AlternateClass ann = clazz.getAnnotation(AlternateClass.class);
        if (ann == null) {
            if (requireAnnotation) {
                throw new IllegalArgumentException(clazz.getName() + " doesn't have an annotation of type " + AlternateClass.class.getName());
            } else {
                return;
            }
        }
        classMapper.addMapping(ann.value(), clazz.getName());
        scan(clazz.getSuperclass(), false);
        for (Field field : clazz.getDeclaredFields()) {
            scan(field.getType(), false);
        }
    }
    
    void release() {
        classMapper = null;
    }
}
