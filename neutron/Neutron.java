package neutron;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

public class Neutron extends Application {
    static private Controller controller = null;
    static private String title = "Neutron App";
    static private String bgColor = "#ffffff";
    static private String htmlResourcePath = "/ui/index.html";
    static private double width = 630;
    static private double height = 410;
    static private boolean verbose = false;
    static private Application app = null;

    public static void setVerbose(boolean verbose) {
        Neutron.verbose = verbose;
    }

    public static boolean isVerbose() {
        return Neutron.verbose;
    }

    public static void launch(Controller controller, String htmlResourcePath, String title, double width,
            double height, String backgroundColor, String[] args) {
        Neutron.controller = controller;
        Neutron.htmlResourcePath = htmlResourcePath;
        Neutron.title = title;
        Neutron.width = width;
        Neutron.height = height;
        Neutron.bgColor = backgroundColor;
        Neutron.launch(Neutron.class, args);
    }

    public static void show(Controller controller, String htmlResourcePath, String title, double width,
            double height, String backgroundColor, StageStyle stageStyle) {
        if (controller == null) {
            throw new RuntimeException("a controller is not set. Please create a class that extends Controller and set it.");
        }
        Stage newStage = new Stage();
        var webView = new WebView();
        var stack = new StackPane();
        controller.setView(webView, newStage, stack);
        controller._start();
        var colorPane = new Pane();
        var engine = webView.getEngine();
        colorPane.setStyle("-fx-background-color: " + bgColor + ";");
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED && controller != null) {
                ((JSObject) engine.executeScript("window")).setMember("java", controller);
                controller.afterMount();
                colorPane.setVisible(false);
                controller._makeDomReady();
            }
            if (controller == null) {
                throw new RuntimeException(
                        "a controller is not set. Please set a controller before launching the application.");
            }
        });
        engine.load(Neutron.class.getResource(htmlResourcePath).toExternalForm());
        stack.getChildren().add(webView);
        stack.getChildren().add(colorPane);
        newStage.initStyle(stageStyle);
        controller.beforeMount();
        newStage.setTitle(title);
        newStage.setScene(new Scene(stack, width, height, Color.web(bgColor)));
        newStage.show();
        newStage.setOnCloseRequest(e -> {
            controller._stop();
        });
    }

    // Builder pattern for Neutron configuration
    public static class builder {
        private String htmlResourcePath = "/ui/index.html";
        private String title = "Neutron App";
        private double width = 630;
        private double height = 410;
        private String bgColor = "#ffffff";
        private Controller controller = null;
        private StageStyle stageStyle = StageStyle.DECORATED;

        public builder htmlResourcePath(String htmlResourcePath) {
            this.htmlResourcePath = htmlResourcePath;
            return this;
        }

        public builder title(String title) {
            this.title = title;
            return this;
        }

        public builder size(double width, double height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public builder backgroundColor(String bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public builder controller(Controller controller) {
            this.controller = controller;
            return this;
        }

        public builder stageStyle(StageStyle stageStyle) {
            this.stageStyle = stageStyle;
            return this;
        }

        public void launch(String[] args) {
            Neutron.launch(this.controller, this.htmlResourcePath, this.title, this.width, this.height, this.bgColor,
                    args);
        }

        public void show() {
            Neutron.show(this.controller, this.htmlResourcePath, this.title, this.width, this.height, this.bgColor,
                    this.stageStyle);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        if (controller == null) {
            System.err.println("a controller is not set. Please create a class that extends Controller and set it.");
            System.exit(1);
        }
        // get Application
        Neutron.app = this;
        WebView webView = new WebView();
        StackPane stack = new StackPane();
        controller.setView(webView, primaryStage, stack);
        controller._start();
        Pane colorPane = new Pane();
        WebEngine engine = webView.getEngine();
        colorPane.setStyle("-fx-background-color: " + bgColor + ";");
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED && controller != null) {
                ((JSObject) engine.executeScript("window")).setMember("java", controller);
                controller._makeDomReady();
                controller.afterMount();
                colorPane.setVisible(false);
            }
            if (controller == null) {
                throw new RuntimeException(
                        "a controller is not set. Please set a controller before launching the application.");
            }
        });
        engine.load(Neutron.class.getResource(htmlResourcePath).toExternalForm());
        primaryStage.setTitle(title);
        stack.getChildren().add(webView);
        stack.getChildren().add(colorPane);
        controller.beforeMount();
        primaryStage.setScene(new Scene(stack, width, height, Color.web(bgColor)));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            controller._stop();
        });
    }

    @Override
    public void stop() {
        Neutron.Stop();
    }

    private static Runnable onStop = () -> {
    };

    private static void Stop() {
        onStop.run();
    }

    @Override
    public void init() {
        Neutron.Init();
    }

    private static Runnable onInit = () -> {
    };

    private static void Init() {
        onInit.run();
    }

    public static void onInit(Runnable r) {
        onInit = r;
    }

    public static void onStop(Runnable r) {
        onStop = r;
    }

    public static HostServices GetHostServices() {
        if (app == null)
            throw new RuntimeException("Application not started yet.");
        return app.getHostServices();
    }

    public static Parameters GetParameters(String[] args) {
        if (app == null)
            throw new RuntimeException("Application not started yet.");
        return app.getParameters();
    }

    public static void NotifyPreloader(Preloader.PreloaderNotification notification) {
        if (app == null)
            throw new RuntimeException("Application not started yet.");
        app.notifyPreloader(notification);
    }

    public static void alert(MsgBoxController ctrl) {
        Neutron.show(ctrl, ctrl.getAlertHtmlPath(), ctrl.getTitle(), ctrl.getWidth(), ctrl.getHeight(),
                ctrl.getBackgroundColor(), ctrl.getStageStyle());
    }
    public static MsgBoxController confirm(MsgBoxController ctrl) {
        Neutron.show(ctrl, ctrl.getConfirmHtmlPath(), ctrl.getTitle(), ctrl.getWidth(), ctrl.getHeight(), ctrl.getBackgroundColor(), ctrl.getStageStyle());
        return ctrl;
    }
}
