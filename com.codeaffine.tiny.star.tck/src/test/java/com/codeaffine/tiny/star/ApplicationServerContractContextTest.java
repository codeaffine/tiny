package com.codeaffine.tiny.star;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.invocation.InvocationOnMock;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApplicationServerContractContextTest {

    private ApplicationServerContractContext context;
    private ApplicationServer applicationServer;
    private ExtensionContext extensionContext;

    @SuppressWarnings("unused")
    interface MethodParameterTemplate {
        void methodWithExpectedParameter(ApplicationServerContractContext parameter);
        void methodWithUnexpectedParameter(Runnable parameter);
    }

    @BeforeEach
    void setUp() {
        context = new ApplicationServerContractContext();
        applicationServer = mock(ApplicationServer.class);
        extensionContext = stubExtensionContext(stubStore());
    }

    @Test
    void beforeAll() {
        ApplicationServerContractContext before = readInstanceFromExtensionContextStore();
        context.beforeAll(extensionContext);
        ApplicationServerContractContext after = readInstanceFromExtensionContextStore();

        assertThat(before).isNull();
        assertThat(after).isNotNull();
    }

    @Test
    void resolveParameter() {
        Object before = context.resolveParameter(null, extensionContext);
        extensionContext.getStore(ApplicationServerContractContext.NAMESPACE)
            .put(ApplicationServerContractContext.CONTEXT_STORAGE_KEY, context);
        Object after = context.resolveParameter(null, extensionContext);

        assertThat(before).isNull();
        assertThat(after).isSameAs(context);
    }

    @ParameterizedTest
    @CsvSource({
        "methodWithExpectedParameter, true",
        "methodWithUnexpectedParameter, false"
    })
    void supportsParameter(String methodName, boolean expectedSupport) throws NoSuchMethodException {
        Parameter parameter = extractParameter(methodName);
        ParameterContext parameterContext = stubParameterContext(parameter);

        boolean actual = context.supportsParameter(parameterContext, null);

        assertThat(actual).isEqualTo(expectedSupport);
    }

    @Test
    void startingApplicationServer() {
        context.stoppingApplicationServer();
        ApplicationServer before = context.getApplicationServer();
        context.startingApplicationServer(applicationServer);
        ApplicationServer after = context.getApplicationServer();

        assertThat(before).isNull();
        assertThat(after).isSameAs(applicationServer);
    }

    @Test
    void stoppingApplicationServer() {
        context.startingApplicationServer(applicationServer);
        ApplicationServer before = context.getApplicationServer();
        context.stoppingApplicationServer();
        ApplicationServer after = context.getApplicationServer();

        assertThat(before).isSameAs(applicationServer);
        assertThat(after).isNull();
    }

    private static Store stubStore() {
        Store result = mock(Store.class);
        Map<String, ApplicationServerContractContext> storage = new HashMap<>();
        doAnswer(invocation -> putToStorage(invocation, storage))
            .when(result)
            .put(any(), any());
        doAnswer(invocation -> getFromStorage(invocation, storage)).
            when(result)
            .get(ApplicationServerContractContext.CONTEXT_STORAGE_KEY, ApplicationServerContractContext.class);
        return result;
    }

    private static Void putToStorage(InvocationOnMock invocation, Map<String, ApplicationServerContractContext> storage) {
        Object key = invocation.getArgument(0);
        Object value = invocation.getArgument(1);
        storage.put((String) key, (ApplicationServerContractContext) value);
        return null;
    }

    private static ApplicationServerContractContext getFromStorage(InvocationOnMock invocation, Map<String, ApplicationServerContractContext> storage) {
        return storage.get(invocation.getArgument(0, String.class));
    }

    private static ExtensionContext stubExtensionContext(Store store) {
        ExtensionContext result = mock(ExtensionContext.class);
        when(result.getStore(ApplicationServerContractContext.NAMESPACE)).thenReturn(store);
        return result;
    }

    private ApplicationServerContractContext readInstanceFromExtensionContextStore() {
        return extensionContext.getStore(ApplicationServerContractContext.NAMESPACE)
            .get(ApplicationServerContractContext.CONTEXT_STORAGE_KEY, ApplicationServerContractContext.class);
    }

    private static Parameter extractParameter(String methodName) throws NoSuchMethodException {
        Method method = Arrays.stream(MethodParameterTemplate.class.getMethods())
            .filter(methodToCheck -> methodToCheck.getName().equals(methodName))
            .findFirst()
            .orElseThrow(() -> new NoSuchMethodException(methodName));
        return method.getParameters()[0];
    }

    private static ParameterContext stubParameterContext(Parameter parameter) {
        ParameterContext result = mock(ParameterContext.class);
        when(result.getParameter()).thenReturn(parameter);
        return result;
    }
}
