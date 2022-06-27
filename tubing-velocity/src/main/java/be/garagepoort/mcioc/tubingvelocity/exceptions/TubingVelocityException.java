package be.garagepoort.mcioc.tubingvelocity.exceptions;

public class TubingVelocityException extends RuntimeException{
    public TubingVelocityException(String message) {
        super(message);
    }

    public TubingVelocityException(String message, Exception e) {
        super(message, e);
    }

    public TubingVelocityException(Exception e) {
        super(e);
    }
}
