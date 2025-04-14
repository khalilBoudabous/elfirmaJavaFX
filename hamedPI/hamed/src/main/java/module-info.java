module hamed.hamed {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.sql;

    // Export the package containing your MainFx class to javafx.graphics
    exports Main to javafx.graphics;

    // Open the package containing your MainFx class to javafx.fxml (for FXML loading)
    opens Main to javafx.fxml;

    // Export the Controller package to javafx.fxml
    exports Controller to javafx.fxml;

    // Open the Controller package to javafx.fxml (for FXML loading)
    opens Controller to javafx.fxml;

    // Export the Models package to make Message accessible
    exports Models;

    // Add any additional exports or opens as needed
}