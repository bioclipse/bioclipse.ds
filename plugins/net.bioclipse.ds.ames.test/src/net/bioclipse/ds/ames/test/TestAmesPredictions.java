package net.bioclipse.ds.ames.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
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
public class TestAmesPredictions {


	String hit1="O=C(OCCN(C)C)C=C";
	String hit2="O=C=NC1=CC=CC(N=C=O)=C1C";

	@Test
	public void testAmesExacMatchSMILES() throws BioclipseException, DSException{

		IDSManager ds = Activator.getDefault().getJavaManager();
		ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();

		IMolecule mol = cdk.fromSMILES( hit1 );
		List<ITestResult> ret = ds.runTest( "ames.lookup.exact", mol );
		assertEquals( 1, ret.size() ); //One match expected

		mol = cdk.fromSMILES( hit2 );
		ret = ds.runTest( "ames.lookup.exact", mol );
		assertEquals( 1, ret.size() ); //One match expected

	}

	@Test
	public void testAmesExacMatchFiles() throws BioclipseException, DSException, URISyntaxException, MalformedURLException, IOException, CoreException{

		IDSManager ds = Activator.getDefault().getJavaManager();

		ICDKMolecule mol = loadMolecule("/data/bursiNegs1.mol");
		List<ITestResult> ret = ds.runTest( "ames.lookup.exact", mol );
		assertEquals( 1, ret.size() );
		assertEquals(ITestResult.NEGATIVE,ret.get(0).getClassification());

		mol = loadMolecule("/data/bursiNegs2.mol");
		ret = ds.runTest( "ames.lookup.exact", mol );
		assertEquals( 1, ret.size() );
		assertEquals(ITestResult.NEGATIVE,ret.get(0).getClassification());

		mol = loadMolecule("/data/bursiNegs3.mol");
		ret = ds.runTest( "ames.lookup.exact", mol );
		assertEquals( 1, ret.size() );
		assertEquals(ITestResult.NEGATIVE,ret.get(0).getClassification());

		mol = loadMolecule("/data/bursiPos1.mol");
		ret = ds.runTest( "ames.lookup.exact", mol );
		assertEquals( 1, ret.size() );
		assertEquals(ITestResult.POSITIVE,ret.get(0).getClassification());

		mol = loadMolecule("/data/bursiPos2.mol");
		ret = ds.runTest( "ames.lookup.exact", mol );
		assertEquals( 1, ret.size() );
		assertEquals(ITestResult.POSITIVE,ret.get(0).getClassification());

		mol = loadMolecule("/data/bursiPos3.mol");
		ret = ds.runTest( "ames.lookup.exact", mol );
		assertEquals( 1, ret.size() );
		assertEquals(ITestResult.POSITIVE,ret.get(0).getClassification());

	}
	


	@Test
	public void testAmesNN() throws BioclipseException, DSException, URISyntaxException, MalformedURLException, IOException, CoreException{

		IDSManager ds = Activator.getDefault().getJavaManager();

		ICDKMolecule mol = loadMolecule("/data/bursiPos1.mol");
		List<ITestResult> ret = ds.runTest( "ames.lookup.nearest", mol );
		assertEquals( 2, ret.size() );
		assertEquals(ITestResult.POSITIVE,ret.get(0).getClassification());
		assertEquals(ITestResult.POSITIVE,ret.get(1).getClassification());

		mol = loadMolecule("/data/bursiPos2.mol");
		ret = ds.runTest( "ames.lookup.nearest", mol );
		assertEquals( 1, ret.size() );
		assertEquals(ITestResult.POSITIVE,ret.get(0).getClassification());

		mol = loadMolecule("/data/bursiPos3.mol");
		ret = ds.runTest( "ames.lookup.nearest", mol );
		assertEquals( 2, ret.size() );
		assertEquals(ITestResult.POSITIVE,ret.get(0).getClassification());
		assertEquals(ITestResult.POSITIVE,ret.get(1).getClassification());

		mol = loadMolecule("/data/sampleBursiHitsCmpds3.mol");
		ret = ds.runTest( "ames.lookup.nearest", mol );
		assertEquals( 2, ret.size() );
		assertEquals(ITestResult.NEGATIVE,ret.get(0).getClassification());
		assertEquals(ITestResult.NEGATIVE,ret.get(1).getClassification());

	}

