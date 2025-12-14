package neutron;

import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Neutron extends Application {
    static private Controller controller = null;
    static private String title = "Neutron App";
    static private String initialBackgroundColor = "#ffffff";
    static private String htmlPath = "ui/index.html";
    static private double width = 630;
    static private double height = 410;
    static private boolean verbose = true;
    static private Application app = null;
    static private StageStyle sStyle = StageStyle.DECORATED;

    public static void setVerbose(boolean verbose) {
        if (!verbose) {
            System.out.println("Neutron verbose set to false");
        } else {
            System.out.println("Neutron verbose set to true");
        }
        Neutron.verbose = verbose;
    }

    public static boolean isVerbose() {
        return Neutron.verbose;
    }

    public static void launch() {
        setVerbose(false);
        Neutron.controller = checkController(null);
        Neutron.controller.onAfterMount((c) -> {
            c.execJs("document.getElementById('rpl').innerText = 'PID: " + ProcessHandle.current().pid() + "' ;");
        });
        Neutron.launch(Neutron.class);
    }

    public static void launch(String htmlPath) {
        Neutron.controller = checkController(null);
        Neutron.htmlPath = htmlPath;
        Neutron.launch(Neutron.class);
    }

    public static void launch(String[] args) {
        setVerbose(false);
        Neutron.controller = checkController(null);
        Neutron.controller.onAfterMount((c) -> {
            c.execJs("document.getElementById('rpl').innerText = 'PID: " + ProcessHandle.current().pid() + "' ;");
        });
        Neutron.launch(Neutron.class, args);
    }

    public static void launch(Controller ctrl, String htmlPath, String title, double width,
            double height, String initialBackgroundColor, StageStyle stageStyle, String[] args) {

        Neutron.controller = checkController(ctrl);
        Neutron.sStyle = (stageStyle != null) ? stageStyle : Neutron.sStyle;
        Neutron.htmlPath = (htmlPath != null) ? htmlPath : Neutron.htmlPath;
        Neutron.title = (title != null) ? title : "no title";
        Neutron.width = (width >= 0) ? width : Neutron.width;
        Neutron.height = (height >= 0) ? height : Neutron.height;
        Neutron.initialBackgroundColor = (initialBackgroundColor != null) ? initialBackgroundColor
                : Neutron.initialBackgroundColor;
        Neutron.launch(Neutron.class, args);
    }

    private static class MyController extends Controller {
    }

    private static Controller checkController(Controller ctrl) {
        if (ctrl == null) {
            return new MyController();
        }
        if (ctrl.getClass().isAnonymousClass()) {
            System.err.println(
                    "Controller cannot be anonymous nor can be implemented\ncreate a stand alone class that extends neutron.Controller");
            System.exit(1);
        }
        return ctrl;
    }

    public static void show(Stage stage) {
        stage.show();
    }

    public static void show(Controller c, String htmlPath, String title, double w,
            double h, String initialBackgroundColor, StageStyle stageStyle) {
        if (app == null) {
            System.err.println("Neurtron needs to be launched first before show()");
            System.exit(1);
        }
        var ctrl = checkController(c);
        var stage = newPage(new Stage(), stageStyle, ctrl, htmlPath, title, w, h, initialBackgroundColor);
        if (ctrl.isInitiallyVisible()) {
            stage.show();
        }
    }

    public static void showAndWait(Controller c, String htmlPath, String title, double w,
            double h, String initialBackgroundColor, StageStyle stageStyle) {
        if (app == null) {
            System.err.println("Neurtron needs to be launched first before show()");
            System.exit(1);
        }
        var ctrl = checkController(c);
        var stage = newPage(new Stage(), stageStyle, ctrl, htmlPath, title, w, h, initialBackgroundColor);
        if (ctrl.isInitiallyVisible()) {
            stage.showAndWait();
        }
    }

    public static builder builder() {
        return new builder();
    }

    // Builder pattern for Neutron configuration
    public static class builder {
        private String htmlPath = "ui/index.html";
        private String title = "Neutron App";
        private double width = 630;
        private double height = 410;
        private String initialBackgroundColor = "#ffffff";
        private Controller controller = null;
        private StageStyle stageStyle = StageStyle.DECORATED;

        public builder(String... kotlinOverloading) {
        }

        public builder htmlPath(String htmlPath) {
            this.htmlPath = htmlPath;
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

        public builder initialBackgroundColor(String initialBackgroundColor) {
            this.initialBackgroundColor = initialBackgroundColor;
            return this;
        }

        public builder controller(Controller controller) {
            this.controller = checkController(controller);
            return this;
        }

        public builder DevServer(int port) {
            DevServer.create(port);
            return this;
        }

        public builder stageStyle(StageStyle stageStyle) {
            this.stageStyle = stageStyle;
            return this;
        }

        public void launch(String[] args) {

            Neutron.launch(this.controller, this.htmlPath, this.title, this.width, this.height,
                    this.initialBackgroundColor, this.stageStyle,
                    args);
        }

        public void show() {
            Neutron.show(this.controller, this.htmlPath, this.title, this.width, this.height,
                    this.initialBackgroundColor,
                    this.stageStyle);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        JSON.mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        if (controller == null) {
            System.err.println("a controller is not set. Please create a class that extends Controller and set it.");
            System.exit(1);
        }
        Neutron.app = this;
        newPage(primaryStage, sStyle, controller, htmlPath, title, Neutron.width, Neutron.height,
                initialBackgroundColor);
        if (controller.isInitiallyVisible()) {
            primaryStage.show();
        }
    }

    private static Stage newPage(Stage primaryStage, StageStyle stageStyle, Controller ctrl, String htmlPath,
            String title, double w, double h, String initialBackgroundColor) {
        ctrl._start();
        WebView webView = new WebView();
        StackPane stack = new StackPane();
        stack.setStyle("-fx-background-color: " + initialBackgroundColor + ";");
        webView.setPageFill(Color.web(initialBackgroundColor));
        ResourceExtractor.ensureOnFilesystem(htmlPath);
        webView.getEngine().load(Paths.get(htmlPath).toUri().toString());
        primaryStage.initStyle(stageStyle);
        primaryStage.setTitle(title);
        stack.getChildren().add(webView);
        ctrl.attachController(webView, primaryStage, stack);
        primaryStage.setScene(new Scene(stack, w, h, Color.web(initialBackgroundColor)));
        return primaryStage;
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
        ctrl.setHiddenCancelButton();
        Neutron.showAndWait(ctrl, ctrl.getMsgBoxHtmlPath(), ctrl.getTitle(), ctrl.getWidth(), ctrl.getHeight(),
                ctrl.getBackgroundColor(), ctrl.getStageStyle());
    }

    public static boolean confirm(MsgBoxController ctrl) {
            Neutron.showAndWait(ctrl, ctrl.getMsgBoxHtmlPath(), ctrl.getTitle(), ctrl.getWidth(), ctrl.getHeight(),
                    ctrl.getBackgroundColor(), ctrl.getStageStyle());
        return ctrl.getConfirmResult();
    }
}
