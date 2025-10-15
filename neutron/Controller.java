package neutron;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import netscape.javascript.JSObject;

public abstract class Controller {
    protected WebView webView;
    protected WebEngine engine;
    protected Stage primaryStage;
    protected Pane root;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isDomReady = false;
    private ControllerRunnable onStart = null;
    private ControllerRunnable onBeforeMount = null;
    private ControllerRunnable onAfterMount = null;
    private ControllerRunnable onStop = null;
    private Runnable ronStart = null;
    private Runnable ronBeforeMount = null;
    private Runnable ronAfterMount = null;
    private Runnable ronStop = null;
    private List<String> listOfDraggableElements = new ArrayList<>();
    private boolean confirmCallbackSet = false;
    private boolean alertHandlerSet = false;

    public interface ControllerRunnable {
        void run(Stage primaryStage, WebView webView, WebEngine engine);
    }


    public final void setView(WebView webView, Stage primaryStage, Pane root) {
        this.webView = webView;
        this.engine = webView.getEngine();
        this.primaryStage = primaryStage;
        this.root = root;
    }

    public final void _makeDomReady() {
        isDomReady = true;
        engine.executeScript("""
            (()=>{
                window.js = {};
                document.dispatchEvent(new CustomEvent("neutron-ready"));
                window.onerror = function(message, source, lineno, colno, error) {
                    if (window.java && window.java.log) {
                        window.java.log(`JS ERROR:  at ::`);
                    }
                };
                ['log', 'warn', 'error'].forEach(t => {
                    const orig = console[t];
                    console[t] = (...args) => {
                        orig(...args);
                        window.java.log(`[${t.toUpperCase()}] ${args.join(' ')}`);
                    };
                });
            })();
        """);

        if (Neutron.isVerbose()) {
            System.out.println("'neutron-ready' event dispatched on document, DOM is ready to call Java functions and controller is ready to call JS functions");
        }
    }
    protected final boolean isDomReady() {return isDomReady;}
    protected final void call(String func) {
        if (isDomReady == false) {
            System.out.println("DOM not ready yet, cannot call " + func);
            return;
        }
        if (Neutron.isVerbose())
            System.out.println("window.js." + func + "();");
        Platform.runLater(() -> engine.executeScript("window.js." +func + "();"));
    }

    protected final void call(String func, JSON... args) {
        if (isDomReady == false) {
            System.out.println("DOM not ready yet, cannot call " + func);
            return;
        }
        StringBuilder s = new StringBuilder("window.js.");
        s.append(func);
        s.append("(");
        for (int i = 0; i < args.length; i++) {
            s.append(JSON.stringValue(args[i]));
            if (i < args.length - 1)
                s.append(",");
        }
        s.append(");");
        if (Neutron.isVerbose())
            System.out.println(s.toString());
        Platform.runLater(() -> engine.executeScript(s.toString()));
    }

