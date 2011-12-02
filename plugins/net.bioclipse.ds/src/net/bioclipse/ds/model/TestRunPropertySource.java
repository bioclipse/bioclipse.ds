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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.bioclipse.core.domain.props.BasicPropertySource;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;


public class TestRunPropertySource extends BasicPropertySource 
                                                implements IPropertySource2{

    //Model info
    protected static final String MODEL_NAME = "Model name";
    protected static final String MODEL_TYPE = "Model type";
    protected static final String MODEL_CHOICE = "Model choice";
    protected static final String MODEL_VALIDATION = "Model validation";
    protected static final String MODEL_PERFORMANCE = "Model performance";
    protected static final String LEARNING_MODEL = "Learning model";
    protected static final String LEARNING_PARAMETERS = "Learning parameters";
    protected static final String SIMILARITY_THRESHOLD = "Similarity threshold";
    protected static final String SIMILARITY_METRIC = "Similarity metric";

    //Dataset info
    protected static final String DATASET_NAME = "Dataset name";
    protected static final String DATASET_URL = "URL";
    protected static final String DATASET_OBSERVATIONS = "Observations";
    protected static final String DATASET_VARIABLES= "Variables";
    protected static final String DATASET_DESCRIPTORS = "Descriptors";
    protected static final String DATASET_SIZE = "Size";

	//Results
    protected static final String PREDICTION_STATUS = "Status";
    protected static final String PREDICTION_TIME = "Prediction time";
    protected static final String PREDICTION_CONSENSUS = "Consensus";


	
    private Object ModelPropertiesTable[][] =
    {
            { MODEL_NAME, new TextPropertyDescriptor(MODEL_NAME,MODEL_NAME)},
            { MODEL_TYPE, new TextPropertyDescriptor(MODEL_TYPE,MODEL_TYPE)},
            { MODEL_CHOICE, new TextPropertyDescriptor(MODEL_CHOICE,MODEL_CHOICE)},
            { MODEL_VALIDATION, new TextPropertyDescriptor(MODEL_VALIDATION,MODEL_VALIDATION)},
            { MODEL_PERFORMANCE, new TextPropertyDescriptor(MODEL_PERFORMANCE,MODEL_PERFORMANCE)},
            { LEARNING_MODEL, new TextPropertyDescriptor(LEARNING_MODEL,LEARNING_MODEL)},
            { LEARNING_PARAMETERS, new TextPropertyDescriptor(LEARNING_PARAMETERS,LEARNING_PARAMETERS)}
    };
    
    private Object DatasetPropertiesTable[][] =
    {
            { DATASET_NAME, new TextPropertyDescriptor(DATASET_NAME,DATASET_NAME)},
            { DATASET_URL, new TextPropertyDescriptor(DATASET_URL,DATASET_URL)},
            { DATASET_OBSERVATIONS, new TextPropertyDescriptor(DATASET_OBSERVATIONS,DATASET_OBSERVATIONS)},
            { DATASET_VARIABLES, new TextPropertyDescriptor(DATASET_VARIABLES,DATASET_VARIABLES)},
            { DATASET_DESCRIPTORS, new TextPropertyDescriptor(DATASET_DESCRIPTORS,DATASET_DESCRIPTORS)}
    };

    private Object ResultsPropertiesTable[][] =
    {
            { PREDICTION_STATUS, new TextPropertyDescriptor(PREDICTION_STATUS,"Status")},
            { PREDICTION_CONSENSUS, new TextPropertyDescriptor(PREDICTION_CONSENSUS,PREDICTION_CONSENSUS)},
            { PREDICTION_TIME, new TextPropertyDescriptor(PREDICTION_TIME,PREDICTION_TIME)},
    };

    public TestRunPropertySource(TestRun model) {
        super( model );

        // clean the table
        setProperties(new ArrayList<IPropertyDescriptor>());
        setValueMap(new HashMap<String, String>());

        // Model properties
    	PropertyDescriptor descriptor = new TextPropertyDescriptor(MODEL_NAME,MODEL_NAME);
        descriptor.setCategory("Model");
        getProperties().add((IPropertyDescriptor)descriptor);
        addToValueMap(MODEL_NAME, model.getTest().getName());

        addParameterProperty(model, MODEL_CHOICE,"Model");
        addParameterProperty(model, MODEL_TYPE,"Model");
        addParameterProperty(model, MODEL_VALIDATION,"Model");
        addParameterProperty(model, MODEL_PERFORMANCE,"Model");
        addParameterProperty(model, LEARNING_MODEL,"Model");
        addParameterProperty(model, LEARNING_PARAMETERS,"Model");
        addParameterProperty(model, SIMILARITY_METRIC,"Model");
        addParameterProperty(model, SIMILARITY_THRESHOLD,"Model");
        
        // Dataset properties
        addParameterProperty(model, DATASET_NAME, "Dataset");
        addParameterProperty(model, DATASET_URL, "Dataset");
        addParameterProperty(model, DATASET_OBSERVATIONS, "Dataset");
        addParameterProperty(model, DATASET_VARIABLES, "Dataset");
        addParameterProperty(model, DATASET_DESCRIPTORS, "Dataset");
        addParameterProperty(model, DATASET_SIZE, "Dataset");


        // Result properties
        for (int i=0;i<ResultsPropertiesTable.length;i++) {        
            descriptor = (PropertyDescriptor)ResultsPropertiesTable[i][1];
            descriptor.setCategory("Result");
            getProperties().add((IPropertyDescriptor)descriptor);
        }   
        
        //Result values
        if (model.getStatus()==TestRun.NOT_STARTED)
            addToValueMap(PREDICTION_STATUS,"NOT STARTED");
        else if (model.getStatus()==TestRun.RUNNING)
            addToValueMap(PREDICTION_STATUS,"RUNNING");
        else if (model.getStatus()==TestRun.FINISHED)
            addToValueMap(PREDICTION_STATUS,"FINISHED");
        else if (model.getStatus()==TestRun.ERROR)
            addToValueMap(PREDICTION_STATUS,"ERROR");
        else if (model.getStatus()==ITestResult.INFORMATIVE)
            addToValueMap(PREDICTION_STATUS,"INFORMATIVE");
        else if (model.getStatus()==TestRun.EXCLUDED)
            addToValueMap(PREDICTION_STATUS,"EXCLUDED");
        else
            addToValueMap(PREDICTION_STATUS,"N/A");

            addToValueMap(PREDICTION_CONSENSUS,model.getConsensusString());
            addToValueMap(PREDICTION_TIME,"" + model.getTest().getExecutionTimeMilliSeconds() + " ms");
    }


    /**
     * Look up a parameter in test and add to values if exists.
     * 
     * @param model
     * @param param
     * @param category
     * 
     */
	private void addParameterProperty(TestRun model,
			String param, String category) {

		Map<String, String> modelParams = model.getTest().getParameters();

		if (modelParams.containsKey(param)){
        	PropertyDescriptor descriptor = new TextPropertyDescriptor(param,param);
            descriptor.setCategory(category);
            getProperties().add((IPropertyDescriptor)descriptor);
            addToValueMap(param, modelParams.get(param));
        }
	}    
    
    
    /**
     * Validate strings are non-empty or else add "N/A"
     * @param keyString
     * @param valueString
     */
    private void addToValueMap(String keyString, String valueString) {
      if (keyString==null || keyString=="") return;
      
      if (valueString==null || valueString=="")
        getValueMap().put(keyString,"N/A");
      else
        getValueMap().put(keyString,valueString);
    }

}
