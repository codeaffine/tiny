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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;

import static com.codeaffine.tiny.demo.ApplicationConfigurations.configureApplication;
import static com.codeaffine.tiny.demo.FormDatas.attach;

class ZOrder extends AbstractEntryPoint implements ApplicationConfiguration {

    @Override
    public void configure(Application application) {
        configureApplication(application, getClass());
    }

    @Override
    protected void createContents(Composite parent) {
        parent.setLayout(new FormLayout());
        Composite canvas = new Composite(parent, SWT.NONE);
        canvas.setLayout(new FormLayout());
        attach(canvas).atTop().atLeft().atRight().fromBottom(30);
        Label labelA = createLabel(canvas, 15, 45, "A", newColor(204, 0, 0));
        Label labelB = createLabel(canvas, 35, 65, "B", newColor(51, 204, 51));
        Label labelC = createLabel(canvas, 55, 85, "C", newColor(0, 153, 255));
        Button aboveA = createButton(parent, 10, 36, "B above A", evt1 -> labelB.moveAbove(labelA));
        Button belowA = createButton(parent, aboveA, 10, 36, "B below A", evt1 -> labelB.moveBelow(labelA));
        Button aboveAll = createButton(parent, 37, 63, "B above all", evt -> labelB.moveAbove(null));
        Button belowAll = createButton(parent, aboveAll, 37, 63, "B below all", evt -> labelB.moveBelow(null));
        Button aboveC = createButton(parent, 64, 90, "B above C", evt -> labelB.moveAbove(labelC));
        Button belowC = createButton(parent, aboveC, 64, 90, "B below C", evt -> labelB.moveBelow(labelC));
        parent.setTabList(new Control[] {
            aboveA, aboveAll, aboveC, belowA, belowAll, belowC
        });
    }

    private Label createLabel(Composite canvas, int topAndLeft, int rightAndBottom, String text, Color background) {
        Label result = new Label(canvas, SWT.BORDER | SWT.CENTER);
        result.setFont(new Font(Display.getCurrent(), "Verdana, \"Lucida Sans\", Arial, Helvetica, sans-serif", 72, SWT.BOLD));
        result.setForeground(newColor(255, 255, 255));
        attach(result).fromTop(topAndLeft).fromLeft(topAndLeft).fromRight(100 - rightAndBottom).fromBottom(100 - rightAndBottom);
        result.setText(text);
        result.setBackground(background);
        return result;
    }

    private Button createButton(Composite parent, int left, int right, String text, Listener listener) {
        Button result = new Button(parent, SWT.PUSH);
        attach(result).fromTop(80).fromLeft(left).fromRight(100 - right);
        result.setText(text);
        result.addListener(SWT.Selection, listener);
        return result;
    }

    private Button createButton(Composite parent, Button aboveA, int left, int right, String text, Listener listener) {
        Button result = new Button(parent, SWT.PUSH);
        attach(result).atTopTo(aboveA, 1).fromLeft(left).fromRight(100 - right);
        result.setText(text);
        result.addListener(SWT.Selection, listener);
        return result;
    }

    private static Color newColor(int red, int green, int blue) {
        return new Color(Display.getCurrent(), new RGB(red, green, blue));
    }
}
