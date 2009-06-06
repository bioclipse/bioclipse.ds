/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.ds.business;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.core.domain.IMolecule;
import net.bioclipse.ds.model.IDSTest;
import net.bioclipse.ds.model.impl.DSException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;


public class TestHelper {

    private static final Logger logger = Logger.getLogger(TestHelper.class);

    public static List<IDSTest> readTestsFromEP(){

        List<IDSTest> retlist = new ArrayList<IDSTest>();

        IExtensionRegistry registry = Platform.getExtensionRegistry();

        if ( registry == null ) return retlist;
        // it likely means that the Eclipse workbench has not
        // started, for example when running tests

        IExtensionPoint serviceObjectExtensionPoint = registry
        .getExtensionPoint("net.bioclipse.ds.test");

        IExtension[] serviceObjectExtensions
        = serviceObjectExtensionPoint.getExtensions();

        for(IExtension extension : serviceObjectExtensions) {
            for( IConfigurationElement element
                    : extension.getConfigurationElements() ) {

                if (element.getName().equals("test")){

                    String pname=element.getAttribute("name");

                    Object obj;
                    try {
                        obj = element.createExecutableExtension("class");
                        if (obj instanceof IDSTest){

                            IDSTest test=(IDSTest)obj;

                            test.setName(pname);
                            String pid=element.getAttribute("id");
                            test.setId(pid);
                            String picon=element.getAttribute("icon");
                            test.setIcon(picon);
                            
                            String pluginID=element.getNamespaceIdentifier();
                            test.setPluginID( pluginID );
                            
                            for( IConfigurationElement subelement
                                    : element.getChildren() ) {
                                if ("resource".equals( subelement.getName() )){
                                    String name=subelement.getAttribute( "name" );
                                    String path=subelement.getAttribute( "path" );
                                    test.addParameter(name,path);
                                }
                                else if ("parameter".equals( subelement.getName() )){
                                    String name=subelement.getAttribute( "name" );
                                    String path=subelement.getAttribute( "value" );
                                    test.addParameter(name,path);
                                }
                            }
                            retlist.add( test );

                            logger.debug("Added Decision support Test from EP: " + element.
                                         getAttribute("name") + " to " + pname);
                        }else{
                            logger.error("WarningTest class " + pname + " must implement IWarningTest ");
                        }
                    } catch ( CoreException e ) {
                        logger.error("Error creating class for :" + pname +": " + e.getLocalizedMessage() );
                    }

                }
            }
        }

        return retlist;
    }

}
