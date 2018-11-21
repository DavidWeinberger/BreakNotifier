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

    public static Backend getInstance() {
        if (instance == null)
        {
            instance = new Backend();
        }

        return instance;

    }

    private Backend(){

    }

    public void readSchoolObj(){
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

    public void login(String username, String password){
        readSchoolObj();
        serverName = schoolObject.getString("server");
        this.target = this.client.target("https://"+serverName+"/WebUntis/j_spring_security_check");
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("school", schoolObject.getString("loginName"));
        formData.add("j_username", username);
        formData.add("j_password", password);
        formData.add("token", "");
        try {
            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.form(formData));
            if (response == null || response.getStatus() == 302){
                System.out.println("Failed");
            }
            else if (response.readEntity(String.class).contains("Your Browser is not supported. Please use IE10 or later!")){
                System.out.println("Failed");
            }
            else if (response.getStatus() == 200) {
            Map<String, NewCookie> map = response.getCookies();
            System.out.println("Erfolgreich");
            loginCookie = map.get("JSESSIONID");
            }
        }catch (Exception e){
            System.out.println("Falsches Passwort");
        }
    }

    public List<String> getDailyHours(){
        Response response;
        this.target = this.client.target("https://"+serverName+"/WebUntis/api/app/config");
        response = target.request(MediaType.APPLICATION_JSON).cookie(loginCookie).get();
        JsonObject object = response.readEntity(JsonObject.class).getJsonObject("data").getJsonObject("loginServiceConfig").getJsonObject("user");
        //System.out.println(object);
        int id = object.getInt("personId");
        int type = object.getInt("roleId");
        //System.out.println(id);
        LocalDate date = LocalDate.now();
        String dateCode = date.getYear()+""+date.getMonthValue()+date.getDayOfMonth();
        String url = "https://"+serverName+"/WebUntis/api/daytimetable/dayLesson?date="+dateCode+"&id=" + String.valueOf(id) + "&type="+type;

        this.target = this.client.target(url);
        response = target.request(MediaType.APPLICATION_JSON).cookie(loginCookie).get();
        JsonArray jsonArray = response.readEntity(JsonObject.class).getJsonObject("data").getJsonArray("dayTimeTable");

        List<String> list = new LinkedList<>();

        for(int x = 0; x < jsonArray.size(); x++){
            JsonObject jsonObject = jsonArray.getJsonObject(x);
            String subjects = jsonObject.getString("subject");
            String startTime = String.valueOf(jsonObject.getInt("startTime"));
            String subTime = startTime.substring(startTime.length()-2);
            String subTimePart1 = startTime.substring(0, startTime.length()-2);
            startTime = subTimePart1+":"+subTime;

            String endTime = String.valueOf(jsonObject.getInt("endTime"));
            subTime = endTime.substring(endTime.length()-2);
            subTimePart1 = endTime.substring(0, endTime.length()-2);
            endTime = subTimePart1+":"+subTime;


            list.add(subjects + "  " + startTime + "-" + endTime);

            //System.out.println(subjects + "  " + startTime + " " + endTime);
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
