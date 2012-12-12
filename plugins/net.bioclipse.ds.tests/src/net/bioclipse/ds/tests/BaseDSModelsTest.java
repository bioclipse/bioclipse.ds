package net.bioclipse.ds.tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.junit.Test;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;

public abstract class BaseDSModelsTest {
	
	IDSManager ds = Activator.getDefault().getJavaManager();
	ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();


	public void assertExactMatchSMILES(String smiles, String model, int status) throws BioclipseException, DSException{

		IMolecule mol = cdk.fromSMILES( smiles );
		assertExactMatch(mol, model, status);

	}

	public void assertExactMatch(IMolecule mol, String model, int status) throws BioclipseException, DSException{

		List<ITestResult> ret = ds.runTest( model, mol );
		assertEquals( 1, ret.size() ); //One match expected
		assertEquals(status,ret.get(0).getClassification());
		
	}

	public void assertNNSMILES(String smiles, String model, int noNN) throws BioclipseException, DSException{

		IMolecule mol = cdk.fromSMILES( smiles );
		assertNN(mol, model, noNN);
		
	}

	public void assertNN(IMolecule mol, String model, int noNN) throws BioclipseException, DSException{

		List<ITestResult> ret = ds.runTest( model, mol );
		assertEquals( noNN, ret.size());
		
	}

	public void assertStructuralAlert(IMolecule mol, String model, String alert) throws BioclipseException, DSException{

		List<ITestResult> ret = ds.runTest( model, mol );
		assertEquals( 1, ret.size());
		assertEquals(alert, ret.get(0).getName());
		
	}
	
	
//	/**
//	 * Rematch all in original data to verify at least one exact match
//	 * @throws InvocationTargetException 
//	 */
//	@Test
//	public void testAmesExacReMatchSDF() throws BioclipseException, DSException, URISyntaxException, MalformedURLException, IOException, CoreException, InvocationTargetException{
//	
//	IDSManager ds = Activator.getDefault().getJavaManager();
//	ICDKManager cdk = 
//		net.bioclipse.cdk.business.
//		Activator.getDefault().getJavaCDKManager();
////	ISignaturesManager signatures=net.bioclipse.ds.signatures.Activator.getDefault().getJavaSignaturesManager();
//
//	//Exact match should work for all original molecules
//	URI uri = getClass().getResource("/data/bursi_nosalts.sdf").toURI();
//	URL url = FileLocator.toFileURL(uri.toURL());
//
//	Iterator<ICDKMolecule> it = cdk.createMoleculeIterator(url.getFile());
//	
//	int cnt=0;
//	Map<Integer, List<ITestResult>> errors=new HashMap<Integer, List<ITestResult>>();
//	while (it.hasNext()){
//		ICDKMolecule mol =it.next();
//		
//		List<ITestResult> ret = ds.runTest( exactModel, mol );
//		
//		if (ret.size()!=1){
//			errors.put(cnt, ret);
//			String ms = signatures.generateMoleculeSignature(mol);
//			System.out.println("Mol anomaly, index " + cnt + " with signature: " + ms);
//		}
////		assertTrue("SDF index " + cnt +" did not have an exact match",
////				ret.size()>=1 );
//		cnt++;
//	}
//
//	//Just debug out anomalies, where we have multiple matches
//	System.out.println("We have " + errors.size() + " anomalies.");
//	for (Integer i : errors.keySet()){
//		System.out.println("Mol: " + i + " had anomalities...");
//		if (errors.get(i)==null || errors.get(i).size()==0)
//			System.out.println("    ...no exact matches were found");
//		else{
//			for (ITestResult tres : errors.get(i)){
//				ExternalMoleculeMatch ematch=(ExternalMoleculeMatch)tres;
//				String matchmolsign="";
//				try{
//					matchmolsign=signatures.generateMoleculeSignature(ematch.getMatchedMolecule());
//				}catch (Exception e){
//					matchmolsign="Error: " + e.getMessage();
//				}
//				System.out.println("   Match: " + ematch.getName() + " with sign: " + matchmolsign);
//			}
//		}
//	}
//
//
////	//Just test a mol with no matches; adrenaline
////	List<ITestResult> ret = ds.runTest( exactModel, 
////			cdk.fromSMILES("Oc1ccc(cc1O)[C@@H](O)CNC") );
////	assertEquals( 0, ret.size() );
//		
//}


	/**
	 * 
	 * @param path to file
	 * @return
	 */
	public ICDKMolecule loadMolecule(String path) 
	throws MalformedURLException, IOException, BioclipseException, 
	CoreException, URISyntaxException {
		URI uri = getClass().getResource(path).toURI();
		URL url = FileLocator.toFileURL(uri.toURL());
		return cdk.loadMolecule(url.getFile());
	}

}
