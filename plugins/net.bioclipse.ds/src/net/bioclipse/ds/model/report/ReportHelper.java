package net.bioclipse.ds.model.report;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.cdk.domain.ISubStructure;
import net.bioclipse.cdk.jchempaint.generators.SubStructureGenerator;
import net.bioclipse.cdk.jchempaint.wizards.NewMoleculeWizard;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.ds.Activator;
import net.bioclipse.ds.model.ITestResult;
import net.bioclipse.ds.model.SubStructureMatch;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.renderer.Renderer;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.osgi.framework.Bundle;


public class ReportHelper {

    private static byte[] questImg;
    private static byte[] warnImg;
    private static byte[] crossImg;
    private static byte[] checkImg;
    private static byte[] wheelImg;

    
    public static byte[] createImage( net.bioclipse.core.domain.IMolecule bcmol,
                                      ISubStructure match ) 
                                      throws BioclipseException {

        //Default values
        int WIDTH = 150;
        int HEIGHT = 150;

        return createImage(bcmol, match, WIDTH, HEIGHT, 0.4);
        
    }
    
    public static byte[] createImage( net.bioclipse.core.domain.IMolecule bcmol,
                        ISubStructure match, int WIDTH, int HEIGHT, double zoom)
                                                     throws BioclipseException {

        if (bcmol==null)
            return null;
        
        ICDKManager cdk = net.bioclipse.cdk.business.Activator
                                              .getDefault().getJavaCDKManager();
        ICDKMolecule cdkmol= cdk.asCDKMolecule( bcmol );

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
        List<IGenerator> generators = new ArrayList<IGenerator>();

        //Add the standard generators
        generators.add(new BasicBondGenerator());

        SubStructureMatch newMatch=null;
        
        //If we have a match:
        //We need to generate a new ISubstructureMatch from pre-clone and SDG
        if ( match instanceof SubStructureMatch ) {
            SubStructureMatch submatch = (SubStructureMatch) match;

            newMatch=new SubStructureMatch(submatch.getName(),
                                                    submatch.getClassification());
            newMatch.setTestRun( ((SubStructureMatch) match).getTestRun() );
            IAtomContainer newac = NoNotificationChemObjectBuilder.getInstance().newAtomContainer();
            for (IAtom atom : match.getAtomContainer().atoms()){
                int atomno=cdkmol.getAtomContainer().getAtomNumber( atom );
                IAtom newAtom=mol.getAtom( atomno );
//                match.getAtomContainer().
                newac.addAtom( newAtom );
            }
            
            newMatch.setAtomContainer( newac );
        }
        
        if (newMatch!=null){
            SubStructureGenerator generator=new SubStructureGenerator();
            generator.add( newMatch );
            generators.add(generator);
        }
        
        generators.add(new BasicAtomGenerator());
        
        // the renderer needs to have a toolkit-specific font manager 
        Renderer renderer = new Renderer(generators, new AWTFontManager());
        
        // the call to 'setup' only needs to be done on the first paint
        renderer.setup(mol, drawArea);
        renderer.getRenderer2DModel().setHighlightDistance( 18 );

        //TODO: belows does not seem to work properly
//        renderer.setZoomToFit( WIDTH, HEIGHT, WIDTH, HEIGHT );
        renderer.setZoom( zoom );
        
        // paint the background
        Graphics2D g2 = (Graphics2D)image.getGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        // the paint method also needs a toolkit-specific renderer
        renderer.paintMolecule(mol, new AWTDrawVisitor(g2));

        ByteArrayOutputStream bo=new ByteArrayOutputStream();
        
        try {
            ImageIO.write((RenderedImage)image, "PNG", bo);
//            ImageIO.write((RenderedImage)image, "PNG", new File("/Users/ola/tmp/molOLA.png"));
            bo.flush();
            bo.close();
            byte[] imagedata=bo.toByteArray();
            return imagedata;
        } catch ( IOException e1 ) {
            e1.printStackTrace();
        }
        
        return null;

    }

    public static String statusToString(int status){
        
        if (status==ITestResult.POSITIVE)
            return "POSITIVE";
        else if (status==ITestResult.NEGATIVE)
            return"NEGATIVE";
        else if (status==ITestResult.INCONCLUSIVE)
            return"INCONCLUSIVE";
        else if (status==ITestResult.INFORMATIVE)
            return"INFORMATIVE";
        else if (status==ITestResult.ERROR)
            return"ERROR";
        else
            return"N/A";
        
    }

    public static int stringToStatus(String value){
        
//        if (status==ITestResult.POSITIVE)
//            return "POSITIVE";
//        else if (status==ITestResult.NEGATIVE)
//            return"NEGATIVE";
//        else if (status==ITestResult.INCONCLUSIVE)
//            return"INCONCLUSIVE";
//        else if (status==ITestResult.INFORMATIVE)
//            return"INFORMATIVE";
//        else if (status==ITestResult.ERROR)
//            return"ERROR";
//        else
//            return"N/A";

        //FIXME: IMPLEMENT THIS
            return ITestResult.ERROR;
        
    }

    public static byte[] statusToImageData( int consensus ) {

        if (checkImg==null)
            readImages();
        
        if (consensus==ITestResult.POSITIVE)
            return crossImg;
        else if (consensus==ITestResult.NEGATIVE)
            return checkImg;
        else if (consensus==ITestResult.INCONCLUSIVE)
            return questImg;

        return warnImg;
    }
    
    private static void readImages(){
        

        //Initialize and cache consensus images
        try {
            wheelImg= readImage( "/icons48/wheel.png" );
            questImg= readImage(  "/icons48/question.png" );
            warnImg= readImage( "/icons48/warn.png" );
            crossImg= readImage( "/icons48/cross.png" );
            checkImg= readImage( "/icons48/check.png" );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    private static byte[] readImage(String relativePath) throws IOException{
        
        //Get absolute path
        Bundle bundle = org.eclipse.core.runtime.Platform.getBundle(
                                                           Activator.PLUGIN_ID); 
        URL url = FileLocator.find(bundle, 
                                   new Path(relativePath), null);
        String absPath = FileLocator.toFileURL(url).getPath();

        //Read the absolute path as file
        File myFile=new File(absPath);
        FileInputStream is;
            is = new FileInputStream(myFile);
        long lengthi=myFile.length();
        byte[] imagedata=new byte[(int)lengthi];
        is.read(imagedata);
        is.close();
        
        return imagedata;

    }
    
}
