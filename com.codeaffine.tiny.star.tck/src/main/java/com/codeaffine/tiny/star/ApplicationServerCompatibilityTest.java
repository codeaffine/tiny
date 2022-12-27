package com.codeaffine.tiny.star;

import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@ExtendWith(ApplicationServerContractContext.class)
@TestMethodOrder(ApplicationServerContractTestExecutionOrder.class)
@interface ApplicationServerCompatibilityTest {}
