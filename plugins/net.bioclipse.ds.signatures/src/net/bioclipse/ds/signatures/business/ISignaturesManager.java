/*******************************************************************************
 * Copyright (c) 2010  Ola Spjuth <ola@bioclipse.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.ds.signatures.business;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.DenseDataset;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.core.domain.SparseDataset;
import net.bioclipse.ds.signatures.prop.calc.AtomSignatures;
import net.bioclipse.managers.business.IBioclipseManager;

/**
 * Signatures manager for generating signatures according to Faulon et al. 1934.
 * @author ola
 *
 */
@PublishedClass(
    value="Generate Signatures according to Faulon et al. 2003,2003, and 2004.",
    doi={"10.1021/ci020346o","10.1021/ci020345w","10.1021/ci0341823"}
)
public interface ISignaturesManager extends IBioclipseManager {


    /**
     * Generate Signatures from SDFile.
     * @param path path to SDFile
     * @return List of Signatures objects
     */
    @Recorded
    @PublishedMethod( 
        params = "String path",
        methodSummary = "Generate Signatures from SDFile")
    public Map<IMolecule, AtomSignatures> generate(String path) 
    throws BioclipseException, CoreException, IOException;
    public Map<IMolecule, AtomSignatures> generate(IFile file) 
    throws BioclipseException, CoreException, IOException;
    public Map<IMolecule, AtomSignatures> generate(IFile file, IProgressMonitor monitor) 
    throws BioclipseException, CoreException, IOException;

    /**
     * Generate Signatures from SDFile.
     * @param path path to SDFile
     * @param height Signatures height
     * @return List of Signatures objects
     */
    @Recorded
    @PublishedMethod( 
        params = "String path, int height",
        methodSummary = "Generate Signatures from SDFile with a given height")
    public Map<IMolecule, AtomSignatures> generate(String path, int height) 
    throws BioclipseException, CoreException, IOException;
    public Map<IMolecule, AtomSignatures> generate(IFile file, int height) 
    throws BioclipseException, CoreException, IOException;
    public Map<IMolecule, AtomSignatures> generate(IFile file, int height, IProgressMonitor monitor) 
    throws BioclipseException, CoreException, IOException;

    
    /**
     * Generate Signatures from a molecule.
     * @param mol IMolecule
     * @return list of Signatures serialized as Strings
     * @exception BioclipseException
     */
    @Recorded
    @PublishedMethod( 
        params = "IMolecule molecule",
        methodSummary = "Generate Signatures for a molecule")
    public AtomSignatures generate(IMolecule molecule)  
    throws BioclipseException;

    /**
     * Generate Signatures from a molecule.
     * @param mol IMolecule
     * @return list of Signatures
     * @exception BioclipseException
     */
    @Recorded
    @PublishedMethod( 
        params = "IMolecule molecule, int height",
        methodSummary ="Generate Signatures with a given height for a molecule")
    public AtomSignatures generate(IMolecule molecule, int height) 
    throws BioclipseException;

    /**
     * Generate Signatures from a list of viles
     * @param mols List of IMoleculs
     * @return map of molecule to Signatures (list of Strings)
     */
    @Recorded
    @PublishedMethod( 
        params = "List<IMolecule> molecules",
        methodSummary = "Generate Signatures from a list of molecules")
    public Map<IMolecule, AtomSignatures> generate(List<? extends IMolecule> mols);
    public Map<IMolecule, AtomSignatures> generate(List<? extends IMolecule> mols, IProgressMonitor monitor);

    /**
     * Generate Signatures from a list of viles
     * @param mols List of IMoleculs
     * @return map of molecule to Signatures (list of Strings)
     */
    @Recorded
    @PublishedMethod( 
        params = "List<IMolecule> molecules, int height",
        methodSummary = "Generate Signatures from a list of molecules with " +
        		"a given height")
    public Map<IMolecule, AtomSignatures> generate(
    		List<? extends IMolecule> molecules,int height);
    public Map<IMolecule, AtomSignatures> generate(
    		List<? extends IMolecule> molecules,int height, IProgressMonitor monitor);

    /**
     * Generate MoleculeSignature from a molecule
     * @param mol 
     * @return String
     */
    @Recorded
    @PublishedMethod( 
        params = "IMolecule mol",
        methodSummary = "Generate a MolecularSignature from a molecule.")
    public String generateMoleculeSignature( IMolecule mol );

    
//    /**
//     * Generate Chiral Signatures from a molecule.
//     * @param mol IMolecule
//     * @return list of Signatures
//     * @exception BioclipseException
//     */
//    @Recorded
//    @PublishedMethod( 
//        params = "IMolecule molecule, int height",
//        methodSummary ="Generate Chiral Signatures with a given height for a molecule")
//    public AtomSignatures generateChiral(IMolecule molecule, int height) 
//    throws BioclipseException;

    @Recorded
    @PublishedMethod( 
        params = "List<? extends IMolecule> mols, int height",
        methodSummary = "Generate a dataset of signature counts for a list of molecules with height h.")
	public DenseDataset generateDataset(List<? extends IMolecule> mols, int height);
	public DenseDataset generateDataset(List<? extends IMolecule> mols, int height, IProgressMonitor monitor);

    @Recorded
    @PublishedMethod( 
        params = "List<? extends IMolecule> mols, int height, String nameProperty, String responseProperty",
        methodSummary = "Generate a dataset of signature counts for a list of molecules. " +
        		"Set name of molecules and append response values to last column.")
	public DenseDataset generateDataset(List<? extends IMolecule> mols, int height, String nameProperty, 
			String responseProperty);
	public DenseDataset generateDataset(List<? extends IMolecule> mols, int height, String nameProperty, 
			String responseProperty, IProgressMonitor monitor);

    @Recorded
    @PublishedMethod( 
        params = "List<? extends IMolecule> mols, int height, ",
        methodSummary = "Generate a sparse dataset of signature counts for a list of molecules with height h. ")
	public SparseDataset generateSparseDataset(List<? extends IMolecule> mols, int height);
	public SparseDataset generateSparseDataset(List<? extends IMolecule> mols, int height, IProgressMonitor monitor);

    @Recorded
    @PublishedMethod( 
        params = "List<? extends IMolecule> mols, int height, String nameProperty, String responseProperty",
        methodSummary = "Generate a sparse dataset of signature counts for a list of molecules. " +
        		"Set name of molecules and append response values to last column.")
	public SparseDataset generateSparseDataset(List<? extends IMolecule> mols, int height, String nameProperty, 
			String responseProperty);
	public SparseDataset generateSparseDataset(List<? extends IMolecule> mols, int height, String nameProperty, 
			String responseProperty, IProgressMonitor monitor);

	
}
