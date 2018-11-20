package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
import javafx.scene.control.Button;

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
        this.target = this.client.target("https://mobile.webuntis.com/ms/schoolquery2");
        String input = schoolNameField.getText();
        JsonArray values = javax.json.Json.createArrayBuilder().add(javax.json.Json.createObjectBuilder().add("search",input)).build();

        JsonObject obj = javax.json.Json.createObjectBuilder().add("id", "wu_schulsuche-1542658388792").add("jsonrpc","2.0").add("method","searchSchool").add("params", values).build();

        Response response = this.target.request(MediaType.APPLICATION_JSON).post(Entity.json(obj));
        JsonObject resultList = response.readEntity(JsonObject.class);
        if (resultList.getJsonObject("error") != null){
            errorLabel.setText("Zu viele Ergebnisse");
        }
        else {
            errorLabel.setText("");
            this.schoolList = resultList.getJsonObject("result").getJsonArray("schools");
            List<String> list = new LinkedList<>();
            for (int i = 0; i < schoolList.size(); i++) {
                list.add(schoolList.getJsonObject(i).getString("displayName"));
            }
            ObservableList<String> itemList = FXCollections.observableArrayList(list);
            listView.setItems(itemList);
            //System.out.println(schoolList.size());
        }
    }

    @FXML
    public void handleSave(ActionEvent event){
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            for (int i = 0; i < schoolList.size(); i++) {
                if (schoolList.getJsonObject(i).getString("displayName").equals(selected)) {
                    try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream("src/package.json"), "utf-8"))) {
                        writer.write(schoolList.getJsonObject(i).toString());
                        Stage stage = (Stage) saveBT.getScene().getWindow();
                        stage.close();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

}

