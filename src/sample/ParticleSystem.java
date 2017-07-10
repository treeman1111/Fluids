package sample;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ParticleSystem {
    private int width;
    private double viscosity, diffusion;
    private double[][] x_vel, y_vel;
    private double[][] part, part_tmp;

    public ParticleSystem(int w) {
        width = w;
        viscosity = 0.01;
        diffusion = 0.01;

        part = new double[w][w];
        part_tmp = new double[w][w];
        x_vel = new double[w][w];
        y_vel = new double[w][w];
    }

    int getWidth() {
        return width;
    }

    double getParticleAt(int x, int y) {
        if(x < 0 || x > width || y < 0 || y > width) throw new IllegalArgumentException();
        return part[x][y];
    }

    double getXAt(int x, int y) {
        if(x < 0 || x > width || y < 0 || y > width) throw new IllegalArgumentException();
        return x_vel[x][y];
    }

    double getYAt(int x, int y) {
        if(x < 0 || x > width || y < 0 || y > width) throw new IllegalArgumentException();
        return y_vel[x][y];
    }

    void setParticleAt(double d, int x, int y) {
        if(x < 0 || x > width || y < 0 || y > width) throw new IllegalArgumentException();
        part_tmp[x][y] = constrainBounds(d);
    }

    void setXAt(double d, int x, int y) {
        if(x < 0 || x > width || y < 0 || y > width) throw new IllegalArgumentException();
        x_vel[x][y] = constrainBounds(d);
    }

    void setYAt(double d, int x, int y) {
        if(x < 0 || x > width || y < 0 || y > width) throw new IllegalArgumentException();
        y_vel[x][y] = constrainBounds(d);
    }

    void clear() {
        part = new double[width][width];
        part_tmp = new double[width][width];
        x_vel = new double[width][width];
        //x_add = new double[width][height];
        y_vel = new double[width][width];
        //y_add = new double[width][height];
    }

    void draw(GraphicsContext gfx) {
        int w = (int) (gfx.getCanvas().getWidth() / width);

        /* draw concentrations */
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < width; y++) {
                gfx.setFill(Color.BLACK.interpolate(Color.AQUAMARINE, part[x][y]));
                gfx.fillRect(x * w, y * w, w, w);
            }
        }

        /* draw velocity vectors */
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < width; y++) {
                Point2D vec = new Point2D(x_vel[x][y], y_vel[x][y]);
                vec = vec.normalize();
                vec = vec.multiply(w / 2);

                double sx = (x * w) + (w / 2);
                double sy = (y * w) + (w / 2);
                double ex = sx + vec.getX();
                double ey = sy + vec.getY();

                gfx.setStroke(Color.RED);
                gfx.strokeLine(sx,sy,ex,ey);
            }
        }
    }

    void run(double tick) {
//        // add new densities
//        for(int i = 0; i < width; i++) {
//            for(int j = 0; j < width; j++) {
//                part[i][j] += tick * part_tmp[i][j];
//            }
//        }
//
//        // diffuse the densities
//        double a = tick * viscosity * width * width;
//
//        for(int k = 0; k < 20; k++) {
//            for(int i = 1; i < width - 1; i++) {
//                for(int j = 1; j < width - 1; j++) {
//                    part_tmp[i][j] = (part[i][j] + a*(part_tmp[i-1][j] + part_tmp[i+1][j] + part_tmp[i][j-1] + part_tmp[i][j+1])) / (1+4*a);
//                }
//            }
//        }
    }

    private double constrainBounds(double d) {
        return (d < 0 ? 0 : (d > 1 ? 1 : d));
    }

    /**
     * @param a An array of current states
     * @param a_tmp An array of values to be added to current states
     */
    private void addSource(double[][] a, double[][] a_tmp, double dt) {
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < width; j++) {
                a[i][j] += dt * a_tmp[i][j];
            }
        }
    }

    private void diffuse(int b, double[][] a, double[][] a_tmp, double dt) {
        double c = dt*width*width*diffusion;

        for(int k=0; k < 20; k++) {
            for(int i = 0; i < width; i++) {
                for(int j = 0; j < width; j++) {
                    a[i][j] = (a_tmp[i][j] + c*(a[i-1][j] + a[i+1][j] + a[i][j-1] + a[i][j+1])) / (1+4*c);
                }
            }
            setBounds(b,a);
        }
    }
    
    private void advect(int b, double[][] d, double[][] d_tmp, double[][] u, double[][] v, double dt) {

    }

    /**
     * @param b Represents the array which is being bounded: 0 for part, 1 for x_vel, 2 for y_vel
     * @param a The array being bounded
     */
    private void setBounds(int b, double[][] a) {
       for(int i = 1; i < width-1; i++) {
           a[0][i] = (b==1 ? -a[1][i] : a[1][i]);
           a[width-1][i] = (b==1 ? -a[width-2][i] : a[width-2][i]);
           a[i][0] = (b==2 ? -a[i][1] : a[i][1]);
           a[i][width-1] = (b==2 ? -a[i][width-2] : a[i][width-2]);
       }

       a[0][0] = (1/2f) * (a[0][1] + a[1][0]); /* top left is avg of adjacent corners */
       a[width-1][0] = (1/2f) * (a[width-2][0] + a[width-1][1]); /* top right corner */
       a[0][width-1] = (1/2f) * (a[0][width-2] + a[1][width-1]); /* bottom left corner */
       a[width-1][width-1] = (1/2f) * (a[width-2][width-1] + a[width-1][width-2]); /* bottom right corner */
    }
}
