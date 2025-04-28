module hamed.hamed {
    requires cloudinary.core;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires okhttp3;
    requires org.json;
    requires com.google.gson;
    requires org.slf4j;
    requires logback.classic;
    
    opens Main to javafx.fxml;
    opens Controller to javafx.fxml;
    exports Main;
    exports Models;
    exports Services;
    exports Utils;
    exports Controller;
}