    protected final void call(String func, Object... args) {
        if (isDomReady == false) {
            System.out.println("DOM not ready yet, cannot call " + func);
            return;
        }
        StringBuilder s = new StringBuilder("window.js.");
        s.append(func);
        s.append("(");
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) s.append("\'").append(args[i]).append("\'");
            else s.append(args[i]);
            if (i < args.length - 1) s.append(",");
        }
        s.append(");");
        if (Neutron.isVerbose())
            System.out.println(s.toString());
        Platform.runLater(() -> engine.executeScript(s.toString()));
    }

    public void log(String s) {System.out.println(" JS > " + s);}
    public void close() {primaryStage.close();}

    public final void setDraggableElement(String htmlNodeId) {
        if (isDomReady == false) {
            System.err.println("DOM not ready yet, cannot call setDraggableElement");
            return;
        }
        JSObject element = (JSObject) engine.executeScript(
                "document.getElementById('%s')".formatted(htmlNodeId));
        if (element == null) {
            System.err.println("Element with id " + htmlNodeId + " not found");
            return;
        }
        if (listOfDraggableElements.contains(htmlNodeId)) {
            System.err.println("Element with id " + htmlNodeId + " is already draggable");
            return;
        }
        engine.executeScript("""
            (()=>{
                const el = document.getElementById('%s');
                let isDragging = false;
                el.addEventListener('mousedown', e => {
                    e.preventDefault();
                    isDragging = true;
                    window.java._startDrag(e.screenX, e.screenY);
                    document.body.style.userSelect = 'none';
                    const move = e => {
                        if (isDragging) window.java._doDrag(e.screenX, e.screenY);
                    };
                    const up = e => {
                        if (!isDragging) return;
                        isDragging = false;
                        window.java._endDrag();
                        document.body.style.userSelect = 'auto';
                        document.removeEventListener('mousemove', move);
                        el.removeEventListener('mouseup', up);
                    };
                    document.addEventListener('mousemove', move);
                    el.addEventListener('mouseup', up);
            });})();""".formatted(htmlNodeId));
        listOfDraggableElements.add(htmlNodeId);
    }

    public final void _doDrag(double x, double y) {
        if (isDomReady == false) {
            System.out.println("DOM not ready yet, cannot call doDrag");
            return;
        }
        primaryStage.getScene().setCursor(Cursor.CLOSED_HAND);
        primaryStage.setX(x - xOffset);
        primaryStage.setY(y - yOffset);
    }
    public final void _startDrag(double x, double y) {
        if (isDomReady == false) {
            System.out.println("DOM not ready yet, cannot call startDrag");
            return;
        }
        primaryStage.getScene().setCursor(Cursor.OPEN_HAND);
        xOffset = x - primaryStage.getX();
        yOffset = y - primaryStage.getY();
    }
    public final void _endDrag(double x, double y) {
        if (isDomReady == false) {
            System.out.println("DOM not ready yet, cannot call startDrag");
            return;
        }
        primaryStage.getScene().setCursor(Cursor.DEFAULT);
        xOffset = 0;
        yOffset = 0;
    }

    public final void onStart(ControllerRunnable r) { this.onStart = r; }
    public final void onBeforeMount(ControllerRunnable r) { this.onBeforeMount = r; }
    public final void onAfterMount(ControllerRunnable r) { this.onAfterMount = r; }
    public final void onStop(ControllerRunnable r) { this.onStop = r; }
    public final void onStart(Runnable r) { this.ronStart = r; }
    public final void onBeforeMount(Runnable r) { this.ronBeforeMount = r; }
    public final void onAfterMount(Runnable r) { this.ronAfterMount = r; }
    public final void onStop(Runnable r) { this.ronStop = r; }

    public final void _start() {
        if (ronStart != null) {
            ronStart.run();
        }else if (onStart != null) {
            onStart.run(primaryStage, webView, engine);
        }
        if (!alertHandlerSet) {
            engine.setOnAlert(event -> {
                var msgBoxController = new MsgBoxController();
                msgBoxController.setMessageInnerHTML(event.getData());
                Neutron.alert(msgBoxController);
            });
        }
        // if (!confirmCallbackSet) {
        engine.setConfirmHandler(event -> {
            throw new UnsupportedOperationException("""
            \n___________________________________________________________________________________
                Confirm dialog not yet supported, please just make a modal in your application.
                or help fix this ...
                Somebody please fix this: https://github.com/jpm-hub/neutron
            ___________________________________________________________________________________
            """);
        });
        //}
    }

    public final void _stop() {
        if (ronStop != null) {
            ronStop.run();
        } else
        if (onStop != null) {
            onStop.run(primaryStage, webView, engine);
        }
    }

    public final void beforeMount() {
        if (ronBeforeMount != null) {
            ronBeforeMount.run();
        } else if (onBeforeMount != null) {
            onBeforeMount.run(primaryStage, webView, engine);
        }
    }

    public final void afterMount() {
        if (ronAfterMount != null) {
            ronAfterMount.run();
        } else if (onAfterMount != null) {
            onAfterMount.run(primaryStage, webView, engine);
        }
    }
    public final void setMessageBoxHandlers(EventHandler<WebEvent<String>> alertHandler, Callback<String, Boolean> confirmCallback) {
        if (alertHandler != null) {engine.setOnAlert(alertHandler); alertHandlerSet = true;}
        if (confirmCallback != null) {engine.setConfirmHandler(confirmCallback); confirmCallbackSet = true;}
    }
}