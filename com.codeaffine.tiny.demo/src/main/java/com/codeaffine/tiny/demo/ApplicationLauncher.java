package com.codeaffine.tiny.demo;

import static com.codeaffine.tiny.star.ApplicationRunner.newApplicationRunnerBuilder;
import static org.slf4j.LoggerFactory.getLogger;

import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.UUID;
import lombok.extern.java.Log;

public class ApplicationLauncher extends AbstractEntryPoint implements ApplicationConfiguration {

    private static final String TEXT = "Hello World!\n\nGive me something unique:";

    public static void main(String[] args) {
        newApplicationRunnerBuilder()
            .withApplicationConfiguration(new ApplicationLauncher())
            .build()
            .run();
    }

    @Override
    public void configure(Application application) {
        application.addEntryPoint("/ui", ApplicationLauncher.class, null);
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
