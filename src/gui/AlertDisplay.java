package gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Interface for objects that can display errors in JavaFX Alerts
 * It has methods with default implementation since Application classes
 * should derive from both this and javafx.application.Application
 */
public interface AlertDisplay {
    default void showInfo(String message) {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION,
                message,
                ButtonType.OK);
        dlg.show();
    }

    default void showError(String message) {
        Alert dlg = new Alert(Alert.AlertType.ERROR,
                message,
                ButtonType.OK);
        dlg.show();
    }
}
