package neutron;

public class App {
    public static class MyController extends Controller { }
    static Controller ctrl = new MyController();
    public static void main(String[] args) {
        ctrl.onAfterMount(App::showPID);
        new Neutron.builder().controller(ctrl).launch(args);
    }
    static void showPID() {
        ctrl.call("showPID", ProcessHandle.current().pid());
        ctrl.setDraggableElement("logo");
    }
}