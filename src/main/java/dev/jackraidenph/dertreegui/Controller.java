package dev.jackraidenph.dertreegui;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jpl7.PrologException;
import org.jpl7.Query;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

public class Controller {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(_\\d+)");
    private static final int LEVEL_OFFSET = 3;
    private static String PATH_TO_SOURCE = "";

    @FXML
    private MenuItem chooseFileOption;

    @FXML
    private TextArea sourceContents;

    @FXML
    private TextField queryField;

    @FXML
    private CheckBox optimizeCheckbox;

    @FXML
    void onChooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        FileChooser.ExtensionFilter extFilter = new FileChooser
                .ExtensionFilter("Prolog source files (*.pl)", "*.pl");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(sourceContents.getScene().getWindow());

        if (file != null) {
            try {
                sourceContents.setText(new String(Files.readAllBytes(file.toPath())));
                PATH_TO_SOURCE = file.toPath().toAbsolutePath().toString().replaceAll("\\\\", "/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onDraw(ActionEvent actionEvent) {
        try {
            Query initQuery = new Query("consult('%s')".formatted(PATH_TO_SOURCE));
            initQuery.hasSolution();

            Query solutionQuery
                    = new Query("""
                    leash(-all),
                    visible([+full]),
                    protocol('trace.txt'),
                    trace,
                    %s,
                    notrace,
                    noprotocol.""".formatted(queryField.getText()));

            solutionQuery.hasSolution();
        } catch (PrologException e) {
            e.printStackTrace();
        }

        int level = -1;
        Node root = new Node(null, level, "", "find(X, Y)");
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream("trace.txt"), StandardCharsets.UTF_8))) {

            int REPLACEMENT_COUNTER = 0;

            String line;
            while ((line = in.readLine()) != null) {
                int firstOpening = line.indexOf('(');
                int firstClosing = line.indexOf(')');

                try {
                    String type = line.substring(0, firstOpening).trim();
                    String message = line.substring(firstClosing + 1).trim();

                    if (VARIABLE_PATTERN.matcher(message).results().findAny().isPresent()) {
                        int cycles = REPLACEMENT_COUNTER / 26;
                        int letter = REPLACEMENT_COUNTER % 26;
                        String postfix = (cycles > 0 ? String.valueOf(cycles) : "");
                        String name = (char) ((int) 'A' + letter) + postfix;
                        message = message.replaceAll(VARIABLE_PATTERN.pattern(), name);
                        REPLACEMENT_COUNTER++;
                    }

                    level = Integer.parseInt(line.substring(firstOpening + 1, firstClosing)) - LEVEL_OFFSET;

                    root = root.trace(level, type, message);
                } catch (Exception ex) {
                    System.err.printf("Line node skipped due to a risen exception!\n\t%s\n", ex.getLocalizedMessage());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (optimizeCheckbox.isSelected()) {
            root.optimize();
        }

        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("draw-view.fxml"));
            Parent parent = loader.load();
            DrawController drawController = loader.getController();
            drawController.setParent(this);
            drawController.setRoot(root);
            Stage stage = new Stage();
            stage.setTitle("Derivation Tree");
            stage.setScene(new Scene(parent, 960, 540));
            stage.setResizable(true);
            stage.show();
            drawController.draw();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter pw = new PrintWriter("out.txt", StandardCharsets.UTF_8)) {
            pw.println(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
