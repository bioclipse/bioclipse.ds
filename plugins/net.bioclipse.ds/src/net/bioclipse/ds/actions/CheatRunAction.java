package net.bioclipse.ds.actions;

import net.bioclipse.ds.ui.views.DSView;
import net.bioclipse.scripting.ui.actions.ScriptAction;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;


public class CheatRunAction extends Action implements ICheatSheetAction {

    private static final Logger logger = Logger.getLogger(ScriptAction.class);

    public void run( String[] params, ICheatSheetManager manager ) {

        DSView.getInstance().fireExternalRun();
        
    }
    
}
