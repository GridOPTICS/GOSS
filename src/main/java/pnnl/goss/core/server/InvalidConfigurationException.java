package pnnl.goss.core.server;

public class InvalidConfigurationException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidConfigurationException(){
        super();
    }

    public InvalidConfigurationException(String message){
        super(message);
    }
}
