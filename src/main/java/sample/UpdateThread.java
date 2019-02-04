package sample;

import com.sun.javafx.binding.Logging;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.json.simple.parser.JSONParser;

import javax.json.*;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import java.io.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
            JSONParser parser = new JSONParser();
            Object obj = null;
            try {
                obj = parser.parse(new FileReader("src/ReferenceTimetable.json"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            JSONObject object = (JSONObject) obj;
            String date = object.get("dateTime").toString();
            LocalDate localDate = LocalDate.now();
            String[] dateElements = date.split("-");
            if (localDate.getYear() == Integer.valueOf(dateElements[0])){
                if (localDate.getMonthValue() == Integer.valueOf(dateElements[1])){
                    if (localDate.minusDays(7).getDayOfMonth() <= Integer.valueOf(dateElements[2]) || localDate.minusDays(7).getMonthValue() < localDate.getMonthValue() ){
                        System.out.println("Its ok");
                    }
                    else {
                        System.out.println(localDate.minusDays(7).getDayOfMonth());
                        System.out.println("Day");
                        updateReference();
                    }
                }
                else {
                    System.out.println("Month");
                    updateReference();
                }
            }
            else {
                System.out.println("Year");
                updateReference();
            }
            
            //System.out.println("ToDo");
            
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
        LocalDateTime time;
        int dayNumber = 0;
        boolean dailyUpdate = true;
        boolean sundayUpdate = false;
        //updateReference();
        while (parent.isAlive()) {
            time = LocalDateTime.now();
            if (LocalDateTime.now().getHour() < 8)
            {
                dailyUpdate = true;
            }
            dateTime = LocalDate.now();
            if (dateTime.getDayOfWeek().getValue() == 7 && sundayUpdate == false) {
                referenceSaved = false;
                updateReference();
                System.out.println("Sunday update");
                sundayUpdate = true;

            }
            else if(dailyUpdate && dateTime.getDayOfWeek().getValue() >= 1 && dateTime.getDayOfWeek().getValue() <= 5 && time.getHour() <= 7)
            {
                System.out.println("Daily Update");
                dayNumber = dateTime.getDayOfWeek().getValue();
                dailyUpdate = false;
                Backend.getInstance().login(username, password);
                JsonArray jsonArray = Backend.getInstance().readDailyHours(0);
                System.out.println("Output:  "+ jsonArray);


                try {
                    JsonParser parser = Json.createParser(new FileReader("src/ReferenceTimetable.json"));
                    parser.next();
                    int value = dateTime.getDayOfWeek().getValue()-1;
                    JsonObject readedObject = parser.getObject();
                    JsonArray week = readedObject.getJsonArray("week");
                    JsonObject day = week.getJsonObject(value);
                    String dayImportet = day.toString();
                    String compareDay = "{\"" + value +"\":"+jsonArray + "}";
                    //System.out.println(compareDay);
                    //System.out.println( dayImportet);
                    if (compareDay.equals(dayImportet)){
                        System.out.println("Keine Abweichung");
                    }
                    else
                    {
                        System.out.println("Stundenplan hat abweichungen");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("\n\n\n");
            }
            else if (dayNumber != dateTime.getDayOfWeek().getValue()){
                dailyUpdate = true;
            }
            else if (dateTime.getDayOfWeek().getValue() != 7)
            {
                sundayUpdate = false;
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
