/* *****************************************************************************
 * Copyright (c) 2009-2010 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/package net.bioclipse.ds.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.jchempaint.view.ChoiceGenerator;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.result.AtomResultMatch;
import net.bioclipse.ds.model.result.BlueRedColorScaleGenerator;
import net.bioclipse.ds.model.result.ExternalMoleculeMatch;
import net.bioclipse.ds.model.result.PosNegIncColorGenerator;
import net.bioclipse.ds.model.result.SubStructureMatch;

import org.apache.log4j.Logger;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator.AtomColor;
import org.openscience.cdk.renderer.generators.HighlightAtomGenerator.HighlightAtomDistance;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;

/**
 * Helper classes to generate Images of structures with DS-highlighting
 * @author ola
 *
 */
public class ImageHelper {

    private static final Logger logger = Logger.getLogger(ImageHelper.class);

    public static Image createImage( net.bioclipse.core.domain.IMolecule bcmol,
                                      ITestResult match ) 
                                      throws BioclipseException {

        //Default values
        int WIDTH = 150;
        int HEIGHT = 150;

        return createImage(bcmol, match, WIDTH, HEIGHT, 0.4);
        
    }
    
    @SuppressWarnings("unchecked")
	public static Image createImage( net.bioclipse.core.domain.IMolecule bcmol,
                        ITestResult match, int WIDTH, int HEIGHT, double zoom)
                                                     throws BioclipseException {

        if (bcmol==null)
            return null;

        ICDKManager cdk = net.bioclipse.cdk.business.Activator
        .getDefault().getJavaCDKManager();

        ICDKMolecule cdkmol=null;

        //Render external match
        if (match instanceof ExternalMoleculeMatch) {
			ExternalMoleculeMatch extmatch = (ExternalMoleculeMatch) match;
			cdkmol=extmatch.getMatchedMolecule();
		}else{
	        //Or the query molecule
			cdkmol=cdk.asCDKMolecule( bcmol );
		}
        

        // the draw area and the image should be the same size
        Rectangle drawArea = new Rectangle(WIDTH, HEIGHT);
        Image image = new BufferedImage(
                WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        
        //Generate 2D
        IMolecule mol = new Molecule(cdkmol.getAtomContainer());
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.setMolecule(mol, true);
        try {
            sdg.generateCoordinates();
        } catch (Exception e) { }
        mol = sdg.getMolecule();
        
        // generators make the image elements
        List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();

        //Add the standard generators
        generators.add(new BasicSceneGenerator());
        
        //Add all generators, we turn them on/off by a parameter now
        BlueRedColorScaleGenerator generator=new BlueRedColorScaleGenerator();
        PosNegIncColorGenerator gen2=new PosNegIncColorGenerator();
        generators.add(generator);
        generators.add( gen2 );
        
        generators.add(new BasicBondGenerator());
        BasicAtomGenerator agen = new BasicAtomGenerator();
        generators.add(agen);

        // the renderer needs to have a toolkit-specific font manager 
        AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());
        
        renderer.setup(mol, drawArea);
        RendererModel model = renderer.getRenderer2DModel();

//        model.set(AtomColor.class, Color.BLUE);
//        model.set(BasicAtomGenerator.CompactAtom.class, true);

        enableSelectedExternalGenerators(match, model);

        //TODO: belows does not seem to work properly
//        renderer.setZoomToFit( WIDTH, HEIGHT, WIDTH, HEIGHT );
//        renderer.setZoom( zoom );
        
        // paint the background
        Graphics2D g2 = (Graphics2D)image.getGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        // the paint method also needs a toolkit-specific renderer
        Double bounds = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
        renderer.paint(mol, new AWTDrawVisitor(g2), bounds, true);
//        renderer.paint(mol, new AWTDrawVisitor(g2));
        
        return image;

    }

	private static void enableSelectedExternalGenerators(ITestResult match,
			RendererModel model) {
		
		//Get all external generators and filter the ones registered in the model
    	List<IGeneratorParameter<?>> parameters = model.getRenderingParameters();
    	List<IGenerator<IAtomContainer>> generators = ChoiceGenerator.getGeneratorsFromExtension();
    	
    	for(IGenerator<IAtomContainer> gen:generators) {
    		List<IGeneratorParameter<?>> params = gen.getParameters();
    		parameters.removeAll(params);
    	}
		
    	for(IGeneratorParameter<?> param : parameters) {
    			if (param.getDefault() instanceof Boolean) {
    				IGeneratorParameter<Boolean> bp= (IGeneratorParameter<Boolean>)param;
    				model.set(bp.getClass(), false);
    			}
    	}				
    	
    	//Now, turn on the generator for this match
        //IF match, turn on the supplied generator
        if ( match != null ) {

            Class<? extends IGeneratorParameter<Boolean>> visibilityParam = match.getGeneratorVisibility();
            Class<? extends IGeneratorParameter<Map<Integer, Number>>> atomMapParam = match.getGeneratorAtomMap();
            
            if (visibilityParam==null){
            	logger.debug("The selected TestResult does not provide a " +
            			"generatorVisibility. No generator turned on.");
//            	return null;
            }
            else{


            //And turn only the selected on
            model.set(visibilityParam, true);
			logger.debug("Turned on visibility for " +
					"Generator: " + match.getGeneratorVisibility());

			if (atomMapParam!=null){
				if (match instanceof AtomResultMatch) {
					AtomResultMatch atomResMatch = (AtomResultMatch) match;
                    model.set(atomMapParam, atomResMatch.getResultMap());
					logger.debug("  ...and AtomMapGeneratorParameter is used with content.");
				}else{
					logger.debug("  ...however, an AtomMapGeneratorParameter is available but TestResult is not PosNegIncMatch.");
				}
				
			}
			else{
				logger.debug("  ...however, no AtomMapGeneratorParameter is available.");
            }
            }
            
        }
	}
}
