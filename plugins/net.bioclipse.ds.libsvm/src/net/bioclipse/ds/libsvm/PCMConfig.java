package net.bioclipse.ds.libsvm;

public interface PCMConfig {
    
    String id();
    String name();
    String endpoint();
    boolean informative();

    boolean isClassification();
    int positiveValue();
    int negativeValue();
    String[] classLabels();
    
    int signaturesMinHeight();
    int signaturesMaxHeight();
    
    String modelFile();             // File
    String signaturesFile();        // File
    
    String modelType();
    String learningModel();
    
    String proteinDescriptorFile();  // File
    String[] proteinNames();
    int proteinDescriptorStartIndex();
    
    double modelPerformance();
    String modelChoice();

    String[] learningParameters();
    String descriptors();
    int observations();
    int variables();
    
}
