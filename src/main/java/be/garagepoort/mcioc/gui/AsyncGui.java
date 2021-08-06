package be.garagepoort.mcioc.gui;

public class AsyncGui<T> {

    private AsyncGuiExecutor<T> asyncGuiExecutor;

    private AsyncGui(AsyncGuiExecutor<T> asyncGuiExecutor) {
        this.asyncGuiExecutor = asyncGuiExecutor;
    }

    public static <T> AsyncGui<T> async(AsyncGuiExecutor<T> asyncGuiExecutor) {
        return new AsyncGui<T>(asyncGuiExecutor);
    }

    public AsyncGuiExecutor<T> getAsyncGuiExecutor() {
        return asyncGuiExecutor;
    }
}
