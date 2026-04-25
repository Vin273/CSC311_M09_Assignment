package viewmodel;

import dao.DbConnectivityClass;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Major;
import model.Person;
import service.CSVService;
import service.MyLogger;
import service.PDFReportService;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class DB_GUI_Controller implements Initializable {

    @FXML
    private ComboBox<Major> majorCombo;
    @FXML
    Label statusLabel;
    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    private Button addButton, editButton, deleteButton;
    @FXML
    private MenuItem editItem, deleteItem;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    private final CSVService csvService = new CSVService();
    private final PDFReportService pdfService = new PDFReportService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            majorCombo.setItems(FXCollections.observableArrayList(Major.values()));
            majorCombo.getSelectionModel().selectFirst();

            editItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteItem.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());

            editButton.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            deleteButton.disableProperty().bind(tv.getSelectionModel().selectedItemProperty().isNull());
            addButton.disableProperty().bind(
                    first_name.textProperty().isEmpty()
                            .or(last_name.textProperty().isEmpty())
                            .or(email.textProperty().isEmpty())
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    protected void addNewRecord() {
        if (!isValidForm()) {
            showStatus("Invalid input.");
            return;
        }
        Person p = new Person(
                first_name.getText(),
                last_name.getText(),
                department.getText(),
                majorCombo.getValue(),
                email.getText(),
                imageURL.getText()
        );
        cnUtil.insertUser(p);
        p.setId(cnUtil.retrieveId(p));
        data.add(p);

        showStatus("User has been added.");
        clearForm();

    }

    @FXML
    private void showStatus(String message) {
        statusLabel.setText(message);
    }

    @FXML
    public void exportCSV(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Export CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fc.showSaveDialog(tv.getScene().getWindow());
        if (file != null) {
            csvService.exportToCSV(file, data);
            showStatus("Data exported.");
        }
    }
    @FXML
    public void importCSV(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Import CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fc.showOpenDialog(tv.getScene().getWindow());
        if (file != null) {
            data.addAll(csvService.importFromCSV(file));
            showStatus("Data imported.");
        }
    }

    @FXML
    public void generatePDFReport(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Generate Report");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fc.showSaveDialog(tv.getScene().getWindow());
        if (file != null) {
            pdfService.generateReport(file, data);
            showStatus("PDF Report generated.");
        }
    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        majorCombo.getSelectionModel().clearSelection();
        email.setText("");
        imageURL.setText("");
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                majorCombo.getValue(), email.getText(), imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
        showStatus("User has been updated.");
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
        statusLabel.setText("User has been deleted.");
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        majorCombo.setValue(p.getMajor());
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/darkTheme.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2, textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    private boolean isValidForm() {
        return validateName(first_name.getText())
                && validateName(last_name.getText())
                && validateDepartment(department.getText())
                && validateEmail(email.getText())
                && validateImageURL(imageURL.getText());
    }

    private boolean validateName(String name) {
        Pattern pattern = Pattern.compile("^[A-Z][a-zA-Z]{1,49}$");
        return pattern.matcher(name).matches();
    }

    private boolean validateDepartment(String department) {
        Pattern pattern = Pattern.compile("^[A-Za-z ]{2,50}$");
        return pattern.matcher(department).matches();
    }

    private boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile("(\\w+)@farmingdale\\.edu");
        return pattern.matcher(email).matches();
    }

    private boolean validateImageURL(String imageURL) {
        Pattern pattern = Pattern.compile("^(https?://).+\\.(png|jpg|gif)$)");
        return pattern.matcher(imageURL).matches();
    }


    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

}