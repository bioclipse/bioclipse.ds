package net.bioclipse.ds.cpdb.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.AtomContainerSelection;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.signatures.Activator;
import net.bioclipse.ds.signatures.business.ISignaturesManager;
import net.bioclipse.ds.signatures.business.SignaturesManager;


public class SignMatchImpl extends AbstractDSTest implements IDSTest{

    
    //Hardcoded data
    public static final String[] signListHeight1 = {"[P]([O][O][O][O])", "[N]([C][N][O])", "[N]([C][C][N])", "[C]([C][Cl])", "[S]([C][S])", "[O]([P])", "[S]([C][P])", "[N]([N][O])", "[N]([C][C][P])", "[C]([N][O])", "[P]([O][O][O][S])", "[S]([P])"};
    public static final String[] signActHeight1 = {"active", "active", "active", "active", "inactive", "active", "inactive", "active", "active", "active", "inactive", "inactive"};
    public static final int nrSignsHeight1 = 12;
    public static final String[] signListHeight2 = {"[O]([C]([C][O])[C])", "[O]([C]([C])[C]([C]))", "[O]([C]([C][C])[C]([C][N]))", "[C]([C]([C])[C]([C][Cl])[Cl])", "[C]([C]([C])[C]([N][O]))", "[C]([C]([Cl])[N]([C][C]))", "[N]([C]([O][O]))", "[N]([C]([C][C])[O])", "[C]([C]([N])[N]([C][N]))", "[C]([N]([C][N])[N][O])", "[C]([C]([C][C])[C]([C][C])[N]([C]))", "[C]([N]([C])[N]([C][C])[O])", "[C]([C][C]([N])[O])", "[N]([N]([C][C]))", "[C]([C]([C][C][O])[O]([C])[O])", "[S]([C]([C])[C]([N][N]))", "[O]([N]([C][N]))", "[C]([C]([C][O])[C]([O][O])[O])", "[O]([C]([C])[C]([N][O]))", "[C]([C]([C])[C]([C][N])[C]([C][N]))", "[C]([C]([C][O])[N]([C][N]))", "[C]([C]([O][O])[C]([C][O])[O])", "[C]([C]([O])[N]([C][N]))", "[C]([C]([O][O])[O]([C]))", "[C]([N]([C][N]))", "[N]([C]([C][O])[O][O])", "[C]([C]([C][O])[O]([C])[O]([C]))", "[N]([C]([C])[N]([C][C]))", "[C]([C][N]([C][N]))", "[C]([N]([C][N])[N]([C])[O])", "[O]([C]([N]))", "[C]([C]([C])[N]([O][O])[O]([C]))", "[Cl]([C]([C]))", "[O]([N]([N]))", "[C]([C]([C])[N]([C][N]))", "[C]([C]([S])[C]([C][O])[N]([C]))", "[C]([C]([C])[C]([C][S])[N]([C]))", "[C]([N][N]([C][N])[O])", "[N]([C]([C][S])[O][O])", "[N]([C]([N][O])[C]([C]))", "[C]([C][O]([P]))", "[C]([C]([C][Cl])[C]([C][Cl]))", "[C]([C]([C])[C]([C][O])[Cl])"};
    public static final String[] signActHeight2 = {"inactive", "active", "active", "inactive", "active", "active", "active", "active", "active", "active", "inactive", "inactive", "active", "active", "active", "active", "active", "inactive", "active", "inactive", "active", "inactive", "active", "inactive", "active", "active", "inactive", "active", "active", "active", "active", "active", "active", "active", "active", "active", "inactive", "active", "active", "active", "inactive", "inactive", "inactive"};
    public static final int nrSignsHeight2 = 43;
    public static final String[] signListHeight3 = {"[O]([C]([C]([O][O])[C]([C][O])))", "[C]([C]([C]([C,1][N]))[C]([C]([C,1])[O]([C])))", "[C]([C]([C,1]([C,2]))[C]([C]([S])[N]([C]))[O]([C,2]([N])))", "[O]([C]([N][N]([C][N])))", "[C]([C][C]([N]([C][N])))", "[C]([C]([C]([C,1][N]))[C]([C]([C,1])))", "[O]([C]([C]([C][C]))[C]([C]([C][C][O])[O]))", "[C]([C]([C]([C,1][Cl])[Cl])[C]([C]([C,1][Cl])[Cl])[Cl])", "[C]([C]([O]([P])))", "[Cl]([C]([C]([C])[C]([C][O])))", "[O]([N]([C]([C][O])[O]))", "[C]([C]([C]([C,1]))[C]([C]([C][N])[C]([C,1])))", "[C]([C]([C]([C]))[C]([N]([C][N])))", "[C]([C]([C]([C]))[C]([C]([C]))[C])", "[O]([C]([C]([C,1][O])[O]([C]))[C]([C]([C,1][O])[C]([O])))", "[C]([C]([C]([C,1][S]))[C]([C]([C,1])[Cl]))", "[O]([C]([N]([C][N])[N]([C])))", "[O]([C]([N]([C][N])[O]([C])))", "[C]([C]([C]([C][C,1]))[C]([C]([C][C,1])))", "[Cl]([C]([C]([C])[C]([C][Cl])))", "[C]([C]([C]([C]))[C]([C]([C])[C]))", "[C]([C]([C]([C,1])[Cl])[C]([C]([C,1][N])[C]([C][N])))", "[O]([C]([N]([C])[N]([C][C])))", "[O]([N]([C]([C][S])[O]))", "[C]([C]([C]([C,1]))[C]([C]([C,1][Cl]))[Cl])", "[C]([C]([C]([C,1]))[C]([C]([C,1])[N]([C])))", "[C]([C]([C]([N])))", "[C]([C]([C]([C,1][N]))[C]([C]([C,1])[N]))", "[C]([C]([C]([N])[O]))", "[C]([C]([C]([C][O])[O])[O])", "[O]([C]([C]([C][C][O])[O]([C])))", "[C]([C]([C,2]([N][O,1]))[C]([C]([C][N])[O,1]))", "[O]([N]([C]([C][C])))", "[O]([C]([N]([C][N])[N]))", "[C]([C]([C]([C]))[C]([C]([C][C])))", "[O]([C]([C]([C][O])[C]([O][O])))", "[C]([C]([C]([C][O])[O]([C])))", "[C]([O]([C]([C][O])))", "[C]([C]([N]([C][N]))[O])", "[C]([C]([N]([C][N])))", "[O]([C]([C][C]([N])))", "[O]([C]([N][O]([C])))", "[N]([C]([N]([C][N])[O]))"};
    public static final String[] signActHeight3 = {"inactive", "active", "active", "active", "active", "inactive", "active", "active", "inactive", "inactive", "active", "inactive", "active", "inactive", "inactive", "inactive", "active", "active", "inactive", "inactive", "inactive", "inactive", "inactive", "active", "inactive", "inactive", "active", "inactive", "active", "inactive", "active", "active", "active", "active", "inactive", "inactive", "inactive", "inactive", "active", "active", "active", "active", "active"};
    public static final int nrSignsHeight3 = 43;
    public static final String[] signListHeight4 = {"[O]([C]([C]([N]([C][N]))))", "[C]([C]([C]([C,1]([O])))[C]([C]([C,1])))", "[O]([C]([C]([C]([C][O])[O])))", "[C]([C]([C]([C,1]([C])[N]([C][C])))[C]([C]([C,1])[Cl]))", "[C]([C]([C]([C]([C])[C]))[C]([C]([C]([C])[C])))", "[Cl]([C]([C]([C]([C,1]))[C]([C]([C,1][Cl]))))", "[C]([C]([C]([C]([C]))[C]([C]([C]))))", "[Cl]([C]([C]([C]([C,1][Cl])[Cl])[C]([C]([C,1][Cl])[Cl])))", "[C]([C]([C]([C,1]))[C]([C]([C,1][N]([C]))))", "[C]([C]([C]([C,1]([S])))[C]([C]([C,1]))[Cl])"};
    public static final String[] signActHeight4 = {"active", "inactive", "inactive", "inactive", "inactive", "inactive", "inactive", "active", "inactive", "inactive"};
    public static final int nrSignsHeight4 = 10;
    public static final String[] signListHeight5 = {"[C]([C]([C]([C,1]([N]([C]))))[C]([C]([C,1])))", "[Cl]([C]([C]([C]([C,1]([S])))[C]([C]([C,1]))))"};
    public static final String[] signActHeight5 = {"inactive", "inactive"};
    public static final int nrSignsHeight5 = 2;
    

    
    /*
     * Atom Nr > list of (height > result)
     * 
     * ex
     *    1 >   (1,-1)
     *    2 >   (1,1)
     *    3 >   (1,-1)
     *    1 >   (2,-1)
     *    2 >   (2,-1)
     *    3 >   (2,1)
     * 
     */
    
