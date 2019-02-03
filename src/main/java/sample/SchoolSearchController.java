package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class SchoolSearchController {
    private Client client;
    private WebTarget target;
    private JsonArray schoolList;
    @FXML
    private TextField schoolNameField;

    @FXML
    private ListView<String> listView;

    @FXML
    private Label errorLabel;

    @FXML
    private Button saveBT;


    @FXML
    public void handleInput(ActionEvent event){
        this.client = ClientBuilder.newClient();
        String input = schoolNameField.getText();
        JsonObject resultList = Backend.getInstance().getSchoolQueryResults(schoolNameField.getText());
        if (resultList.getJsonObject("error") != null){
            errorLabel.setText("Zu viele Ergebnisse");
        }
        else {
            errorLabel.setText("");

            ObservableList<String> itemList = FXCollections.observableArrayList(Backend.getInstance().getFormatedSchoolResult(resultList));
            listView.setItems(itemList);
            //System.out.println(schoolList.size());

        }
    }

    @FXML
    public void handleSave(ActionEvent event){
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (Backend.getInstance().setSave(selected)){
                Stage stage = (Stage) saveBT.getScene().getWindow();
                stage.close();
            }
        }
    }

}

