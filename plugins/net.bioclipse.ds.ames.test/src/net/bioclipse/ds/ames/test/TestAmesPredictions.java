package net.bioclipse.ds.ames.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.business.IDSManager;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.ITestResult;

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


}
