package com.codeaffine.tiny.shared;

public class ServiceLoaderAdapterTestServiceFactoryImpl implements ServiceLoaderAdapterTestServiceFactory {
    @Override
    public Runnable create() {
        return () -> {};
    }
}
