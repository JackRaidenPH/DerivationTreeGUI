module dev.jackraidenph {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.jpl7;

    opens dev.jackraidenph to javafx.fxml;
    exports dev.jackraidenph;
}