package org.TaskMgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import static org.TaskMgmt.service.MySQLManager.startMySQL;
import static org.TaskMgmt.service.MySQLManager.stopMySQL;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        try {
            startMySQL();

            SpringApplication app = new SpringApplication(Application.class);
            app.addListeners((ApplicationListener<ContextClosedEvent>) event -> {
                try {
                    stopMySQL();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            app.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
