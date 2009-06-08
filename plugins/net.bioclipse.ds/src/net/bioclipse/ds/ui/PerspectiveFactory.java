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
package net.bioclipse.ds.ui;

import net.bioclipse.cdk.jchempaint.view.JChemPaintView;
import net.bioclipse.ds.ui.views.DSView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class PerspectiveFactory implements IPerspectiveFactory {

    IPageLayout storedLayout;

    /**
     * This perspective's ID
     */
    public static final String ID_PERSPECTIVE =
        "net.bioclipse.ds.ui.perspective";

    public static final String ID_NAVIGATOR = 
        "net.bioclipse.navigator";

    /**
     * Create initial layout
     */
    public void createInitialLayout(IPageLayout layout) {

        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);
        layout.setFixed(false);
        layout.addPerspectiveShortcut(ID_PERSPECTIVE);

        //Add layouts for views
        IFolderLayout left_folder_layout =
            layout.createFolder(
                    "explorer",
                    IPageLayout.LEFT,
                    0.20f,
                    editorArea);

        IFolderLayout right_folder_layout =
            layout.createFolder(
                    "ds",
                    IPageLayout.RIGHT,
                    0.70f,
                    editorArea);

        IFolderLayout right_bottom_folder_layout =
            layout.createFolder( 
                    "dsbottom",
                    IPageLayout.BOTTOM,
                    0.70f,
                    "ds");

        IFolderLayout bottom_folder_layout =
            layout.createFolder(
                    "properties",
                    IPageLayout.BOTTOM,
                    0.70f,
                    editorArea);


        //Add views
        left_folder_layout.addView(ID_NAVIGATOR);
        bottom_folder_layout.addView(IPageLayout.ID_PROP_SHEET);
        right_folder_layout.addView(DSView.VIEW_ID);
        right_bottom_folder_layout.addView(JChemPaintView.VIEW_ID);

        //Add ShowView shortcuts
        layout.addShowViewShortcut(ID_NAVIGATOR);    
        layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);    
        layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);    
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);    
        layout.addShowViewShortcut(DSView.VIEW_ID);    
        layout.addShowViewShortcut(JChemPaintView.VIEW_ID);    

    }
}
