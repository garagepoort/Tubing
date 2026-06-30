package be.garagepoort.mcioc.diagnostics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TubingDiagnosticException extends RuntimeException {

    private final String title;
    private final List<String> details;
    private final List<String> hints;
    private final boolean stacktraceUseful;

    public TubingDiagnosticException(String title, List<String> details, List<String> hints) {
        this(title, details, hints, null, false);
    }

    public TubingDiagnosticException(String title, List<String> details, List<String> hints, Throwable cause) {
        this(title, details, hints, cause, false);
    }

    public TubingDiagnosticException(String title, List<String> details, List<String> hints, Throwable cause, boolean stacktraceUseful) {
        super(title, cause);
        this.title = title;
        this.details = details == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(details));
        this.hints = hints == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(hints));
        this.stacktraceUseful = stacktraceUseful;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getDetails() {
        return details;
    }

    public List<String> getHints() {
        return hints;
    }

    public boolean isStacktraceUseful() {
        return stacktraceUseful;
    }
}