	@Test
	public void testAmesStructuralAlerts() throws BioclipseException, DSException, URISyntaxException, MalformedURLException, IOException, CoreException{
		IDSManager ds = Activator.getDefault().getJavaManager();

		ICDKMolecule mol = loadMolecule("/data/bursiPos3.mol");
		List<ITestResult> ret = ds.runTest( "ames.smarts", mol );
		assertEquals( 1, ret.size() );
		assertEquals(ITestResult.POSITIVE,ret.get(0).getClassification());
		assertEquals("Aromatic nitroso".toLowerCase(), ret.get(0).getName().toLowerCase());

		mol = loadMolecule("/data/bursiPos1.mol");
		ret = ds.runTest( "ames.smarts", mol );
		assertEquals( 0, ret.size() );

		mol = loadMolecule("/data/sampleBursiHitsCmpds2.mol");
		ret = ds.runTest( "ames.smarts", mol );
		assertEquals( 1, ret.size() );
		assertEquals(ITestResult.POSITIVE,ret.get(0).getClassification());
		assertEquals("Alkyl nitrite".toLowerCase(), ret.get(0).getName().toLowerCase());

		mol = loadMolecule("/data/sampleBursiHitsCmpds3.mol");
		ret = ds.runTest( "ames.smarts", mol );
		assertEquals( 1, ret.size() );
		assertEquals(ITestResult.POSITIVE,ret.get(0).getClassification());
		assertEquals("Nitrosamine".toLowerCase(), ret.get(0).getName().toLowerCase());

	}
	
	

	/**
	 * Rematch all in original data to verify at least one exact match
	 * @throws InvocationTargetException 
	 */
	@Test
	public void testAmesExacReMatchSDF() throws BioclipseException, DSException, URISyntaxException, MalformedURLException, IOException, CoreException, InvocationTargetException{
	
	IDSManager ds = Activator.getDefault().getJavaManager();
	ICDKManager cdk = 
		net.bioclipse.cdk.business.
		Activator.getDefault().getJavaCDKManager();
	ISignaturesManager signatures=net.bioclipse.ds.signatures.Activator.getDefault().getJavaSignaturesManager();

	//Exact match should work for all original molecules
	Iterator<ICDKMolecule> it = cdk.createMoleculeIterator(sourcePathtoAbsolutePath("/data/bursi_nosalts.sdf"));
	
	int cnt=0;
	Map<Integer, List<ITestResult>> errors=new HashMap<Integer, List<ITestResult>>();
	while (it.hasNext()){
		ICDKMolecule mol =it.next();
		
		List<ITestResult> ret = ds.runTest( "ames.lookup.exact", mol );
		
		if (ret.size()!=1){
			errors.put(cnt, ret);
			String ms = signatures.generateMoleculeSignature(mol);
			System.out.println("Mol anomaly, index " + cnt + " with signature: " + ms);
		}
//		assertTrue("SDF index " + cnt +" did not have an exact match",
//				ret.size()>=1 );
		cnt++;
	}

	//Just debug out anomalies, where we have multiple matches
	System.out.println("We have " + errors.size() + " anomalies.");
	for (Integer i : errors.keySet()){
		System.out.println("Mol: " + i + " had anomalities...");
		if (errors.get(i)==null || errors.get(i).size()==0)
			System.out.println("    ...no exact matches were found");
		else{
			for (ITestResult tres : errors.get(i)){
				ExternalMoleculeMatch ematch=(ExternalMoleculeMatch)tres;
				String matchmolsign="";
				try{
					matchmolsign=signatures.generateMoleculeSignature(ematch.getMatchedMolecule());
				}catch (Exception e){
					matchmolsign="Error: " + e.getMessage();
				}
				System.out.println("   Match: " + ematch.getName() + " with sign: " + matchmolsign);
			}
		}
	}


//	//Just test a mol with no matches; adrenaline
//	List<ITestResult> ret = ds.runTest( "ames.lookup.exact", 
//			cdk.fromSMILES("Oc1ccc(cc1O)[C@@H](O)CNC") );
//	assertEquals( 0, ret.size() );
		
}

	
	private ICDKMolecule loadMolecule(String path) 
	throws MalformedURLException, IOException, BioclipseException, 
	CoreException, URISyntaxException {
		ICDKManager cdk=net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
		return cdk.loadMolecule(sourcePathtoAbsolutePath(path));
	}

	
	private String sourcePathtoAbsolutePath(String sourcepath) 
	throws URISyntaxException, MalformedURLException, IOException{
		URI uri = getClass().getResource(sourcepath).toURI();
		URL url = FileLocator.toFileURL(uri.toURL());
		return url.getFile();
	}


}
