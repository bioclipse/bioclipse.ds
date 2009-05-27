package net.bioclipse.ds.model;


public class ErrorResult extends SimpleResult{

    private String errorMessage;
    
    @Override
    public String getName() {
        return "ERROR";
    }

    
    public ErrorResult(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }


    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String toString() {
        return "ErrorResult: "+ getName() + " - message=" + getErrorMessage();
    }

}
