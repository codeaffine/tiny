package com.codeaffine.tiny.demo;

import com.codeaffine.tiny.star.cli.CommandLineInterface;
import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.UUID;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;

public class DemoApplication extends AbstractEntryPoint {

    private static final String TEXT = "Hello World!\n\nGive me something unique:";

    public static void main(String[] args) {
        newApplicationServerBuilder(DemoApplication::configure)
            .withLifecycleListener(new CommandLineInterface())
            .withApplicationIdentifier(DemoApplication.class.getName())
            .build()
            .start();
    }

    private static void configure(Application application) {
        application.addEntryPoint("/ui", DemoApplication.class, null);
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