    /**
     * Loop over all query signatures, match against existing, add 
     * results per atom
     * @param querySignatures
     * @param signHeightList
     * @param atomToClass
     * @param i 
     * @return
     */
    public Map<Integer, List<Map<Integer, Integer>>> getAtoms(List<String> querySignatures, 
                                                String[] signHeightList, 
                                                Map<Integer, List<Map<Integer, Integer>>> atomToClass, 
                                                int height){

        //Holds the height>results map
        int atomNr = 0;
        for (String qrysign : querySignatures){
            int matchNr = 0;
            Map<Integer, Integer> heightMap = new HashMap<Integer, Integer>();
            
            for (String matchsign : signHeightList){
                if (qrysign == matchsign){
                    //If first height hit, make room
                    if (!atomToClass.containsKey( atomNr )){
                        //Atom Nr > list of (height > result)
                        atomToClass.put( atomNr, new ArrayList<Map<Integer, Integer>>() );
                        atomToClass.get( atomNr ).add( heightMap );
                    }
                   
                    if (signHeightList[matchNr] == "active"){
                        heightMap.put( height, 1 );
                    }
                    else{
                        heightMap.put( height, 2 );
                    }
                }
                matchNr++;
            }
            atomNr++;
        }
        return atomToClass;
    }

    public static void colorNeighbors(int centerAtom,int height, String color){
      //for each ceter atom loop thru neighbors and color them
      
    }
    
