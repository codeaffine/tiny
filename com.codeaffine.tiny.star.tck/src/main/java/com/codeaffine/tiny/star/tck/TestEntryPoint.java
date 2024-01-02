/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.tck;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class TestEntryPoint extends AbstractEntryPoint {

    static final String BUTTON_LABEL = "Push me: ";
    static final String BUTTON_AFTER_PUSHED_LABEL = "Pushed: ";

    @NonNull
    private String trackingId;

    @Override
    protected void createContents(Composite parent) {
        parent.setLayout(new FillLayout(SWT.VERTICAL));
        Button button = new Button(parent, SWT.PUSH);
        button.setText(BUTTON_LABEL + trackingId);
        button.addListener(SWT.Selection, event -> button.setText(BUTTON_AFTER_PUSHED_LABEL + trackingId));
    }
}
