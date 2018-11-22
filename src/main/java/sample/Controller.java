package sample;

import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.*;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.Json;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;

public class Controller extends Thread {
    private boolean running = true;
    private Controller con = this;
    private Stage stage;
    private Client client;
    private WebTarget target;
    private JsonObject schoolObject;

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

    public Controller(){

        con.start();
    }

    @FXML
    private void BtSelectSchool(ActionEvent event){
        Parent root;
        //running = false;
        try {
            root = FXMLLoader.load(getClass().getResource("/schoolSearch.fxml"));

            stage = new Stage();
            stage.setTitle("Schule auswählen");
            stage.setScene(new Scene(root, 1000, 500));
            stage.show();



        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //running=true;
        //con.start();
    }

    @FXML
    public void run(){

        while (running) {

            try {
                BufferedReader br = new BufferedReader(new FileReader("src/package.json"));
                if (br.readLine() != null) {
                    try (InputStream fis = new FileInputStream("src/package.json")) {
                        //Read JSON file
                        JsonReader reader = Json.createReader(fis);

                        schoolObject = reader.readObject();

                        reader.close();
                        if(schoolLabel != null){
                            String in = schoolObject.getString("displayName");
                            if (!in.equals(schoolLabel.getText()))
                            {
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
    private void login(){

        running=false;
        selectSchoolBT.disableProperty().setValue(true);
        String username = usernameField.getText();
        String password = passwordField.getText();


        Backend.getInstance().login(username,password);



        ObservableList<String> itemList = FXCollections.observableArrayList(Backend.getInstance().getDailyHours());

        hours.setItems(itemList);

        }
    }
