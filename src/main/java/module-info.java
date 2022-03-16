module com.example.projektfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.projektfx to javafx.fxml;
    exports com.example.projektfx;
}