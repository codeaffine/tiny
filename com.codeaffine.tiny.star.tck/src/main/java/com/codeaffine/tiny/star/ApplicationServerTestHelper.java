package com.codeaffine.tiny.star;

import lombok.NoArgsConstructor;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings("java:S1075")
public class ApplicationServerTestHelper {

    public static final String ENTRYPOINT_PATH_1 = "/ep1";
    public static final String ENTRYPOINT_PATH_2 = "/ep2";
    public static final ApplicationConfiguration MULTI_ENTRYPOINT_CONFIGURATION = application -> {
        application.addEntryPoint(ENTRYPOINT_PATH_1, () -> null, null);
        application.addEntryPoint(ENTRYPOINT_PATH_2, () -> null, null);
    };
}
