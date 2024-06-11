/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nimesha
 */
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;

public class WhiteboardClient extends Application {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private Canvas canvas;
    private GraphicsContext gc;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        initGUI(primaryStage);
        initNetwork();
    }

    private void initGUI(Stage primaryStage) {
        primaryStage.setTitle("Collaborative Whiteboard");
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.setLineWidth(2);

        canvas.setOnMousePressed(e -> {
            gc.beginPath();
            gc.moveTo(e.getX(), e.getY());
            gc.stroke();
            sendDrawMessage(e.getX(), e.getY(), true);
        });

        canvas.setOnMouseDragged(e -> {
            gc.lineTo(e.getX(), e.getY());
            gc.stroke();
            sendDrawMessage(e.getX(), e.getY(), false);
        });

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initNetwork() {
        try {
            socket = new Socket("localhost", 12345); // Replace with your server address and port
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start a thread to listen for drawing commands from the server
            new Thread(this::handleServerMessages).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleServerMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                String[] parts = message.split(",");
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                boolean start = Boolean.parseBoolean(parts[2]);
                drawOnCanvas(x, y, start);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawOnCanvas(double x, double y, boolean start) {
        if (start) {
            gc.beginPath();
            gc.moveTo(x, y);
        } else {
            gc.lineTo(x, y);
            gc.stroke();
        }
    }

    private void sendDrawMessage(double x, double y, boolean start) {
        String message = x + "," + y + "," + start;
        out.println(message);
    }
}
