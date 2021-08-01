package be.garagepoort.mcioc.gui;

public class TubingGuiException extends RuntimeException{
    public TubingGuiException(String message) {
        super(message);
    }

    public TubingGuiException(String message, Exception e) {
        super(message, e);
    }
}
