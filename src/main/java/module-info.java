module com.example.tbproglan {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.tbproglan to javafx.fxml;
    exports com.example.tbproglan;
}