package net.bioclipse.ds.actions;

import net.bioclipse.scripting.ui.actions.ScriptAction;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.handlers.IHandlerService;


public class CheatReportAction extends Action implements ICheatSheetAction {

    private static final Logger logger = Logger.getLogger(ScriptAction.class);

    private static final String HANDLER_ID = "net.bioclipse.ds.ui.report";

    public void run( String[] params, ICheatSheetManager manager ) {

        IWorkbench wb = PlatformUI.getWorkbench(); 
        if (wb != null) {
          Object serviceObject = wb.getAdapter(IHandlerService.class);
            if (serviceObject != null) {
              IHandlerService service = (IHandlerService)serviceObject;
              try {
                service.executeCommand(HANDLER_ID, new Event());
            } catch ( ExecutionException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch ( NotDefinedException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch ( NotEnabledException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch ( NotHandledException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            }
        }

        
    }
    
}
