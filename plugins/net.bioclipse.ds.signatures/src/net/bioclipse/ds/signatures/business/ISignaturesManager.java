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

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.signatures.prop.calc.Signatures;
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
    public List<Signatures> generate(String path);
    public List<Signatures> generate(IFile path);

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
    public List<Signatures> generate(String path, int height);
    public List<Signatures> generate(IFile path, int height);
    
    
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
    public Signatures generate(IMolecule molecule)  
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
    public Signatures generate(IMolecule molecule, int height) 
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
    public Map<IMolecule, Signatures> generate(
                                                     List<IMolecule> molecules);

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
    public Map<IMolecule, Signatures> generate(List<IMolecule> molecules
                                                       ,int height);

    /**
     * Generate MoleculeSignature from a molecule
     * @param mol 
     * @return String
     */
    @Recorded
    @PublishedMethod( 
        params = "ICDKMolecule cdkmol",
        methodSummary = "Generate a MolecularSignature from a molecule.")
    public String generateMoleculeSignature( ICDKMolecule cdkmol );

}