    //return highlight atoms, atom, color
    //Match
    
    
    
    
    
    @Override
    protected List<? extends ITestResult> doRunTest( ICDKMolecule cdkmol,
                                                     IProgressMonitor monitor ) {

        ISignaturesManager sign = Activator.getDefault()
                .getJavaSignaturesManager();
        
        try {
            //Calculate signatures for height 1-5
            List<List<String>> signMap=new ArrayList<List<String>>();
            for (int i= 1; i<6; i++){
                signMap.add( sign.generate( cdkmol, i ));
            }

            //Hold results by mapping atom number to list of [-1,1] 
            //representing [inactive,active]
            Map<Integer, List<Map<Integer, Integer>>> atomToClass = 
                    new HashMap<Integer, List<Map<Integer,Integer>>>();
            
            //for each sign height match qrysigns with matchSigns
            atomToClass = getAtoms(signMap.get( 0 ),signListHeight1, atomToClass, 1);
            atomToClass = getAtoms(signMap.get( 1 ),signListHeight2, atomToClass, 2);
            atomToClass = getAtoms(signMap.get( 2 ),signListHeight3, atomToClass ,3);
            atomToClass = getAtoms(signMap.get( 3 ),signListHeight4, atomToClass, 4);
            atomToClass = getAtoms(signMap.get( 4 ),signListHeight5, atomToClass, 5);
            
            //TODO: continue...
            
        } catch ( BioclipseException e ) {
            return returnError( e.getMessage(),"");
        }

        //TODO: TBC
        return null;
    }

    public void initialize( IProgressMonitor monitor ) throws DSException {

        // TODO TBC
        
    }

}
