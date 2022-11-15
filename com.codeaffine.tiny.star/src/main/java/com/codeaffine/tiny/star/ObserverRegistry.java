package com.codeaffine.tiny.star;

import static com.codeaffine.tiny.star.Messages.ERROR_INVALID_METHOD_SIGNATURE;

import static java.lang.String.format;
import static java.util.Arrays.stream;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import lombok.NonNull;

class ObserverRegistry<T> {

    private final Map<Class<? extends Annotation>, List<Observer>> observers;
    private final Class<T> observedType;
    private final T observedInstance;

    record Observer(Object observer, Method method) {}

    @SafeVarargs
    ObserverRegistry(@NonNull T observedInstance, @NonNull Class<T> observedType, @NonNull Class<? extends Annotation>... observerAnnotations) {
        this.observedInstance = observedInstance;
        this.observedType = observedType;
        this.observers = new HashMap<>();
        stream(observerAnnotations).forEach(observerType -> observers.put(observerType, new CopyOnWriteArrayList<>()));
    }

    void registerObserver(Object observer) {
        Method[] methods = observer.getClass().getDeclaredMethods();
        observers.forEach((annotation, annotationObservers) -> updateRegistration(observer, methods, annotationObservers::add, annotation));
    }

    void deregisterObserver(Object observer) {
        Method[] methods = observer.getClass().getDeclaredMethods();
        observers.forEach((annotation, annotationObservers) -> updateRegistration(observer, methods, annotationObservers::remove, annotation));
    }

    private void updateRegistration(Object observer, Method[] methods, Consumer<Observer> observerRegistrar, Class<? extends Annotation> annotation) {
        stream(methods)
            .filter(method -> method.isAnnotationPresent(annotation))
            .map(method -> new Observer(observer, verifySignature(observer, method)))
            .forEach(observerRegistrar);
    }

    private Method verifySignature(Object observer, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 0 || parameterTypes.length == 1 && observedType.isAssignableFrom(parameterTypes[0])) {
            return method;
        }
        throw createIllegalSignatureDetectedException(observer, method);
    }

    private IllegalArgumentException createIllegalSignatureDetectedException(Object observer, Method method) {
        String observerClass = observer.getClass().getName();
        String observerMethod = method.getName();
        String lifecycleSimpleName = observedType.getSimpleName();
        String lifecycleClass = observedType.getName();
        return new IllegalArgumentException(format(ERROR_INVALID_METHOD_SIGNATURE, observerClass, observerMethod, lifecycleSimpleName, lifecycleClass));
    }

    void notifyObservers(Class<? extends Annotation> observerType, Consumer<Exception> exceptionHandler) {
        observers.get(observerType)
            .forEach(observer -> notifyObserver(observer, exceptionHandler));
    }

    private void notifyObserver(Observer observer, Consumer<Exception> exceptionHandler) {
        observer.method().setAccessible(true); // NOSONAR
        Class<?>[] parameterTypes = observer.method().getParameterTypes();
        try {
            doNotifyObserver(observer, parameterTypes);
        } catch (IllegalAccessException | InvocationTargetException cause) {
            exceptionHandler.accept(cause);
        }
    }

    private void doNotifyObserver(Observer observer, Class<?>[] parameterTypes) throws IllegalAccessException, InvocationTargetException {
        if (parameterTypes.length == 0) {
            observer.method().invoke(observer.observer());
        } else {
            observer.method().invoke(observer.observer(), observedInstance);
        }
    }
}
