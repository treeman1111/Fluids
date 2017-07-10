package sample;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class ParticleSystem {
    private int width;
    private double viscosity, diffusion;
    private double[][] x_vel, y_vel, x_vel_tmp, y_vel_tmp;
    private double[][] part, part_tmp;

    public ParticleSystem(int w) {
        width = w+2;
        viscosity = 0.1;
        diffusion = 0.01;

        part = new double[width][width];
        part_tmp = new double[width][width];
        x_vel = new double[width][width];
        y_vel = new double[width][width];
        x_vel_tmp = new double[width][width];
        y_vel_tmp = new double[width][width];
    }

    int getWidth() {
        return width-2;
    }

    double getParticleAt(int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= width) return -1;
        return part[x+1][y+1];
    }

    double getXAt(int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= width) return 0;
        return x_vel[x+1][y+1];
    }

    double getYAt(int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= width) return 0;
        return y_vel[x+1][y+1];
    }

    void setParticleAt(double d, int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= width) return;
        part_tmp[x+1][y+1] = constrainDensity(d);
    }

    void setXAt(double d, int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= width) return;
        x_vel[x+1][y+1] = constrainVelocity(d);
    }

    void setYAt(double d, int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= width) return;
        y_vel[x+1][y+1] = constrainVelocity(d);
    }

    void clear() {
        part = new double[width][width];
        part_tmp = new double[width][width];
        x_vel = new double[width][width];
        x_vel_tmp = new double[width][width];
        y_vel = new double[width][width];
        y_vel_tmp = new double[width][width];
    }

    void draw(GraphicsContext gfx) {
        int w = (int) (gfx.getCanvas().getWidth() / getWidth());

        /* draw concentrations */
        for(int x = 1; x < width-1; x++) {
            for(int y = 1; y < width-1; y++) {
                gfx.setFill(Color.BLACK.interpolate(Color.AQUAMARINE, part[x][y]));
                gfx.fillRect((x-1) * w, (y-1) * w, w, w);
            }
        }

        /* draw velocity vectors */
        for(int x = 1; x < width-1; x++) {
            for(int y = 1; y < width-1; y++) {
                Point2D vec = new Point2D(x_vel[x][y], y_vel[x][y]);
//                vec = vec.normalize();
//                vec = vec.multiply(w / 2.0);

                double sx = ((x-1) * w) + (w / 2);
                double sy = ((y-1) * w) + (w / 2);
                double ex = sx + vec.getX();
                double ey = sy + vec.getY();

                gfx.setStroke(Color.RED);
                gfx.strokeLine(sx,sy,ex,ey);
            }
        }
    }

    void run(double tick) {
        stepVelocity(tick);
        stepDensity(tick);
        System.out.format("(%f,%f)\n", x_vel[1][1], y_vel[1][1]);

        for(int i = 0; i < width; i++) {
            for(int j = 0; j < width; j++) {
                part_tmp[i][j] = 0;
                x_vel_tmp[i][j] = 0;
                y_vel_tmp[i][j] = 0;
            }
        }
    }


    private void stepVelocity(double tick) {
        addSource(x_vel, x_vel_tmp, tick);
        addSource(y_vel, y_vel_tmp, tick);

        diffuse(1, x_vel_tmp, x_vel, viscosity, tick);
        diffuse(2, y_vel_tmp, y_vel, viscosity, tick);
        project(x_vel_tmp, y_vel_tmp, x_vel, y_vel);

        advect(1, x_vel, x_vel_tmp, x_vel_tmp, y_vel_tmp, tick);
        advect(2, y_vel, y_vel_tmp, x_vel_tmp, y_vel_tmp, tick);
        project(x_vel, y_vel, x_vel_tmp, y_vel_tmp);
    }

    private void stepDensity(double tick) {
        addSource(part, part_tmp, tick);
        diffuse(0, part_tmp, part, diffusion, tick);
        advect(0, part, part_tmp, x_vel, y_vel, tick);
    }

    private void addSource(double[][] a, double[][] a_tmp, double dt) {
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < width; j++) {
                a[i][j] += (dt * a_tmp[i][j]);
            }
        }
    }

    private void diffuse(int b, double[][] a, double[][] a_tmp, double diff, double dt) {
        double c = dt*width*diff;

        for(int k=0; k < 20; k++) {
            for(int i = 1; i < width-1; i++) {
                for(int j = 1; j < width-1; j++) {
                    a[i][j] = (a_tmp[i][j] + c*(a[i-1][j] + a[i+1][j] + a[i][j-1] + a[i][j+1])) / (1+4*c);
                }
            }
            setBounds(b,a);
        }
    }

    private void advect(int b, double[][] d, double[][] d_tmp, double[][] u, double[][] v, double dt) {
        for (int i = 1; i < width - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                double x = i - (dt * u[i][j]); // x origin as double
                double y = j - (dt * v[i][j]); // y origin as double

                if(x < 0.5) x = 0.5;
                if(x > width - 1.5) x = width - 1.5;
                int i0 = (int)x; // the x coordinate of the originating cell
                int i1 = i0+1; // x_orig + 1

                if(y < 0.5) y = 0.5;
                if(y > width - 1.5) y = width - 1.5;
                int j0 = (int)y; // the y coordinate of the originating cell
                int j1 = j0+1; // y_orig + 1

                double s1 = x - i0; // the length of the unit square between x and i0
                double s0 = 1 - s1; // the length of the unit square not between x and i0
                double t1 = y - j0; // the length of the unit square between y and j0
                double t0 = 1 - t1; // the length of the unit square not between y and j0

                d[i][j] = s0 * ((t0 * d_tmp[i0][j0]) + (t1 * d_tmp[i0][j1])) +
                          s1 * ((t0 * d_tmp[i1][j0]) + (t1 * d_tmp[i1][j1]));

            }
        }
    }

    private void project(double[][] x_vel, double[][] y_vel, double[][] p, double[][] div) {
        double h = 1.0/width;

        for(int i = 1; i < width-1; i++) {
            for(int j = 1; j < width-1; j++) {
                div[i][j] = -0.5*h*(x_vel[i+1][j] - x_vel[i-1][j] + y_vel[i][j+1] - y_vel[i][j-1]);
                p[i][j] = 0;
            }
        }
        setBounds(0,div);
        setBounds(0,p);

        for(int k = 0 ; k < 20; k++) {
            for(int i = 1; i < width - 1; i++) {
                for(int j = 1; j < width - 1; j++) {
                    p[i][j] = (div[i][j] + p[i-1][j] + p[i+1][j] + p[i][j-1] + p[i][j+1]) / 4f;
                }
            }
            setBounds(0,p);
        }

        for(int i = 1; i < width-1; i++) {
            for(int j = 1; j < width-1; j++) {
                x_vel[i][j] -= 0.5*(p[i+1][j] - p[i-1][j])/h;
                y_vel[i][j] -= 0.5*(p[i][j+1] - p[i][j-1])/h;
            }
        }
        setBounds(1,x_vel);
        setBounds(2,y_vel);
    }

    /**
     * @param b Represents the array which is being bounded: 0 for part, 1 for x_vel, 2 for y_vel
     * @param a The array being bounded
     */
    private void setBounds(int b, double[][] a) {
       for(int i = 1; i < width-1; i++) {
           /* when x_vel is being bounded, set x_vels at vertical columns to oppose neighboring velocities */
           a[0][i] = (b==1 ? -a[1][i] : a[1][i]);
           a[width-1][i] = (b==1 ? -a[width-2][i] : a[width-2][i]);

           /* when y_vel is being bounded, set y_vels to be negative of those adjacent to prevent escape from 'box' */
           a[i][0] = (b==2 ? -a[i][1] : a[i][1]);
           a[i][width-1] = (b==2 ? -a[i][width-2] : a[i][width-2]);
       }

       /* all of the corner values are obtained by averaging the two adjacent corners' values */
       a[0][0] = (1/2f) * (a[0][1] + a[1][0]); /* top left corner */
       a[width-1][0] = (1/2f) * (a[width-2][0] + a[width-1][1]); /* top right corner */
       a[0][width-1] = (1/2f) * (a[0][width-2] + a[1][width-1]); /* bottom left corner */
       a[width-1][width-1] = (1/2f) * (a[width-2][width-1] + a[width-1][width-2]); /* bottom right corner */
    }

    private double constrainDensity(double d) {
        if(d < 0) d = 0;
        if(d > 1) d = 1;
        return d;
    }

    private double constrainVelocity(double d) {
        if(d < -1) d = -1;
        if(d > 1) d = 1;
        return d;
    }
}
