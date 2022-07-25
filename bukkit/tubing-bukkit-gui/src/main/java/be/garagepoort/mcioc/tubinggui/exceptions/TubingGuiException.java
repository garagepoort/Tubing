package be.garagepoort.mcioc.tubinggui.exceptions;

public class TubingGuiException extends RuntimeException{
    public TubingGuiException(String message) {
        super(message);
    }

    public TubingGuiException(String message, Exception e) {
        super(message, e);
    }

    public TubingGuiException(Exception e) {
        super(e);
    }
}
