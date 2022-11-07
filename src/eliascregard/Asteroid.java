package eliascregard;

import java.awt.*;

public class Asteroid {

    Vector2D[] points;
    Vector2D position;
    Vector2D velocity;
    public int generation;

    public Asteroid(Vector2D position, Vector2D[] points, int generation) {
        this.position = position;
        this.velocity = new Vector2D(0, 0);
        this.points = points;
        this.generation = generation;
    }
    public Asteroid(Vector2D position, Vector2D[] points) {
        this(position, points, 1);
    }

    public void update(double deltaTime) {
        this.position.add(this.velocity, deltaTime);
    }

    public static Asteroid randomAsteroid(Vector2D position, Vector2D size) {
        int n = (int) (Math.random() * 6) + 6;
        Vector2D[] points = new Vector2D[n];
        double[] angles = new double[n];
        for (int i = 0; i < n; i++) {
            points[i] = new Vector2D(Math.random() * size.x, Math.random() * size.y);
        }
        double x = 0;
        double y = 0;
        Vector2D center;
        for (int i = 0; i < n; i++) {
            x += points[i].x;
            y += points[i].y;
        }
        center = new Vector2D(x / n, y / n);
        for (int i = 0; i < n; i++) {
            angles[i] = Math.atan2(points[i].y - center.y, points[i].x - center.x);
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n-1; j++) {
                if (angles[j] > angles[j + 1]) {
                    double tempA = angles[j];
                    Vector2D tempP = points[j];
                    angles[j] = angles[j + 1];
                    points[j] = points[j + 1];
                    angles[j + 1] = tempA;
                    points[j + 1] = tempP;
                }
            }
        }
        return new Asteroid(position, points);
    }

    public Asteroid resize(double factor) {
        Vector2D[] newPoints = new Vector2D[this.points.length];
        Vector2D center = new Vector2D(0, 0);
        for (Vector2D point : this.points) {
            center.add(point);
        }
        center.scale(1.0 / this.points.length);
        for (int i = 0; i < this.points.length; i++) {
            double angle = Math.atan2(this.points[i].y - center.y, this.points[i].x - center.x);
            double distance = this.points[i].distance(center);
            newPoints[i] = new Vector2D(Math.cos(angle) * distance * factor, Math.sin(angle) * distance * factor);
        }
        return new Asteroid(this.position.makeCopy(), newPoints, this.generation);
    }
    public Asteroid[] split() {
        Asteroid[] asteroids = new Asteroid[2];
        for (int i = 0; i < 2; i++) {
            asteroids[i] = randomAsteroid(
                    this.position.makeCopy(),
                    new Vector2D(100 * Math.pow(0.75, this.generation), 100 * Math.pow(0.75, this.generation))
            );
            asteroids[i].generation = this.generation + 1;
        }
        double parentDirection = Math.atan2(this.velocity.y, this.velocity.x);
        double parentSpeed = this.velocity.length();
        double child1Direction = parentDirection + Math.PI / 2;
        double child2Direction = parentDirection - Math.PI / 2;
        asteroids[0].velocity = new Vector2D(
                Math.cos(child1Direction) * parentSpeed * 0.5, Math.sin(child1Direction) * parentSpeed * 0.5);
        asteroids[1].velocity = new Vector2D(
                Math.cos(child2Direction) * parentSpeed, Math.sin(child2Direction) * parentSpeed);
        return asteroids;
    }

    public void draw(Graphics2D g2, double scale) {
        g2.setColor(new Color(255, 255, 255));
        if (this.points.length >= 3) {
            Polygon polygon = new Polygon();
            for (Vector2D point : this.points) {
                polygon.addPoint((int) ((this.position.x + point.x) * scale), (int) ((this.position.y + point.y) * scale));
            }
            g2.setStroke(new BasicStroke(1));
            g2.setColor(new Color(255, 255, 255));
            g2.drawPolygon(polygon);
        }
    }
}
