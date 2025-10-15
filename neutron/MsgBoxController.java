package neutron;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

public class MsgBoxController extends Controller {
    private String alertHtmlPath = "/alert.html";
    private String confirmHtmlPath = "/confirm.html";
    private String message = "";
    private String title = "";
    
    private String okButtonText = "OK";
    private String cancelButtonText = "Cancel";
    private double height = 170;
    private double width = 310;
    private String bgColor = "#ffffff;";
    private StageStyle stageStyle = StageStyle.UTILITY;
    private boolean confirmResult;

    public MsgBoxController() {
        onStart( (primaryStage, webView, engine) -> {
            primaryStage.setAlwaysOnTop(true);
            primaryStage.setResizable(false);
        });
        onAfterMount( (primaryStage, webView, engine) -> {
            JSObject messageElement = (JSObject) engine.executeScript("document.getElementById('message')");
            JSObject okButtonElement = (JSObject) engine.executeScript("document.getElementById('ok-button')");
            if (messageElement == null) {System.err.println("No element with id 'message' found in the alert/confirm HTML."); close();}
            if (okButtonElement == null) {System.err.println("No element with id 'ok-button' found in the alert/confirm HTML."); close();}
            messageElement.setMember("innerHTML", message);
            okButtonElement.setMember("innerHTML", okButtonText);
            //
            // Somebody please fix this ... 
            //
            // JSObject cancelButtonElement = (JSObject) engine.executeScript("document.getElementById('cancel-button')");
            // if (cancelButtonElement == null) {
            //     System.err.println("No element with id 'cancel-button' found in the confirm HTML.");
            //     close();
            // }
            // cancelButtonElement.setMember("innerHTML", cancelButtonText);
        });
    }

    public String getAlertHtmlPath() { return alertHtmlPath; }
    public String getConfirmHtmlPath() { return confirmHtmlPath; }
    public String getTitle() { return title; }
    public String getOkButtonText() { return okButtonText; }
    public String getCancelButtonText() { return cancelButtonText; }
    public String getMessage() { return message; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public String getBackgroundColor() { return bgColor; }
    public StageStyle getStageStyle() { return stageStyle; }
    public MsgBoxController setAlertHtmlPath(String alertHtmlPath) { this.alertHtmlPath = alertHtmlPath; return this; }
    public MsgBoxController setConfirmHtmlPath(String confirmHtmlPath) { this.confirmHtmlPath = confirmHtmlPath; return this; }
    public MsgBoxController setTitleInnerHTML(String title) { this.title = title; return this; }
    public MsgBoxController setOkButtonText(String okButtonText) { this.okButtonText = okButtonText; return this; }
    public MsgBoxController setCancelButtonText(String cancelButtonText) { this.cancelButtonText = cancelButtonText; return this; }
    public MsgBoxController setMessageInnerHTML(String message) { this.message = message; return this; }
    public MsgBoxController setSize(double width, double height) {
        this.width = width;
        this.height = height;
        return this;
    }
    public void setConfirmResult(boolean result) { 
        this.confirmResult = result;
        close();
    }
    
    public boolean getConfirmResult() {
        return this.confirmResult;
    }
}