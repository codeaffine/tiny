/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.demo;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.UUID;

import static com.codeaffine.tiny.demo.ApplicationConfigurations.configureApplication;

class HelloWorld extends AbstractEntryPoint implements ApplicationConfiguration {

    private static final String TEXT = "Hello World!\n\nGive me something unique:";

    @Override
    public void configure(Application application) {
        configureApplication(application, getClass());
    }

    @Override
    protected void createContents(Composite parent) {
        parent.setLayout(new FillLayout(SWT.VERTICAL));
        Label label = new Label(parent, SWT.WRAP);
        label.setText(TEXT);
        Button button = new Button(parent, SWT.PUSH);
        button.setText("Push me");
        button.addListener(SWT.Selection, event -> label.setText(TEXT + "\n" + UUID.randomUUID()));
    }
}
