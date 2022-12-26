package com.codeaffine.tiny.star;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;

import java.util.UUID;

class TestEntryPoint extends AbstractEntryPoint {

    private static final String TEXT = "Hello World!\n\nGive me something unique:";

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
