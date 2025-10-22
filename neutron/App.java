package neutron;

public class App {
    public static class MyController extends Controller {
    }

    public static void main(String[] args) {
        Neutron.setVerbose(true);
        Neutron.launch("/ui/index.html");
    }

    public static void starter(Controller c) {
        // Dialog<Boolean> d = new Dialog<>();
        // d.showAndWait();
        c.getPrimaryStage().widthProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Stage width changed: " + newVal);
        });

        c.getPrimaryStage().heightProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Stage height changed: " + newVal);
        });
    }
}