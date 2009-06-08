package net.bioclipse.ds.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * A handler to handle Report invocation
 * @author ola
 *
 */
public class ReportHandler extends AbstractHandler{

    public Object execute( ExecutionEvent event ) throws ExecutionException {

        System.out.println("REPOOOOOOORT!");
        return null;
    }
}
