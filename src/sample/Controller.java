package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML private Canvas canvas;
    private double currentTick;
    private GraphicsContext gfx;
    private ParticleSystem system;
    private Timeline gameclock;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        int cw = 500, ch = 500, ps=10;
        init_canvas(cw,ch);
        init_particle_system(cw/ps);
        init_game_clock();
        init_status();
    }

    private void init_canvas(int w, int h) {
        gfx = canvas.getGraphicsContext2D();
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    private void init_particle_system(int w) {
        system = new ParticleSystem(w);
    }

    private void init_game_clock() {
        gameclock = new Timeline(new KeyFrame(Duration.seconds(1/60f), event -> {
            long t0 = System.nanoTime();

            system.run(1/60f);
            system.setYAt(1,20,20);
            system.setXAt(1,20,20);
            draw();

            currentTick = ((System.nanoTime() - t0) / 1e9);
        }));


        gameclock.setCycleCount(Animation.INDEFINITE);
        gameclock.play();
    }

    private void init_status() {

    }

    private void draw() {
        gfx.clearRect(0,0,canvas.getWidth(),canvas.getHeight());

        system.draw(gfx);

        gfx.setFill(Color.BLACK);
        gfx.fillText("FPS: " + (int) 60f / currentTick, 10, 20);
    }

    @FXML
    public void mouseClickedButtonClear() {
        system.clear();
    }

    @FXML
    public void mouseClickedCanvas(MouseEvent e) {
        int unit_x = (int) (e.getX() / 10);
        int unit_y = (int) (e.getY() / 10);
        Random rnd = new Random();

        if(e.getButton() == MouseButton.PRIMARY) {
            system.setParticleAt(1, unit_x, unit_y);
        } else if(e.getButton() == MouseButton.SECONDARY) {
            system.setYAt((rnd.nextBoolean() ? -1 : 1) * 0.7, unit_x, unit_y);
            system.setXAt((rnd.nextBoolean() ? -1 : 1) * 0.5, unit_x, unit_y);
        }
    }
}
