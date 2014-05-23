package net.bioclipse.ds.libsvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.bioclipse.ds.business.DSBusinessModel;
import net.bioclipse.ds.libsvm.model.SignLibsvmModel;
import net.bioclipse.ds.libsvm.model.SignLibsvmUtils;
import net.bioclipse.ds.model.DSException;
import net.bioclipse.ds.model.IConsensusCalculator;
import net.bioclipse.ds.model.IDSTest;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PCMSignPredictionDS extends PCMSignLibSvmPrediction implements IDSTest{

    private static Logger logger = LoggerFactory.getLogger( PCMSignPredictionDS.class );
    protected PCMConfig config;

    URL signaturesPath;
    URL modelPath;
    URL protDescFile;  
    
    public PCMSignPredictionDS() {

        super();
    }

    public void initResource(Bundle bundle) {
        signaturesPath  = bundle.getEntry( config.signaturesFile() );
        modelPath       = bundle.getEntry( config.modelFile() );
        protDescFile    = bundle.getEntry( config.proteinDescriptorFile() );
        if(signaturesPath== null || modelPath == null || protDescFile == null) {
            logger.error( "One or more filepaths is null" );
            throw new RuntimeException("Faild to read file");
        }
    }
    
    public void activate(BundleContext context,Map<String, Object> properties ) {
        try{

            // config = Configurable.createConfigurable( PCMConfig.class,
            // properties );
        
        logger.info( "PCM Model Activated" );

        setId( properties.get( "id" ).toString() );
        setName( properties.get( "name" ).toString() );
        
        // setEntryPoint
        setInformative( Boolean.valueOf( properties.get( "informative" ).toString() ) );

        String pconsid = null;
        if (pconsid==null) pconsid="net.bioclipse.ds.consensus.majority";
        IConsensusCalculator conscalc = DSBusinessModel.createNewConsCalc(pconsid);
        setConsensusCalculator( conscalc );
        setVisible( true );
        
        logger.debug( "Config is " + config.toString() );
        logger.debug(properties.toString());
        } catch(Throwable ex) {
            logger.error("Could not start component",ex);
            ex.printStackTrace();
        }
    }

    protected InputStream openStream(URL resource) throws Exception {
        return resource.openStream();
    }
    
    @Override
    public List<String> getRequiredParameters() {
        return Collections.emptyList();
    }

    final private Bundle lookupBundle() {

        String bsn = getParameters().get( "Bundle-SymbolicName" );
        Bundle[] bundles = FrameworkUtil.getBundle( this.getClass() )
                        .getBundleContext().getBundles();
        for ( Bundle bundle : bundles ) {
            if ( bundle.getSymbolicName().equals( bsn ) ) {
                return bundle;
            }
        }
        return null;
    }

    @Override
    public void initialize( IProgressMonitor monitor ) throws DSException {
        logger.info("Initializing ds pcym model");
        InputStream signaturesInput = null;
        InputStream modelInput = null;
        InputStream protDescInput = null;
        try{
            // assert( config!=null);
            if(config==null) {
                try {
                    //config = Configurable.createConfigurable( PCMConfig.class, getParameters() );
                    config = createConfig( getParameters() );
                    initResource( lookupBundle() );
                } catch(Exception e){
                    logger.error("Faild to initilize configuration from parameters");
                    throw new DSException( "Faild to initialize configuration from parameters", e );
                }
                logger.error( "Config is null" );
            }
            
            signaturesInput = openStream(signaturesPath);//.openStream();
            modelInput = openStream(modelPath);//.openStream();
            protDescInput = openStream(protDescFile);//.openStream();
            
        startHeight = config.signaturesMinHeight();
        endHeight = config.signaturesMaxHeight();

        setInformative( config.informative() );

        if ( !config.isClassification() ) {

        } else {
            positiveValue = Integer.toString( config.positiveValue());
            negativeValue = Integer.toString( config.negativeValue());

            classLabels = Arrays.asList( config.classLabels());
        }
        addParameter( "isClassification", Boolean.toString( config.isClassification()) );
        logger.debug( "Endponit: "+config.endpoint() );
        addParameter( "endpoint", config.endpoint() );

        logger.info( "Initializing signature file" );
        try {
            monitor.subTask( "loading svm" );
        	List<String> modelSignatures = SignLibsvmUtils.readSignaturesFile(new BufferedReader(new InputStreamReader(signaturesInput,"UTF-8"))); 
			libsvm.svm_model svmModel = libsvm.svm.svm_load_model(new BufferedReader(new InputStreamReader(modelInput,"UTF-8")));
			signSvmModel = new SignLibsvmModel(svmModel, modelSignatures);
		} catch (IOException e) {
			logger.error( "Failed to load svm model",e );
			e.printStackTrace();
		}

        protNames = Arrays.asList( config.proteinNames() );
        proteinDescriptorStartIndex = config.proteinDescriptorStartIndex();

        protDescList = initializeResource(new BufferedReader(new InputStreamReader(protDescInput,"UTF-8")));
			
        } catch( Throwable t) {
            logger.error( "Failed to initialize test "+ getId() +" got exception "+t.getClass().getName(),t );
            t.printStackTrace();
            DSException ex = new DSException( "Failed to initialize test",t );
            ex.setStackTrace( t.getStackTrace() );
            throw ex;
        }
        finally {
            InputStream[] iss = { signaturesInput, modelInput, protDescInput};
            for(InputStream is:iss) {
                try {
                    if(is!=null) {
                        is.close();
                    }
                } catch ( IOException e ) {
                    logger.warn( "Could not close resource",e );
                }
            }
            
        }
    }
    
    private static PCMConfig createConfig( final Map<String, ? extends Object> input ) {
        return new PCMConfig() {
            
            <T> T readMap( String key,
                           Map<String, ? extends Object> map,
                           Class<T> type ) {
                Object value = map.get( key );
                if(type.isAssignableFrom( value.getClass() )) {
                    return type.cast( value );
                } else {
                    if(type.isAssignableFrom( String.class )) {
                        return type.cast( value.toString());
                    }
                    if(type.isAssignableFrom( Integer.class )) {
                        return type.cast( Integer.parseInt( value.toString() ) );
                    } else if(type.isAssignableFrom( Boolean.class )) {
                        return type.cast( Boolean.parseBoolean( value.toString() ));
                    } else if( type.isAssignableFrom( Double.class )) {
                        return type.cast(Double.parseDouble( value.toString() ));
                    } else if( type.isAssignableFrom( String[].class )) {
                        Scanner scanner = new Scanner( value.toString() ).useDelimiter( ",\\s?" );
                        List<String> result = new ArrayList<String>();
                        while(scanner.hasNext()) {
                            result.add(scanner.next());
                        }
                        return type.cast(result.toArray(new String[result.size()]));
                    } else {
                        throw new IllegalArgumentException("Could not read "+key);
                    }
                    
                }
            }
            
            @Override
            public int variables() {
                return readMap("Variables",input,Integer.class);
            }
            @Override
            public int signaturesMinHeight() {
                return readMap("signatures.min.height",input,Integer.class);
            }
            @Override
            public int signaturesMaxHeight() {
                return readMap("signatures.max.height",input,Integer.class);
            }
            @Override
            public String signaturesFile() {
                return readMap("signaturesfilet",input,String.class);
            }
            @Override
            public String[] proteinNames() {
                return readMap("proteinNames",input,String[].class);
            }
            @Override
            public int proteinDescriptorStartIndex() {
                return readMap("proteinDescriptorStartIndex",input,Integer.class);
            }
            @Override
            public String proteinDescriptorFile() {
                return readMap("proteinDescriptorFile",input,String.class);
            }
            
            @Override
            public int positiveValue() {
                return readMap("positiveValue",input,Integer.class);
            }
            
            @Override
            public int observations() {
                return readMap("Observations",input,Integer.class);
            }
            
            @Override
            public int negativeValue() {
                return readMap("negativeValue",input,Integer.class);
            }
            
            @Override
            public String name() {
                return readMap("proteinDescriptorFile",input,String.class);
            }
            
            @Override
            public String modelType() {
                return readMap("Model type",input,String.class);
            }
            
            @Override
            public double modelPerformance() {
                return readMap("Model performance",input,Double.class);
            }
            
            @Override
            public String modelFile() {
                return readMap("modelfile",input,String.class);
            }
            
            @Override
            public String modelChoice() {
                return readMap("Model choice",input,String.class);
            }
            
            @Override
            public String[] learningParameters() {
                return readMap("Learning parameters",input,String[].class);
            }
            
            @Override
            public String learningModel() {
                return readMap("Learning model",input,String.class);
            }
            
            @Override
            public boolean isClassification() {
                return readMap("isClassification",input,Boolean.class);
            }
            
            @Override
            public boolean informative() {
                return readMap("informative",input,Boolean.class);
            }
            
            @Override
            public String id() {
                return readMap("id",input,String.class);
            }
            
            @Override
            public String endpoint() {
                return readMap("endpoint",input,String.class);
            }
            
            @Override
            public String descriptors() {
                return readMap("Descriptors",input,String.class);
            }
            
            @Override
            public String[] classLabels() {
                return readMap("classLabels",input,String[].class);
            }
        };
    }
}
