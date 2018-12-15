package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Controller extends Thread {
    private UpdateThread updateThread = null;
    private boolean running = true;
    private Controller con = this;
    private Stage stage;
    private Client client;
    private WebTarget target;
    private JsonObject schoolObject;
    private String username;
    private String password;
    //private List<String> itemList = new LinkedList<>(FXCollections.observableArrayList());
    private ObservableList<String> itemList = FXCollections.observableArrayList();

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ListView<String> hours;
    @FXML
    private Button selectSchoolBT;

    @FXML
    private Text schoolLabel;

    public Controller() {
        itemList.addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(Change<? extends String> c) {
                System.out.println("List Changed");
                hours.setItems(itemList);
            }
        });
        con.start();
    }

    @FXML
    private void BtSelectSchool(ActionEvent event) {
        Parent root;
        //running = false;
        try {
            root = FXMLLoader.load(getClass().getResource("/schoolSearch.fxml"));

            stage = new Stage();
            stage.setTitle("Schule auswählen");
            stage.setScene(new Scene(root, 1000, 500));
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }
        //running=true;
        //con.start();
    }

    @FXML
    public void run() {

        while (running) {

            try {
                BufferedReader br = new BufferedReader(new FileReader("src/package.json"));
                if (br.readLine() != null) {
                    try (InputStream fis = new FileInputStream("src/package.json")) {
                        //Read JSON file
                        JsonReader reader = Json.createReader(fis);

                        schoolObject = reader.readObject();

                        reader.close();
                        if (schoolLabel != null) {
                            String in = schoolObject.getString("displayName");
                            if (!in.equals(schoolLabel.getText())) {
                                schoolLabel.setText(in);

                            }

                        }
                        //
                        sleep(100);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    @FXML
    public void login() {
        usernameField.setEditable(false);
        passwordField.setEditable(false);

        running = false;
        selectSchoolBT.disableProperty().setValue(true);
        username = usernameField.getText();
        password = passwordField.getText();

        if (updateThread == null && Backend.getInstance().login(username, password))
        {
            updateThread = new UpdateThread(username, password, con, Thread.currentThread());
            updateThread.start();
        }

        else {
            usernameField.setEditable(true);
            passwordField.setEditable(true);
        }






        itemList = FXCollections.observableArrayList(Backend.getInstance().getDailyHours());

        changeSubjectList(itemList);

    }

    @FXML
    public void changeSubjectList(ObservableList<String> _itemList){
        //hours = new ListView<>();
        itemList = _itemList;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                hours.setItems(itemList);
            }
        });
        //System.out.println("After");
    }

}
