package sample;

import com.sun.javafx.binding.Logging;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import org.omg.PortableServer.THREAD_POLICY_ID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.io.*;

import java.time.LocalDate;
import java.time.format.TextStyle;

public class UpdateThread extends Thread {
    private String username;
    private String password;
    private Controller controllerClass;
    public boolean quiting = true;
    private boolean referenceSaved = false;
    private Thread parent;

    public UpdateThread(String usernameIn, String passwordIn, Controller controllerClassIn, Thread parentIn){
        username = usernameIn;
        password = passwordIn;
        controllerClass = controllerClassIn;
        parent = parentIn;
        //checkReference();
    }

    private void checkReference(){
        File tmpDir = new File("src/ReferenceTimetable.json");
        boolean exists = tmpDir.exists();
        if (!exists)
        {
            referenceSaved=false;
            System.out.println("Not Existing");
            try {
                tmpDir.createNewFile();
                updateReference();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("ToDo");
            
        }
        //updateReference();
    }

    public void updateReference() {

        //controllerClass.login();
        LocalDate date = LocalDate.now();
        if (date.getDayOfWeek().getValue() != 7){
            date = date.minusDays(date.getDayOfWeek().getValue());
        }

        JsonObject jsonObject;
        if (!referenceSaved) {
            int dateCode = date.getDayOfMonth();
            System.out.println(dateCode);
            jsonObject = Json.createObjectBuilder().add("dateTime", date.toString()).add("week",
                    Json.createArrayBuilder()
                            .add(Json.createObjectBuilder().add("0", Backend.getInstance().readDailyHours(dateCode+1)))
                            .add(Json.createObjectBuilder().add("1", Backend.getInstance().readDailyHours(dateCode+2)))
                            .add(Json.createObjectBuilder().add("2", Backend.getInstance().readDailyHours(dateCode+3)))
                            .add(Json.createObjectBuilder().add("3", Backend.getInstance().readDailyHours(dateCode+4)))
                            .add(Json.createObjectBuilder().add("4", Backend.getInstance().readDailyHours(dateCode+5)))
                            .build()).build();
            try (FileWriter fileWriter = new FileWriter("src/ReferenceTimetable.json")) {
                fileWriter.write(jsonObject.toString());
                System.out.println("Successfully Copied JSON Object to File...");
                System.out.println("\nJSON Object: " + jsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
            referenceSaved = true;
        }
    }

    @FXML
    @Override
    public void run() {
        checkReference();
        LocalDate dateTime;
        boolean sundayUpdate = false;
        //updateReference();
        while (parent.isAlive()) {
            dateTime = LocalDate.now();
            if (dateTime.getDayOfWeek().getValue() == 7 && sundayUpdate == false) {
                updateReference();
                System.out.println("Sunday update");
                sundayUpdate = true;

            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        /*int count = 0;
        while (quiting){
            if (count%10 == 0){
                for(int i = 0; i < 5; i++){
                    quiting = Backend.getInstance().login(username, password);
                    if (quiting){
                        //System.out.println(count);
                        break;
                    }
                }

            }
            else {

                try {

                    ObservableList<String> itemList = FXCollections.observableArrayList(Backend.getInstance().getDailyHours());

                    controllerClass.changeSubjectList(itemList);

                }catch (IllegalStateException ex){
                    System.err.println(ex + "Test");
                }

                try {

                    sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            count++;
        }
        controllerClass.login();*/
    }
}
