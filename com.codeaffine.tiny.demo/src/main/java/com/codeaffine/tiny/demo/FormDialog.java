/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

import static com.codeaffine.tiny.demo.FormDatas.*;
import static com.codeaffine.tiny.demo.ApplicationConfigurations.*;
import static java.lang.Math.*;

public class FormDialog extends AbstractEntryPoint implements ApplicationConfiguration {

    private static final int MARGIN = 8;

    @Override
    public void configure(Application application) {
        configureApplication(application, getClass());
    }

    @Override
    protected void createContents(Composite parent) {
        Shell shell = createShell(parent);
        Label label = createLabel(shell);
        List list = createList(shell);
        Button buttonOk = createButton(shell, "OK");
        Button buttonCancel = createButton(shell, "Cancel");
        layoutControls(label, buttonCancel, buttonOk, list);
        registerListeners(buttonOk, shell, list, buttonCancel);
        openShell(shell);
        list.setFocus ();
    }

    private static Shell createShell(Composite parent) {
        Shell result = new Shell (parent.getShell(), SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE);
        result.setText("Form Dialog");
        FormLayout formLayout = new FormLayout ();
        formLayout.marginWidth = MARGIN;
        formLayout.marginHeight = MARGIN;
        result.setLayout (formLayout);
        return result;
    }

    private static Label createLabel(Shell shell) {
        Label result = new Label (shell, SWT.WRAP);
        result.setText ("This is a long text string that will wrap when the dialog is resized.");
        return result;
    }

    private static List createList(Shell shell) {
        List result = new List (shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        result.setItems (new String[]{"Item 1", "Item 2"});
        result.select(0);
        return result;
    }

    private static Button createButton(Shell shell, String text) {
        Button result = new Button (shell, SWT.PUSH);
        result.setText (text);
        return result;
    }

    private static void layoutControls(Label label, Button buttonCancel, Button buttonOk, List list) {
        attach(label).atLeft().atRight().atTop();
        attach(buttonCancel).atRight().atBottom();
        attach(buttonOk).atRightTo(buttonCancel, MARGIN).atBottom();
        attach(list).atLeft().atRight().atTopTo(label, MARGIN).atBottomTo(buttonCancel, MARGIN);
    }

    private static void registerListeners(Button buttonOk, Shell shell, List list, Button buttonCancel) {
        buttonOk.addListener(SWT.Selection, event -> showMessageBox(shell, "Selection", "You have selected " + list.getSelection()[0]));
        buttonCancel.addListener(SWT.Selection, event -> showMessageBox(shell, "Cancel", "Cancel of current operation requested"));
        shell.addListener (SWT.Resize, evt -> {
            Rectangle clientArea = shell.getClientArea ();
            shell.setSize(shell.computeSize(max(clientArea.width, 200), SWT.DEFAULT));
            shell.layout ();
        });
    }

    private static void openShell(Shell shell) {
        shell.setLocation(100, 100);
        shell.pack ();
        shell.getDisplay().asyncExec(shell::open);
    }

    private static void showMessageBox(Shell shell, String title, String message) {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        messageBox.setText(title);
        messageBox.setMessage(message);
        messageBox.open(returnCode -> {});
    }
}
