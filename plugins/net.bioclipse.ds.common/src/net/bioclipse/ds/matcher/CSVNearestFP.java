package net.bioclipse.ds.matcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import net.bioclipse.cdk.business.Activator;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.domain.IBioObject;
import net.bioclipse.core.util.LogUtils;
import net.bioclipse.ds.model.AbstractDSMolModel;
import net.bioclipse.ds.model.AbstractDSTest;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;

/**
 * 
 * @author Ola Spjuth
 *
 */
public class CSVNearestFP extends AbstractDSMolModel implements IDSTest{

    private static final String TANIMOTO_PARAMETER="distance.tanimoto";
    private static final String[] CONTENT_HEADERS = new String[]{"SMILES","TYPE", "VALUE", "TARGET_TYPE", 
    													 "TARGET_NAME", "BIO_SPECIES", 
    													 "BIO_EFFECT", "SWISSP_ID"};

    private static final Logger logger = Logger.getLogger(CSVNearestFP.class);
    private float tanimoto;

	Map<BitSet, List<Integer>> lookup = new HashMap<BitSet, List<Integer>>();
	Map<Integer, List<String>> content = new HashMap<Integer, List<String>>();

	SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
	Fingerprinter fpr = new Fingerprinter(1024);

	
	@Override
	public List<String> getRequiredParameters() {

		List<String> params = new ArrayList<String>();
		params.add("fp2id");
		params.add("id2all");
		params.add(TANIMOTO_PARAMETER);
		return null;
	}

	@Override
	public void initialize(IProgressMonitor monitor) throws DSException {

		super.initialize(monitor);

        String tanimotoString=getParameters().get( TANIMOTO_PARAMETER );
        if (tanimotoString!=null && tanimotoString.length()>0){
            //Parse tanimoto string into a Float
            tanimoto=Float.parseFloat( tanimotoString );
        }else{
            logger.error("Error parsing required parameter: " 
                         + TANIMOTO_PARAMETER + " in test: " + getId());
        }

		String fp2id = getFileFromParameter("fp2id" );
		String id2all = getFileFromParameter( "id2all" );

		//Read files
		try {
			BufferedReader fp2idreader = new BufferedReader(new FileReader(new File(fp2id)));
			String line=fp2idreader.readLine();
			while (line!=null){
				String[] parts = line.split(";");
				BitSet fp = decode(parts[0]);
				int molid = Integer.parseInt(parts[1]);
				
				List<Integer> ids;
				if (lookup.containsKey(fp)){
					ids=lookup.get(fp);
				}else{
					ids=new ArrayList<Integer>();
					lookup.put(fp, ids);
				}
				ids.add(molid);

				line=fp2idreader.readLine();
			}
		} catch (IOException e) {
			throw new DSException("processing file: " + fp2id + " - " + e.getMessage());
		}
		
		
		BufferedReader id2allreader;
		try {
			id2allreader = new BufferedReader(new FileReader(new File(id2all)));
			String line = id2allreader.readLine(); //Skip first line
			line = id2allreader.readLine();
			while (line!=null){
				int ix = line.indexOf(";");
				String smdl_id=line.substring(0,ix);
				String rest=line.substring(ix+1);
				int smdl = Integer.parseInt(smdl_id);
				
				List<String> entries;
				if (content.containsKey(smdl)){
					entries=content.get(smdl);
				}else{
					entries=new ArrayList<String>();
					content.put(smdl, entries);
				}
				entries.add(rest);
				
				line = id2allreader.readLine();
			}
		} catch (IOException e) {
			throw new DSException("processing file: " + id2all + " - " + e.getMessage());
		}
		
		
		System.out.println("Finieshed inititalizing CSVNearest model: " + getName()); 

	}

	@Override
	protected List<? extends ITestResult> doRunTest(IBioObject input,
			IProgressMonitor monitor) {
		
		if (!(input instanceof ICDKMolecule))
			return returnError("Input is not a Molecule", "");
		ICDKMolecule cdkmol = (ICDKMolecule) input;

		//Store results here
		ArrayList<ExternalMoleculeMatch> results=new ArrayList<ExternalMoleculeMatch>();

		//The CDK manager is needed for tanimoto
		ICDKManager cdk=Activator.getDefault().getJavaCDKManager();

		try {
			
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(cdkmol.getAtomContainer());
			AtomContainerManipulator.convertImplicitToExplicitHydrogens(cdkmol.getAtomContainer());
			BitSet molFP = fpr.getFingerprint(cdkmol.getAtomContainer());

//			BitSet molFP = cdkmol.getFingerprint( IMolecule.Property.
//					USE_CALCULATED );
			logger.debug( "FP: " + molFP);
			
			
			for (BitSet efp : lookup.keySet()){
				
				if (efp.size()!=molFP.size()){
	                logger.error( "FP size=" 
	                             + efp.size() + 
	                             " but molecule searched for has FP size=" 
	                             + molFP.size());
	            }
				
//				logger.debug("efp: " + efp);

				
				float calcTanimoto = cdk.calculateTanimoto( efp, molFP );
				if (calcTanimoto >= tanimoto){

					//A hit is found
					for (Integer smdl : lookup.get(efp)){

						//A list of interactions matched by this FP
						//All have same SMILES, this is redundant info in file
						List<String> props = content.get(smdl);
						Map<String, Map<String, String>> parsedProps = parseProps(props);
						
						int ix = props.get(0).indexOf(";");
						String smiles = props.get(0).substring(0,ix);

						ICDKMolecule matchmol = cdk.fromSMILES(smiles);
						
						ExternalMoleculeMatch match = new ExternalMoleculeMatch(
								"" + smdl, matchmol, calcTanimoto,  ITestResult.INFORMATIVE);

						match.setProperties(parsedProps);

						results.add( match);
					}
					
				}

//				if (monitor.isCanceled())
//					return returnError( "Cancelled","");

			}

		} catch ( Exception e ) {
			LogUtils.debugTrace( logger, e );
			return returnError( "Test failed: " , e.getMessage());
		}

		return results;
	}


	private Map<String, Map<String, String>> parseProps(List<String> props) {

		//map from property category, then name > value
		Map<String, Map<String,String>> ret = new HashMap<String, Map<String,String>>();
		
		int cnt=0;
		for (String prop : props){
			String[] parts = prop.split(";");

			Map<String,String> target = new HashMap<String, String>();
			ret.put("Target: " + parts[4], target);
			for (int i=1; i<parts.length; i++){
				String propname=CONTENT_HEADERS[i];
				String propval=parts[i];
				target.put(propname, propval);
			}

			cnt++;
		}
		
		return ret;
	}

	public static BitSet decode( String value ) {
		byte[] bytes = new Base64().decode( value.getBytes() );
		BitSet set = new BitSet(1024);
		for(int i=0;i<bytes.length*8;i++) {
			if( (bytes[bytes.length-i/8-1] & (1<<(i%8))) > 0) {
				set.set( i );
			}
		}
		return set;
	}
}
