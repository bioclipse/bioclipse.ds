package net.bioclipse.ds.cpdb.tests;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;
import net.bioclipse.ds.signatures.business.ISignaturesManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.junit.Test;

/**
 * 
 * @author ola
 */
public class TestCPDBPredictions {


	@Test
	public void testCPDBExactMatch() throws BioclipseException, DSException, URISyntaxException, MalformedURLException, IOException, CoreException{

		IDSManager ds = Activator.getDefault().getJavaManager();
		
		//Exact match should work for original molecules
		List<ICDKMolecule> mols = loadSDF("/data/cpdb.sdf");
		int cnt=0;
		System.out.print("Total: " + mols.size() + "; done");
		Map<Integer, List<ITestResult>> errors=new HashMap<Integer, List<ITestResult>>();
		for (ICDKMolecule mol : mols){
			List<ITestResult> ret = ds.runTest( "cpdb.lookup.exact", mol );
			
			if (ret.size()!=1){
				errors.put(cnt, ret);
			}
			assertTrue("SDF index " + cnt +" did not have an exact match",
					ret.size()>=1 );
			cnt++;
		}

		//Just debug out anomalies, where we have multiple matches
		ISignaturesManager signatures=net.bioclipse.ds.signatures.Activator.getDefault().getJavaSignaturesManager();
		System.out.println("We have " + errors.size() + " anomalies.");
		for (Integer i : errors.keySet()){
			ICDKMolecule mol = mols.get(i);
			String molsign=signatures.generateMoleculeSignature(mol);
			System.out.println("Mol: " + i + " generated molsign: " + molsign);
			if (errors.get(i)==null || errors.get(i).size()==0)
				System.out.println("  but no exact matches were found");
			else{
				for (ITestResult tres : errors.get(i)){
					ExternalMoleculeMatch ematch=(ExternalMoleculeMatch)tres;
					String matchmolsign=signatures.generateMoleculeSignature(ematch.getMatchedMolecule());
					System.out.println("   Match: " + ematch.getName() + " with sign: " + matchmolsign);
				}
			}
		}


		//Just test a mol with no matches; adrenaline
		ICDKManager cdk = 
			net.bioclipse.cdk.business.
			Activator.getDefault().getJavaCDKManager();

		List<ITestResult> ret = ds.runTest( "cpdb.lookup.exact", 
				cdk.fromSMILES("Oc1ccc(cc1O)[C@@H](O)CNC") );
		assertEquals( 0, ret.size() );
		
	}
	


	/**
	 * Read 
	 * @param path to molecule on Classpath
	 * @return ICDKMolecule
	 */
	private ICDKMolecule loadMolecule(String path)
	throws IOException, MalformedURLException, BioclipseException,
	CoreException, URISyntaxException {

		URI uri = getClass().getResource(path).toURI();
		ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
		URL url = FileLocator.toFileURL(uri.toURL());
		String p_path = url.getFile();
		return cdk.loadMolecule(p_path);
	}

	/**
	 * Read 
	 * @param path to and SDF on Classpath
	 * @return List<ICDKMolecule>
	 */
	private List<ICDKMolecule> loadSDF(String path)
	throws IOException, MalformedURLException, BioclipseException,
	CoreException, URISyntaxException {

		URI uri = getClass().getResource(path).toURI();
		ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
		URL url = FileLocator.toFileURL(uri.toURL());
		String p_path = url.getFile();
		return cdk.loadMolecules(p_path);
	}


}
