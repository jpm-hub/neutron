package neutron;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventType;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import netscape.javascript.JSObject;

public abstract class Controller {
    private WebView webView;
    private WebEngine engine;
    private Stage primaryStage;
    private StackPane root;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isDomReady = false;
    private CtrlRunnable onAfterMount = null;
    private CtrlRunnable onStart = null;
    private CtrlRunnable onBeforeMount = null;
    private CtrlRunnableEvent onStop = null;
    private List<String> listOfDraggableElements = new ArrayList<>();
    private MsgBoxController msgCtrl = null;

    public static interface CtrlRunnableEvent {
        void run(Controller ctrl, EventType<WindowEvent> eventType);
    }

    public static interface CtrlRunnable {
        void run(Controller ctrl);
    }

    public final void attachController(WebView webView, Stage primaryStage, StackPane root) {
        this.webView = webView;
        this.engine = webView.getEngine();
        this.primaryStage = primaryStage;
        this.root = root;
        _beforeMount();
        this.engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED && this != null) {
                ((JSObject) this.engine.executeScript("window")).setMember("java", this);
                _makeDomReady();
                _afterMount();
            }
        });
        this.primaryStage.setOnCloseRequest(e -> {
            _stop(e.getEventType());
        });
    }

    public final void loadHTML(String htmlResourcePath) {
        _beforeMount();
        isDomReady = false;
        var resourceUrl = Neutron.class.getResource(htmlResourcePath).toExternalForm();
        if( Neutron.isVerbose() ) {
            System.out.println("[NEUTRON-VERBOSE] Loading HTML resource from: " + resourceUrl);
        }
        ResourceExtractor.ensureOnFilesystem(htmlResourcePath);
        engine.load(Paths.get(htmlResourcePath).toUri().toString());
    }

    final void _makeDomReady() {
        isDomReady = true;
        engine.executeScript("""
            (()=>{
                window.js = {};
                window.onerror = function(message, source, lineno, colno, error) {
                    if (window.java && window.java.print) {
                        window.java.print(`JS ERROR: ${message}\n at ${source}:${lineno}:${colno}`);
                    }
                };
                ['log', 'warn', 'error','info','trace','time','timeEnd','timeStamp','timeLog','assert'].forEach(t => {
                    const orig = console[t];
                    console[t] = (...args) => {
                        orig(...args);
                        window.java.print(`[console.${t}] ${args.join(' ')}`);
                    };
                });
                window.dispatchEvent(new CustomEvent("neutron-ready"));
            })();
            """.formatted(Neutron.class.getResource("/").toExternalForm()));

        if (Neutron.isVerbose()) {
            System.out.println(
                """
                [NEUTRON-VERBOSE] 'neutron-ready' event dispatched on window !!!
                [NEUTRON-VERBOSE] DOM is ready to call Controller Methods through 'window.java.<your_java_function_name>(args...);'
                [NEUTRON-VERBOSE] Controller is ready to call JS functions through controller.call(<your_js_function>, args...);""");
        }
    }

    public final boolean isDomReady() {
        return isDomReady;
    }

    public final void execJs(String code) {
        if (isDomReady == false) {
            System.out.println("'neutron-ready' event did not dispatch yet, cannot eval js code");
            return;
        }
        if (Neutron.isVerbose())
            System.out.println("[NEUTRON-VERBOSE] " + code);
        Platform.runLater(() -> engine.executeScript(code));
    }

    public final void call(String funcName) {
        if (isDomReady == false) {
            System.out.println("'neutron-ready' event did not dispatch yet, cannot call " + funcName);
            return;
        }
        if (Neutron.isVerbose())
            System.out.println("[NEUTRON-VERBOSE] window.js." + funcName + "();");
        Platform.runLater(() -> engine.executeScript("window.js." + funcName + "();"));
    }

    public final void call(String funcName, JSON arg) {
        if (isDomReady == false) {
            System.out.println("'neutron-ready' event did not dispatch yet, cannot call " + funcName);
            return;
        }
        StringBuilder s = new StringBuilder("window.js.");
        s.append(funcName);
        s.append("(");
        s.append(JSON.stringValue(arg));
        s.append(");");
        if (Neutron.isVerbose())
            System.out.println("[NEUTRON-VERBOSE] " + s.toString());
        Platform.runLater(() -> engine.executeScript(s.toString()));
    }

    public final void call(String funcName, Object... args) {
        if (isDomReady == false) {
            System.out.println("'neutron-ready' event did not dispatch yet, cannot call " + funcName);
            return;
        }
        StringBuilder s = new StringBuilder("window.js.");
        s.append(funcName);
        s.append("(");
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String)
                s.append("\'").append(args[i]).append("\'");
            else
                s.append(args[i]);
            if (i < args.length - 1)
                s.append(",");
        }
        s.append(");");
        if (Neutron.isVerbose())
            System.out.println("[NEUTRON-VERBOSE] " + s.toString());
        Platform.runLater(() -> engine.executeScript(s.toString()));
    }

    public final void emit(String event) {
        if (isDomReady == false) {
            System.out.println("'neutron-ready' event did not dispatch yet, cannot emit");
            return;
        }
        if (Neutron.isVerbose())
            System.out.println("[NEUTRON-VERBOSE] window.dispatchEvent(new CustomEvent(\"" + event + "\"));");
        Platform.runLater(() -> engine.executeScript("window.dispatchEvent(new CustomEvent(\"" + event + "\"));"));
    }

    public final void emit(String event, JSON arg) {
        if (isDomReady == false) {
            System.out.println("'neutron-ready' event did not dispatch yet, cannot emit");
            return;
        }
        StringBuilder s = new StringBuilder("window.dispatchEvent(new CustomEvent(\"" + event + "\",");
        s.append(JSON.stringValue(arg));
        s.append("));");
        if (Neutron.isVerbose())
            System.out.println("[NEUTRON-VERBOSE] " + s.toString());
        Platform.runLater(() -> engine.executeScript(s.toString()));
    }

    public final void emit(String event, Object data) {
        if (isDomReady == false) {
            System.out.println("'neutron-ready' event did not dispatch yet, cannot emit");
            return;
        }
        StringBuilder s = new StringBuilder("window.dispatchEvent(new CustomEvent(\"" + event + "\",");
        if (data instanceof String)
            s.append("\'").append(data).append("\'");
        else
            s.append(data);
        s.append("));");
        if (Neutron.isVerbose())
            System.out.println("[NEUTRON-VERBOSE] " + s.toString());
        Platform.runLater(() -> engine.executeScript(s.toString()));
    }

    public final void print(String s) {
        System.out.println(s);
    }

    public void close() {
        try {
            primaryStage.close();
        } catch (NullPointerException e) {
            System.err.println("Cannot close stage, primaryStage before starting");
        }
    }

    public final void setDraggableElement(String htmlNodeId) {
        if (isDomReady == false) {
            System.err.println("'neutron-ready' event did not dispatch yet, cannot call setDraggableElement");
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
            System.out.println("'neutron-ready' event did not dispatch yet, cannot call doDrag");
            return;
        }
        primaryStage.getScene().setCursor(Cursor.CLOSED_HAND);
        primaryStage.setX(x - xOffset);
        primaryStage.setY(y - yOffset);
    }

    public final void _startDrag(double x, double y) {
        if (isDomReady == false) {
            System.out.println("'neutron-ready' event did not dispatch yet, cannot call startDrag");
            return;
        }
        primaryStage.getScene().setCursor(Cursor.OPEN_HAND);
        xOffset = x - primaryStage.getX();
        yOffset = y - primaryStage.getY();
    }

    public final void _endDrag(double x, double y) {
        if (isDomReady == false) {
            System.out.println("'neutron-ready' event did not dispatch yet, cannot call startDrag");
            return;
        }
        primaryStage.getScene().setCursor(Cursor.DEFAULT);
        xOffset = 0;
        yOffset = 0;
    }

    public final void onStart(CtrlRunnable r) {
        this.onStart = r;
    }

    public final void onBeforeMount(CtrlRunnable r) {
        this.onBeforeMount = r;
    }

    public final void onAfterMount(CtrlRunnable r) {
        this.onAfterMount = r;
    }

    public final void onStop(CtrlRunnableEvent r) {
        this.onStop = r;
    }

    final void _start() {
        if (onStart != null) {
            onStart.run(this);
        }
        engine.setOnAlert(event -> {
            var msgBoxController = (this.msgCtrl != null) ? MsgBoxController.from(this.msgCtrl)
                    : new MsgBoxController();
            msgBoxController.setMessageInnerHTML(event.getData());
            Neutron.alert(msgBoxController);
        });
        engine.setConfirmHandler(event -> {
            var msgBoxController = (this.msgCtrl != null) ? MsgBoxController.from(this.msgCtrl)
                    : new MsgBoxController();
            msgBoxController.setMessageInnerHTML(event);
            return Neutron.confirm(msgBoxController);
        });
    }

    final void _stop(EventType<WindowEvent> eventType) {
        if (onStop != null) {
            onStop.run(this, eventType);
        }
    }

    final void _beforeMount() {
        if (onBeforeMount != null) {
            onBeforeMount.run(this);
        }
    }

    final void _afterMount() {
        if (onAfterMount != null) {
            onAfterMount.run(this);
        }
    }

    public final void setMessageBoxController(MsgBoxController msgCtrl) {
        this.msgCtrl = msgCtrl;
    }

    public final WebView getWebView() {
        return webView;
    }

    public final WebEngine getEngine() {
        return engine;
    }

    public final Stage getPrimaryStage() {
        return primaryStage;
    }

    public final StackPane getRootPane() {
        return root;
    }
}