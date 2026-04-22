package com.eventorganizer.ui.components;

import com.eventorganizer.exceptions.AppException;

import javax.swing.AbstractButton;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Thin SwingWorker wrapper for primary button actions.
 *
 * Usage keeps button handlers on the EDT-safe side while the service call runs
 * in the background. The source button is disabled with a "…" affordance for
 * the lifetime of the work and restored on completion (success or failure).
 *
 * Services currently complete in microseconds because DataStore is in-memory,
 * so users won't see the loading state — but the pattern is right for when
 * data grows or persistence lands.
 */
public final class AsyncUI {
    private AsyncUI() {}

    public static <T> void run(AbstractButton source,
                               Supplier<T> work,
                               Consumer<T> onSuccess,
                               Consumer<AppException> onError) {
        final String originalText = source == null ? null : source.getText();
        if (source != null) {
            source.setEnabled(false);
            source.setText(originalText + " …");
        }

        new SwingWorker<T, Void>() {
            AppException appErr;
            RuntimeException runtimeErr;

            @Override protected T doInBackground() {
                try { return work.get(); }
                catch (AppException e) { appErr = e; return null; }
                catch (RuntimeException e) { runtimeErr = e; return null; }
            }

            @Override protected void done() {
                if (source != null) {
                    source.setText(originalText);
                    source.setEnabled(true);
                }
                if (appErr != null) { onError.accept(appErr); return; }
                if (runtimeErr != null) {
                    onError.accept(new AppException(
                        "Something went wrong: " + runtimeErr.getMessage(),
                        com.eventorganizer.exceptions.ErrorCode.ERR_INVALID_OPERATION));
                    return;
                }
                try { onSuccess.accept(get()); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                catch (ExecutionException ee) {
                    Throwable cause = ee.getCause();
                    onError.accept(new AppException(
                        cause == null ? "Unexpected error" : cause.getMessage(),
                        com.eventorganizer.exceptions.ErrorCode.ERR_INVALID_OPERATION));
                }
            }
        }.execute();
    }

    /** Convenience for actions that return nothing. */
    public static void run(AbstractButton source,
                           Runnable work,
                           Runnable onSuccess,
                           Consumer<AppException> onError) {
        run(source,
            () -> { work.run(); return null; },
            v -> onSuccess.run(),
            onError);
    }

    /** When the handler already manages its own toasts, pass the parent component. */
    public static void runWithToast(AbstractButton source,
                                    Component toastAnchor,
                                    Runnable work,
                                    Runnable onSuccess) {
        run(source, work, onSuccess, ex -> Toast.error(toastAnchor, ex.getMessage()));
    }
}
