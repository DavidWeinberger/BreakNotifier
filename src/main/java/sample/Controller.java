package sample;

import javafx.beans.binding.StringBinding;
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
import java.util.Map;
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
    private ListView<?> hours;
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

        while (true) {

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
        String username = usernameField.getText();
        String password = passwordField.getText();
        this.client = ClientBuilder.newClient();
        String serverName = schoolObject.getString("server");
        this.target = this.client.target("https://"+serverName+"/WebUntis/j_spring_security_check");
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("school", "htbla linz leonding");
        formData.add("j_username", username);
        formData.add("j_password", password);
        formData.add("token", "");

        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.form(formData));
        if (response.getStatus() == 200) {
            Map<String, NewCookie> map = response.getCookies();
            //System.out.println(payload);

            this.target = this.client.target("https://mese.webuntis.com/WebUntis/api/app/config");
            response = target.request(MediaType.APPLICATION_JSON).cookie(map.get("JSESSIONID")).get();
            JsonObject object = response.readEntity(JsonObject.class).getJsonObject("data").getJsonObject("loginServiceConfig").getJsonObject("user");
            //System.out.println(object);
            int id = object.getInt("personId");
            int type = object.getInt("roleId");
            //System.out.println(id);
            LocalDate date = LocalDate.now();
            String dateCode = date.getYear()+""+date.getMonthValue()+date.getDayOfMonth();
            String url = "https://mese.webuntis.com/WebUntis/api/daytimetable/dayLesson?date="+dateCode+"&id=" + String.valueOf(id) + "&type="+type;

            this.target = this.client.target(url);
            response = target.request(MediaType.APPLICATION_JSON).cookie(map.get("JSESSIONID")).get();
            String payload = response.readEntity(String.class);
            System.out.println(payload);
        }
    }




}