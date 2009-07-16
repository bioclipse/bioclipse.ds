/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.actions;

import net.bioclipse.ds.ui.views.DSView;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;


public class CheatRunAction extends Action implements ICheatSheetAction {

//    private static final Logger logger = Logger.getLogger(ScriptAction.class);

    public void run( String[] params, ICheatSheetManager manager ) {

        DSView.getInstance().fireExternalRun();
        
    }
    
}
