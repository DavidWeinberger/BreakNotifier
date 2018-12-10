package sample;

import com.sun.javafx.binding.Logging;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.io.Console;

public class UpdateThread extends Thread {
    private String username;
    private String password;
    private Controller controllerClass;
    public boolean quiting = true;

    public UpdateThread(String usernameIn, String passwordIn, Controller controllerClassIn){
        username = usernameIn;
        password = passwordIn;
        controllerClass = controllerClassIn;

    }

    @FXML
    @Override
    public void run() {
        int count = 0;
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
        controllerClass.login();
    }
}
