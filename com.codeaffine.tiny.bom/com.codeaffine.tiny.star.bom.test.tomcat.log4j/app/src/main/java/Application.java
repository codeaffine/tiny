import org.eclipse.rap.rwt.application.AbstractEntryPoint;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static java.lang.System.getenv;

public class Application extends AbstractEntryPoint {

    public static void main( String[] args ) {
        newApplicationServerBuilder(Application::configure)
            .build()
            .start();
    }

    @Override
    protected void createContents(Composite composite) {
        new Label(composite, SWT.NONE).setText("Hello World");
    }

    private static void configure(org.eclipse.rap.rwt.application.Application application) {
        String entryPointPath = getenv().getOrDefault("ENTRY_POINT_PATH", "/ui");
        application.addEntryPoint(entryPointPath, Application.class, null);
    }
}
