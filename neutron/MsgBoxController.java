package neutron;
import javafx.stage.StageStyle;
import netscape.javascript.JSObject;

public class MsgBoxController extends Controller {
    private String msgBoxHtmlPath = "ui/msgbox.html";
    private String message = "";
    private String title = "Message Box";
    
    private String okButtonText = "OK";
    private String cancelButtonText = "Cancel";
    private double height = 190;
    private double width = 320;
    private String bgColor = "#ffffff";
    private StageStyle stageStyle = StageStyle.UTILITY;
    private boolean confirmResult;
    private boolean isCancelButtonHidden = false;

    public MsgBoxController() {
        onBeforeMount( (ctrl) -> {
            getPrimaryStage().setAlwaysOnTop(true);
            getPrimaryStage().setResizable(false);
        });
        onAfterMount( (ctrl) -> {
            JSObject messageElement = (JSObject) getEngine().executeScript("document.getElementById('message')");
            JSObject okButtonElement = (JSObject) getEngine().executeScript("document.getElementById('ok-button')");
            JSObject cancelButtonElement = (JSObject) getEngine().executeScript("document.getElementById('cancel-button')");
            if (messageElement == null) {System.err.println("No element with id 'message' found in the alert/confirm HTML."); close(); return;}
            if (okButtonElement == null) {System.err.println("No element with id 'ok-button' found in the alert/confirm HTML."); close(); return;}
            if (cancelButtonElement == null) {System.err.println("No element with id 'cancel-button' found in the confirm HTML."); close(); return;}
            messageElement.setMember("innerHTML", message);
            okButtonElement.setMember("innerHTML", okButtonText);
            cancelButtonElement.setMember("innerHTML", cancelButtonText);
            hideCancelButton();
            getEngine().executeScript("""
                document.getElementById('ok-button').addEventListener('click', () => {
                    window.java.setConfirmResult(true);
                });
                document.getElementById('cancel-button').addEventListener('click', () => {
                    window.java.setConfirmResult(false);
                });
            """);
        });
    }

    private void hideCancelButton() {
        if (!isCancelButtonHidden) return;
        getEngine().executeScript("document.getElementById('cancel-button').style.display = 'none';");
    }

    public String getMsgBoxHtmlPath() { return msgBoxHtmlPath; }
    public String getTitle() { return title; }
    public String getOkButtonText() { return okButtonText; }
    public String getCancelButtonText() { return cancelButtonText; }
    public String getMessage() { return message; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public String getBackgroundColor() { return bgColor; }
    public StageStyle getStageStyle() { return stageStyle; }
    public MsgBoxController setStageStyle(StageStyle stageStyle) { this.stageStyle = stageStyle; return this; }
    public MsgBoxController setMsgBoxHtmlPath(String msgBoxHtmlPath) { this.msgBoxHtmlPath = msgBoxHtmlPath; return this; }
    public MsgBoxController setTitleInnerHTML(String title) { this.title = title; return this; }
    public MsgBoxController setOkButtonText(String okButtonText) { this.okButtonText = okButtonText; return this; }
    public MsgBoxController setCancelButtonText(String cancelButtonText) { this.cancelButtonText = cancelButtonText; return this; }
    public MsgBoxController setBackgroundColor(String backgroundColor) { this.bgColor = backgroundColor; return this; }
    public MsgBoxController setMessageInnerHTML(String message) { this.message = message; return this; }
    public MsgBoxController setHiddenCancelButton() { this.isCancelButtonHidden = true; return this; }
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

    public static MsgBoxController from(MsgBoxController msgCtrl) {
        MsgBoxController newCtrl = new MsgBoxController();
        newCtrl.setMsgBoxHtmlPath(msgCtrl.getMsgBoxHtmlPath());
        newCtrl.setTitleInnerHTML(msgCtrl.getTitle());
        newCtrl.setOkButtonText(msgCtrl.getOkButtonText());
        newCtrl.setCancelButtonText(msgCtrl.getCancelButtonText());
        newCtrl.setMessageInnerHTML(msgCtrl.getMessage());
        newCtrl.setSize(msgCtrl.getWidth(), msgCtrl.getHeight());
        newCtrl.setBackgroundColor(msgCtrl.getBackgroundColor());
        newCtrl.setStageStyle(msgCtrl.getStageStyle());
        return newCtrl;
    }

}