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

import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.managers.business.IBioclipseManager;

/**
 * Signatures manager for generating signatures according to Faulon et al. 1934.
 * @author ola
 *
 */
@PublishedClass(
    value="Generate Signatures according to Faulon et al. 1934",
    doi="TBC"
)
public interface ISignaturesManager extends IBioclipseManager {


    /**
     * Generate Signatures from SDFile.
     * @param path path to SDFile
     * @return map of molecule to Signatures (list of Strings)
     */
    @Recorded
    @PublishedMethod( 
        params = "String path",
        methodSummary = "Generate Signatures from SDFile")
    public Map<IMolecule, List<String>> generate(String path);

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
    public List<String> generate(IMolecule molecule)  throws BioclipseException;

    /**
     * Generate Signatures from a molecule.
     * @param mol IMolecule
     * @return list of Signatures serialized as Strings
     * @exception BioclipseException
     */
    @Recorded
    @PublishedMethod( 
        params = "IMolecule molecule, int height",
        methodSummary ="Generate Signatures with a given height for a molecule")
    public List<String> generate(IMolecule molecule, int height) 
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
    public Map<IMolecule, List<String>> generate(List<IMolecule> molecules);

}
