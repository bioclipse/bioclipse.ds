package net.bioclipse.ds.model;


public class ErrorResult extends SimpleResult{

    private String errorMessage;
    
    public ErrorResult(String errorMessage) {
        super();
        this.errorMessage = errorMessage;
    }


    public ErrorResult(String name, String message) {
        this(message);
        setName( name );
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
    @Override
    public String toString() {
        return "ErrorResult: "+ getName() + " - message=" + getErrorMessage();
    }

}
