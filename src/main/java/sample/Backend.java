package sample;

import javafx.stage.Stage;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Backend {
    private static Backend instance = null;
    private Client client = ClientBuilder.newClient();
    private WebTarget target;
    private JsonObject schoolObject;
    private NewCookie loginCookie;
    private String serverName;
    private JsonArray schoolList;
    private boolean loggedIn = false;

    public static Backend getInstance() {
        if (instance == null)
        {
            instance = new Backend();
        }

        return instance;

    }

    private Backend(){

    }

    public void readSchoolObj(){ //Lest Schulnamen aus der Package.json Datei aus.
        try {
            BufferedReader br = new BufferedReader(new FileReader("src/package.json"));
            if (br.readLine() != null) {
                try (InputStream fis = new FileInputStream("src/package.json")) {
                    //Read JSON file
                    JsonReader reader = Json.createReader(fis);

                    schoolObject = reader.readObject();

                    reader.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean login(String username, String password){
        readSchoolObj();
        serverName = schoolObject.getString("server");
        this.target = this.client.target("https://"+serverName+"/WebUntis/j_spring_security_check"); //Setzt das WebTarget auf den Secrurity Check vom WebUntis
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("school", schoolObject.getString("loginName"));
        formData.add("j_username", username);
        formData.add("j_password", password);
        formData.add("token", "");
        try {
            Response response = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.form(formData)); //Schick Post request mit den Login Daten. Diese werden als FormData versandt

            if (response == null || response.getStatus() == 302){
                System.out.println("Failed");
                loggedIn = false;
                loginCookie = null;

            }
            else if (response.readEntity(String.class).contains("Your Browser is not supported. Please use IE10 or later!")){
                System.out.println("Failed");
                loggedIn = false;
                loginCookie = null;

            }
            else if (response.getStatus() == 200) {
            Map<String, NewCookie> map = response.getCookies();
            System.out.println("Erfolgreich");
            loginCookie = map.get("JSESSIONID"); //Speichert den Cookie vom Erfolgreichen Login
            loggedIn = true;

            }
        }catch (Exception e){
            System.out.println("Falsches Passwort");
        }
        return loggedIn;
    }

    public List<String> getDailyHours(){
        List<String> list = new LinkedList<>();
        if (loggedIn == true) {
            Response response;
            this.target = this.client.target("https://" + serverName + "/WebUntis/api/app/config");
            response = target.request(MediaType.APPLICATION_JSON).cookie(loginCookie).get();
            JsonObject object = response.readEntity(JsonObject.class).getJsonObject("data").getJsonObject("loginServiceConfig").getJsonObject("user");
            //System.out.println(object);
            int id = object.getInt("personId");
            int type = object.getInt("roleId");
            //System.out.println(id);
            LocalDate date = LocalDate.now();
            String dateCode = date.getYear() + "" + date.getMonthValue();
            if(Integer.valueOf(date.getDayOfMonth()) < 10){
                dateCode += "0" + date.getDayOfMonth();
            }
            else
            {
                dateCode += date.getDayOfMonth();
            }
            String url = "https://" + serverName + "/WebUntis/api/daytimetable/dayLesson?date=" + dateCode + "&id=" + String.valueOf(id) + "&type=" + type;
            this.target = this.client.target(url);
            response = target.request(MediaType.APPLICATION_JSON).cookie(loginCookie).get();
            JsonArray jsonArray = response.readEntity(JsonObject.class).getJsonObject("data").getJsonArray("dayTimeTable");
            List<Subject> subjectList = new LinkedList<>();


            for (int x = 0; x < jsonArray.size(); x++) {
                JsonObject jsonObject = jsonArray.getJsonObject(x);
                String subjects = jsonObject.getString("subject");
                String startTime = String.valueOf(jsonObject.getInt("startTime"));
                String subTime = startTime.substring(startTime.length() - 2);
                String subTimePart1 = startTime.substring(0, startTime.length() - 2);
                startTime = subTimePart1 + ":" + subTime;

                String endTime = String.valueOf(jsonObject.getInt("endTime"));
                subTime = endTime.substring(endTime.length() - 2);
                subTimePart1 = endTime.substring(0, endTime.length() - 2);
                endTime = subTimePart1 + ":" + subTime;

                String teacher = jsonObject.getString("teacher");
                String room = jsonObject.getString("room");


                String backColor = jsonObject.getString("backColor");
                LocalDateTime time = LocalDateTime.now();
                if (backColor.contains("#a781b5")){
                    if (jsonObject.getInt("endTime") > time.getHour()*100+time.getMinute()) {
                        subjectList.add(new Subject(subjects, startTime, endTime, "Supplierung", room, teacher));
                    }
                }
                else if(backColor.contains("#b0bc00")){
                    if (jsonObject.getInt("endTime") > time.getHour()*100+time.getMinute()) {
                        subjectList.add(new Subject(subjects, startTime, endTime, "Test oder ähnliches", room, teacher));
                    }
                }
                else if(jsonObject.getBoolean("hasExam")){
                    if (jsonObject.getInt("endTime") > time.getHour()*100+time.getMinute()) {
                        subjectList.add(new Subject(subjects, startTime, endTime, "Schularbeit/Test", room, teacher));
                    }
                }
                else{
                    if (jsonObject.getInt("endTime") > time.getHour()*100+time.getMinute()){
                        subjectList.add(new Subject(subjects, startTime, endTime, room, teacher));
                    }
                }



                //System.out.println(subjects + "  " + startTime + " " + endTime);
            }

            for (int x = 0; x < subjectList.size(); x++){
                list.add(subjectList.get(x).toString());
            }

        }
        return list;
    }

    public JsonObject getSchoolQueryResults(String input){
        this.target = this.client.target("https://mobile.webuntis.com/ms/schoolquery2");

        JsonArray values = javax.json.Json.createArrayBuilder().add(javax.json.Json.createObjectBuilder().add("search",input)).build();

        JsonObject obj = javax.json.Json.createObjectBuilder().add("id", "wu_schulsuche-1542658388792").add("jsonrpc","2.0").add("method","searchSchool").add("params", values).build();

        Response response = this.target.request(MediaType.APPLICATION_JSON).post(Entity.json(obj));
        JsonObject resultList = response.readEntity(JsonObject.class);
        return resultList;
    }

    public List<String> getFormatedSchoolResult(JsonObject resultList){
        schoolList = resultList.getJsonObject("result").getJsonArray("schools");
        List<String> list = new LinkedList<>();
        for (int i = 0; i < schoolList.size(); i++) {
            list.add(schoolList.getJsonObject(i).getString("displayName"));
        }
        return list;
    }

    public boolean setSave(String selected) {
        for (int i = 0; i < schoolList.size(); i++) {
            if (schoolList.getJsonObject(i).getString("displayName").equals(selected)) {
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("src/package.json"), "utf-8"))) {
                    writer.write(schoolList.getJsonObject(i).toString());
                    return true;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return false;
    }
}
