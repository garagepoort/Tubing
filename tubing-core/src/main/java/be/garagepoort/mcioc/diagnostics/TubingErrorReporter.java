package be.garagepoort.mcioc.diagnostics;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TubingErrorReporter {

    private TubingErrorReporter() {
    }

    public static void report(Logger logger, Throwable throwable) {
        TubingDiagnosticException diagnosticException = findDiagnosticException(throwable);
        if (diagnosticException == null) {
            logger.log(Level.SEVERE, "[Tubing] Unexpected startup error", throwable);
            return;
        }

        logger.severe("");
        logger.severe("[Tubing] " + diagnosticException.getTitle());
        for (String detail : diagnosticException.getDetails()) {
            logger.severe("[Tubing] " + detail);
        }
        if (!diagnosticException.getHints().isEmpty()) {
            logger.severe("[Tubing] Fix:");
            for (String hint : diagnosticException.getHints()) {
                logger.severe("[Tubing] - " + hint);
            }
        }
        logger.severe("");

        if (diagnosticException.isStacktraceUseful()) {
            logger.log(Level.SEVERE, "[Tubing] Technical details", diagnosticException);
        }
    }

    public static TubingDiagnosticException findDiagnosticException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof TubingDiagnosticException) {
                return (TubingDiagnosticException) current;
            }
            current = current.getCause();
        }
        return null;
    }
}
