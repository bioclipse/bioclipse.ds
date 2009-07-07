package net.bioclipse.ds.dblookup.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.TestRun;
import net.bioclipse.ds.model.report.AbstractTestReportModel;
import net.bioclipse.ds.model.report.DSRow;
import net.bioclipse.ds.model.report.ReportHelper;


public class DBNNmodel extends AbstractTestReportModel{

   
    public DBNNmodel() {
        super();
    }
    
    public List<DSRow> extractRows(TestRun run){

        DecimalFormat twoDForm = new DecimalFormat("#.##");

        //for a testrunm transform to a DSRow with a structure image and paams
        List<DSRow> newrows=new ArrayList<DSRow>();
        if (run.getMatches()==null) return newrows;

        for (int i=0; i<run.getMatches().size(); i++){
            
            ITestResult match = run.getMatches().get( i );
            if ( match instanceof ExternalMoleculeMatch ) {
                ExternalMoleculeMatch extmolmatch = (ExternalMoleculeMatch) match;

                Map<String, String> params=new HashMap<String, String>();
                params.put("name",  match.getName());
                params.put("classification",  ReportHelper.statusToString(
                                                      match.getClassification()));
                params.put("tanimoto",  twoDForm.format( 
                                                  extmolmatch.getSimilarity()));

                IMolecule bcmol=(IMolecule) match.getAdapter( IMolecule.class );
                byte[] structureImage = null;
                try {
                    structureImage = ReportHelper.createImage(bcmol, null);
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
