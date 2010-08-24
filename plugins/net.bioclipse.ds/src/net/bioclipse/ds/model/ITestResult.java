/* *****************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.model;

import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;

import net.bioclipse.cdk.domain.ISubStructure;


/**
 * A base interface for all test results. Extends ISubStructure since even if 
 * no substructure is returned it should clear previous substructure selections.
 * @author ola
 *
 */
public interface ITestResult extends IAdaptable{

    //The possible result statuses
    public static final int POSITIVE=0x1;
    public static final int NEGATIVE=0x2;
    public static final int INCONCLUSIVE=0x3;
    public static final int INFORMATIVE=0x4;
    public static final int ERROR=0x5;

    
    /**
     * The name of this match to be displayed in e.g. UI
     * @return
     */
    public String getName();

    public void setName( String name );

    /**
     * The parent TestRun. Required in e.g. TreeViewer.
     * @return
     */
    public TestRun getTestRun();
    public void setTestRun( TestRun testRun );
    
    public int getClassification();
    public void setClassification(int resultStatus);

    public Image getIcon();

    public String getDetailedMessage();
    public void setDetailedMessage( String detailedMessage );

    String getSuffix();
    
//    public String getResultProperty();
//    public void setResultProperty( String propertyKey );

	public Class<? extends IGeneratorParameter<Boolean>> getGeneratorVisibility();
	public Class<? extends IGeneratorParameter<Map<Integer, Number>>> getGeneratorAtomMap();

}
