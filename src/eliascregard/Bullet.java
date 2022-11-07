package eliascregard;

import java.awt.*;

public class Bullet {
    Vector2D position;
    Vector2D velocity;
    double radius;

    public Bullet(Vector2D position, Vector2D velocity, double radius) {
        this.position = position;
        this.velocity = velocity;
        this.radius = radius;
    }

    public void updatePosition(double deltaTime) {
        this.position.add(this.velocity, deltaTime);
    }

    public boolean hitAsteroid(Asteroid asteroid) {
        int intersections = 0;
        int n = asteroid.points.length;
        Line line1 = new Line(
                this.position,
                new Vector2D(this.position.x + 999999, this.position.y)
        );
        for (int i = 0; i < n; i++) {
            Line line2 = new Line(
                    asteroid.points[i].add(asteroid.position),
                    asteroid.points[(i + 1) % n].add(asteroid.position)
            );
            if (line1.lineLineIntersection(line2) != null) {
                intersections++;
            }
        }
        return intersections % 2 == 1;
    }

    public static Bullet[] removeBulletByIndex(Bullet[] bullets, int index) {
        if (index < 0 || index >= bullets.length) {
            return bullets;
        }
        Bullet[] newBullets = new Bullet[bullets.length - 1];
        for (int i = 0; i < index; i++) {
            newBullets[i] = bullets[i];
        }
        for (int i = index + 1; i < bullets.length; i++) {
            newBullets[i - 1] = bullets[i];
        }
        return newBullets;
    }
    public static Bullet[] removeBullet(Bullet[] bullets, Bullet bullet) {
        for (int i = 0; i < bullets.length; i++) {
            if (bullets[i] == bullet) {
                return Bullet.removeBulletByIndex(bullets, i);
            }
        }
        return bullets;
    }

    public boolean outOfBounds(Dimension screenSize) {
        return this.position.x + this.radius < 0 || this.position.x - this.radius > screenSize.width || this.position.y + this.radius < 0 || this.position.y - this.radius > screenSize.height;
    }

    public void draw(Graphics2D g2, double scale) {
        g2.setColor(new Color(255, 255, 255));
        g2.fillOval((int) (this.position.x * scale), (int) (this.position.y * scale),
                    (int) (2 * this.radius * scale), (int) (2 * this.radius * scale));
//        g2.drawLine((int) (this.position.x * scale), (int) (this.position.y * scale),
//                    (int) ((this.position.x + 999999) * scale), (int) ((this.position.y) * scale));
    }
}
