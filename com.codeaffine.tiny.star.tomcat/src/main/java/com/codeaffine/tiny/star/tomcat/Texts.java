package com.codeaffine.tiny.star.tomcat;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
class Texts {

    static final String ERROR_CREATE_DOC_BASE = "Could not create doc base: %s.";
    static final String ERROR_STARTING_TOMCAT = "Unable to start embedded tomcat.";
    static final String ERROR_STOPPING_TOMCAT = "Unable to stop embedded tomcat.";
    static final String SERVER_NAME = "Tomcat";
}
