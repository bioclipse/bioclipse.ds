/* *****************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.impl.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;
import net.bioclipse.ds.report.AbstractTestReportModel;
import net.bioclipse.ds.report.DSRow;
import net.bioclipse.ds.report.StatusHelper;
import net.bioclipse.ds.ui.ImageHelper;


public class ExactMatchReportModel extends AbstractTestReportModel{

    
    public ExactMatchReportModel() {
    }
    
    public List<DSRow> extractRows(TestRun run){

        //for a testrunm transform to a DSRow with a structure image and paams
        List<DSRow> newrows=new ArrayList<DSRow>();
        if (run.getMatches()==null) return newrows;
        for (int i=0; i<run.getMatches().size(); i++){
            
            ITestResult match = run.getMatches().get( i );
            if ( match instanceof ExternalMoleculeMatch ) {

                Map<String, String> params=new HashMap<String, String>();
                params.put("name",  match.getName());
                params.put("classification",  StatusHelper.statusToString(
                                                      match.getClassification()));

                IMolecule bcmol=(IMolecule) match.getAdapter( IMolecule.class );
                byte[] structureImage = null;
                try {
                    structureImage = ImageHelper.createImage(bcmol, null);
                } catch ( BioclipseException e ) {
                    e.printStackTrace();
                }
                DSRow row=new DSRow(structureImage, params );
                newrows.add( row );

            }
            

          }
        
        return newrows;
    }

}
