module dev.jackraidenph.dertreegui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.jpl7;
    requires java.logging;

    opens dev.jackraidenph.dertreegui to javafx.fxml;
    exports dev.jackraidenph.dertreegui;
}