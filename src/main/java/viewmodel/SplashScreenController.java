package viewmodel;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SplashScreenController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onButtonClick() {

        welcomeText.setText("Welcome to JavaFX Application!");
    }
}