package com.codeaffine.tiny.star;

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
