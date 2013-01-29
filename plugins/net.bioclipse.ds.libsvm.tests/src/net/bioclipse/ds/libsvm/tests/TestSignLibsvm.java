package net.bioclipse.ds.libsvm.tests;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import net.bioclipse.cdk.domain.CDKMolecule;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.libsvm.model.SignLibsvmModel;
import net.bioclipse.ds.libsvm.model.SignLibsvmUtils;
import net.bioclipse.ds.model.DSException;

import org.junit.Test;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.templates.MoleculeFactory;

public class TestSignLibsvm {
	
	String modelfile="/Users/ola/repos/bioclipse.ds.models/plugins/net.bioclipse.ds.models.ames/models/ames.model";
	String signfile="/Users/ola/repos/bioclipse.ds.models/plugins/net.bioclipse.ds.models.ames/models/ames.sign";

	/**
	 * Read a model and predict for a molecule
	 */
	@Test
	public void TestReadModel() throws IOException, DSException, BioclipseException{

		SignLibsvmModel model = SignLibsvmUtils.ModelFromFile(modelfile,signfile);
		Assert.assertEquals(23226,model.getModelSignatures().size());
		Assert.assertNotNull(model.getSvmModel());
		
		Molecule mol = MoleculeFactory.makeBenzene();
		ICDKMolecule cdkmol = new CDKMolecule(mol);

		List<String> molsigns = SignLibsvmUtils.generateSignatures(cdkmol,0,3);
		System.out.println("Query signs: " + molsigns);

		double res = model.predict(molsigns);
		System.out.println("Prediction: " + res);
		Assert.assertEquals(0.0d, res);

		//Next mol
		mol = MoleculeFactory.make4x3CondensedRings();
		cdkmol = new CDKMolecule(mol);

		molsigns = SignLibsvmUtils.generateSignatures(cdkmol,0,3);
		System.out.println("Query signs: " + molsigns);

		res = model.predict(molsigns);
		System.out.println("Prediction: " + res);
		Assert.assertEquals(1.0d, res);

	}

	

	
}
