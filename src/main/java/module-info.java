module sample {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;


    opens sample to javafx.fxml;
    exports sample;
}