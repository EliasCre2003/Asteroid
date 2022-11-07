package eliascregard;

import java.awt.*;
import java.util.Arrays;

public class Spaceship {
    public Vector2D position;
    public Vector2D velocity;
    public double angle;
    public Vector2D[] points;
    public Bullet[] bullets;
    public boolean vulnerable;
    public long invulnerableStartTime;
    public double invulnerabilityDuration;
    public long timeAtBlink;
    public boolean visible;
    public Spaceship(Vector2D position, Vector2D[] points, double invulnerabilityDuration) {
        this.position = position;
        this.velocity = new Vector2D(0, 0);
        this.angle = 3 * Math.PI / 2;
        this.points = points;
        this.bullets = new Bullet[0];
        this.vulnerable = true;
        this.invulnerableStartTime = 0;
        this.invulnerabilityDuration = invulnerabilityDuration;
        this.timeAtBlink = 0;
        this.visible = true;
    }
    public Spaceship(Vector2D position, Vector2D[] points) {
        this(position, points, 2);
    }

    public void update(double deltaTime) {
        this.position.add(this.velocity, deltaTime);
        this.updateInvulnerability();
        this.updateBlink();
        for (Bullet bullet : this.bullets) {
            bullet.updatePosition(deltaTime);
        }
    }
    public void accelerate(double acceleration, double deltaTime) {
        Vector2D accelerationVector = new Vector2D(
                Math.sin(this.angle) * acceleration,
                Math.cos(this.angle) * -acceleration
        );
        this.velocity.add(accelerationVector, deltaTime);
    }
    public void turn(double angularVelocity, double deltaTime) {
        this.angle += angularVelocity * deltaTime;
    }

    public void updateInvulnerability() {
        if (this.vulnerable) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.invulnerableStartTime >= this.invulnerabilityDuration * 1000) {
            this.vulnerable = true;
        }
    }

    public Vector2D[] getPoints(double angle) {
        Vector2D[] points = new Vector2D[this.points.length];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Vector2D(
                    this.points[i].x * Math.cos(angle) - this.points[i].y * Math.sin(angle),
                    this.points[i].x * Math.sin(angle) + this.points[i].y * Math.cos(angle)
            );
        }
        return points;
    }
    public Vector2D[] getPoints() {
        return this.getPoints(this.angle);
    }

    public static Line[] pointsToLines(Vector2D[] points) {
        Line[] lines = new Line[points.length];
        for (int i = 0; i < points.length-1; i++) {
            lines[i] = new Line(points[i].makeCopy(), points[i + 1].makeCopy());
        }
        lines[points.length - 1] = new Line(points[points.length - 1].makeCopy(), points[0].makeCopy());
        return lines;
    }
    public Line[] pointsToLines() {
        return Spaceship.pointsToLines(this.points);
    }

    public boolean checkCollision(Asteroid asteroid) {
        Vector2D[] spaceshipPoints = new Vector2D[this.points.length];
        Vector2D[] spaceshipUnmodifiedPoints = this.getPoints();
        for (int i = 0; i < spaceshipPoints.length; i++) {
            Vector2D point = spaceshipUnmodifiedPoints[i];
            spaceshipPoints[i] = point.add(this.position.makeCopy());
        }
        Vector2D[] asteroidPoints = new Vector2D[asteroid.points.length];
        for (int i = 0; i < asteroidPoints.length; i++) {
            Vector2D point = asteroid.points[i].makeCopy();
            asteroidPoints[i] = point.add(asteroid.position.makeCopy());
        }
        Line[] spaceshipLines = Spaceship.pointsToLines(spaceshipPoints);
        Line[] asteroidLines = Spaceship.pointsToLines(asteroidPoints);
        for (Line spaceshipLine : spaceshipLines) {
            for (Line asteroidLine : asteroidLines) {
                if (asteroidLine.lineLineIntersection(spaceshipLine) != null) {
                    System.out.println(spaceshipLine.lineLineIntersection(asteroidLine));
                    return true;
                }
            }
        }
        return false;
    }

    public void shootBullet(Vector2D position, double speed, double direction) {
        Vector2D velocity = new Vector2D(Math.cos(direction - Math.PI / 2) * speed, Math.sin(direction - Math.PI / 2) * speed);
        Bullet bullet = new Bullet(position.makeCopy(), velocity, 4);
        this.bullets = Arrays.copyOf(this.bullets, this.bullets.length + 1);
        this.bullets[this.bullets.length - 1] = bullet;
    }

    public void handleInputs(KeyHandler keyH, double deltaTime) {
        if (keyH.upPressed) {
            this.accelerate(500, deltaTime);
        }
        if (keyH.downPressed) {
            this.accelerate(-500, deltaTime);
        }
        if (keyH.leftPressed) {
            this.turn(-Math.PI, deltaTime);
        }
        if (keyH.rightPressed) {
            this.turn(Math.PI, deltaTime);
        }
        if (keyH.spacePressed) {
            keyH.spacePressed = false;
            if (this.vulnerable) {
                this.shootBullet(this.position, 2000, this.angle);
            }
        }
    }


    public void updateBlink() {
        if (this.vulnerable) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.timeAtBlink >= 100) {
            this.visible = !this.visible;
            this.timeAtBlink = currentTime;
        }
    }
    public void draw(Graphics2D g2, double scale) {
        Vector2D[] thisPoints = this.getPoints();
        if (thisPoints.length >= 3 && this.visible) {
            Polygon polygon = new Polygon();
            for (Vector2D point : thisPoints) {
                polygon.addPoint((int) ((this.position.x + point.x) * scale), (int) ((this.position.y + point.y) * scale));
            }
            g2.setColor(new Color(255, 255, 255));
            g2.setStroke(new BasicStroke(2));
            g2.drawPolygon(polygon);
            for (Bullet bullet : this.bullets) {
                bullet.draw(g2, scale);
            }
        }
    }
}