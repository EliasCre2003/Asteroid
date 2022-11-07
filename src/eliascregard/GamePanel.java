package eliascregard;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;


public class GamePanel extends JPanel implements Runnable {
    final Dimension SCREEN_SIZE = new Dimension(1600, 900);
    final Dimension DEFAULT_SCREEN_SIZE = new Dimension(1920, 1080);
    final double SCREEN_SCALE = (double) SCREEN_SIZE.width / DEFAULT_SCREEN_SIZE.width;
    short MAX_FRAME_RATE = 0;
    long timeAtStart = System.nanoTime();
    public long timeSinceStart() {
        return System.nanoTime() - timeAtStart;
    }
    public Thread gameThread;
    GameTime time = new GameTime();
    KeyHandler keyH = new KeyHandler();
    Vector2D mousePos = new Vector2D(0, 0);
    double deltaT;
    public int updateFrequency;
    double renderDeltaT = 0;
    public int fps;
    final Line[] SCREEN_EDGES = new Line[] {
            new Line(new Vector2D(0, 0), new Vector2D(DEFAULT_SCREEN_SIZE.width, 0)),
            new Line(new Vector2D(DEFAULT_SCREEN_SIZE.width, 0), new Vector2D(DEFAULT_SCREEN_SIZE.width, DEFAULT_SCREEN_SIZE.height)),
            new Line(new Vector2D(DEFAULT_SCREEN_SIZE.width, DEFAULT_SCREEN_SIZE.height), new Vector2D(0, DEFAULT_SCREEN_SIZE.height)),
            new Line(new Vector2D(0, DEFAULT_SCREEN_SIZE.height), new Vector2D(0, 0))
    };

    int playerPoints = 0;
    int targetAsteroidCount = 20;

    public boolean linePoint(Vector2D lineStart, Vector2D lineEnd, Vector2D point) {
        double d1 = Vector2D.distance(point, lineStart);
        double d2 = Vector2D.distance(point, lineEnd);
        double lineLength = Vector2D.distance(lineStart, lineEnd);
        double buffer = 0.5;
        return d1 + d2 >= lineLength - buffer && d1 + d2 <= lineLength + buffer;
    }
    public boolean linePoint(Line line, Vector2D point) {
        return linePoint(line.point1, line.point2, point);
    }

    public static double reflectionAngle(double angle, double normal) {
        return 2 * normal - angle;
    }

    public double[] sortArray(double[] array) {
        double[] sortedArray = Arrays.copyOf(array, array.length);
        for (int i = 0; i < sortedArray.length; i++) {
            for (int j = 0; j < sortedArray.length - 1; j++) {
                if (sortedArray[j] > sortedArray[j + 1]) {
                    double temp = sortedArray[j];
                    sortedArray[j] = sortedArray[j + 1];
                    sortedArray[j + 1] = temp;
                }
            }
        }
        return sortedArray;
    }

    public Vector2D getPolygonCenter(Polygon polygon) {
        double centerX = 0;
        double centerY = 0;
        for ( int i = 0; i < polygon.npoints; i++ ) {
            centerX += polygon.xpoints[i];
            centerY += polygon.ypoints[i];
        }
        return new Vector2D(centerX / polygon.npoints, centerY / polygon.npoints);
    }

    public Asteroid randomlySpawnAsteroid() {
        Vector2D position;
        Vector2D velocity;
        byte side = (byte) (Math.random() * 4);
        if (side == 0) {
            position = new Vector2D(-200 + Math.random() * (DEFAULT_SCREEN_SIZE.width + 200), -100);
            velocity = new Vector2D(-100 + Math.random() * 200, 20 + Math.random() * 100);
        } else if (side == 1) {
            position = new Vector2D(DEFAULT_SCREEN_SIZE.width + 100,
                    -100 + Math.random() * (DEFAULT_SCREEN_SIZE.height + 200));
            velocity = new Vector2D(-100 - Math.random() * 200, 20 + Math.random() * 100);
        } else if (side == 2) {
            position = new Vector2D(-200 + Math.random() * (DEFAULT_SCREEN_SIZE.width + 200),
                    DEFAULT_SCREEN_SIZE.height + 100);
            velocity = new Vector2D(-100 + Math.random() * 200, -100 - Math.random() * 200);
        } else {
            position = new Vector2D(-100, -100 + Math.random() * (DEFAULT_SCREEN_SIZE.height + 200));
            velocity = new Vector2D(20 + Math.random() * 100, -100 - Math.random() * 200);
        }
        Asteroid newAsteroid = Asteroid.randomAsteroid(position, new Vector2D(100,100));
        newAsteroid.velocity = velocity;
        return newAsteroid;
    }

    public void addAsteroid(Asteroid asteroid) {
        Asteroid[] newArray = Arrays.copyOf(asteroids, asteroids.length + 1);
        newArray[newArray.length - 1] = asteroid;
        asteroids = newArray;
    }

    public void removeAsteroidByIndex(int index) {
        if (index < 0 || index >= asteroids.length) {
            return;
        }
        Asteroid[] newArray = new Asteroid[asteroids.length - 1];
        for (int i = 0; i < index; i++) {
            newArray[i] = asteroids[i];
        }
        for (int i = index; i < newArray.length; i++) {
            newArray[i] = asteroids[i + 1];
        }
        asteroids = newArray;
    }
    public void removeAsteroid(Asteroid asteroid) {
        for (int i = 0; i < asteroids.length; i++) {
            if (asteroids[i] == asteroid) {
                removeAsteroidByIndex(i);
                return;
            }
        }
    }

    public void removeIfOutside(Asteroid asteroid) {
        for (Vector2D polygonPoint : asteroid.points) {
            Vector2D point = polygonPoint.add(asteroid.position);
            if (point.x > 0 && point.x < DEFAULT_SCREEN_SIZE.width && point.y > 0 && point.y < DEFAULT_SCREEN_SIZE.height) {
                return;
            }
            double direction = Math.atan2(asteroid.velocity.y, asteroid.velocity.x);
            Line velocityRay = new Line(point, new Vector2D(point.x + Math.cos(direction) * 9999, point.y + Math.sin(direction) * 9999));

            for (Line edge : SCREEN_EDGES) {
                Vector2D intersection = velocityRay.lineLineIntersection(edge);
                if (intersection != null) {
                    return;
                }
            }
        }
        removeAsteroid(asteroid);
        if (asteroids.length < targetAsteroidCount) {
            addAsteroid(randomlySpawnAsteroid());
        }
    }

    Spaceship spaceship = new Spaceship(
            new Vector2D((double) DEFAULT_SCREEN_SIZE.width / 2, (double) DEFAULT_SCREEN_SIZE.height / 2),
            new Vector2D[] {
                    new Vector2D(0, -50),
                    new Vector2D(25, 50),
                    new Vector2D(0, 25),
                    new Vector2D(-25, 50)
            }
    );

    Asteroid asteroid = Asteroid.randomAsteroid(new Vector2D(0,0), new Vector2D(100,100));

    Asteroid[] asteroids = new Asteroid[0];

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    public GamePanel() throws IOException {
        this.setPreferredSize(SCREEN_SIZE);
        this.setBackground(new Color(0, 0, 0));
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
//        this.addMouseListener(mouseH);
        this.setFocusable(true);
    }

    @Override
    public void run() {

        for (int i = 0; i < targetAsteroidCount; i++) {
            addAsteroid(randomlySpawnAsteroid());
        }

        while (gameThread != null) {
            deltaT = time.getDeltaTime();
            updateFrequency = time.getFPS(deltaT);
            renderDeltaT += deltaT;
            fps = updateFrequency;
            if (fps > MAX_FRAME_RATE && MAX_FRAME_RATE > 0) {
                fps = MAX_FRAME_RATE;
            }

            if (keyH.escapePressed) {
                System.exit(0);
            }

            update();
            if (MAX_FRAME_RATE > 0) {
                if (renderDeltaT >= 1.0 / MAX_FRAME_RATE) {
                    repaint();
                    renderDeltaT -= 1.0 / MAX_FRAME_RATE;
                }
            }
            else {
                repaint();
            }
        }
    }

    public void update() {
        mousePos.set(
                (double) MouseInfo.getPointerInfo().getLocation().x / SCREEN_SCALE,
                (double) MouseInfo.getPointerInfo().getLocation().y / SCREEN_SCALE
        );

        if (keyH.enterPressed) {
            keyH.enterPressed = false;
            addAsteroid(randomlySpawnAsteroid());
        }

        if (spaceship.checkCollision(asteroid)) {
            System.out.println("Collision!");
        }

        for (Asteroid asteroid : asteroids) {
            removeIfOutside(asteroid);
//            if (spaceship.checkCollision(asteroid) && spaceship.vulnerable) {
//                spaceship.vulnerable = false;
//                spaceship.invulnerableStartTime = System.currentTimeMillis();
//                System.out.println("Collision!");
//            }
            for (Bullet bullet : spaceship.bullets) {
                if (!bullet.hitAsteroid(asteroid)) {
                    continue;
                }
                if (asteroid.generation >= 3) {
                    removeAsteroid(asteroid);
                    playerPoints++;
                    targetAsteroidCount++;
                }
                else {
                    Asteroid[] newAsteroids = asteroid.split();
                    for (Asteroid newAsteroid : newAsteroids) {
                        addAsteroid(newAsteroid);
                    }
                    removeAsteroid(asteroid);
                }
                spaceship.bullets = Bullet.removeBullet(spaceship.bullets, bullet);
            }
        }
        for (Bullet bullet : spaceship.bullets) {
            if (bullet.outOfBounds(DEFAULT_SCREEN_SIZE)) {
                spaceship.bullets = Bullet.removeBullet(spaceship.bullets, bullet);
            }
        }

        spaceship.handleInputs(keyH, deltaT);
        spaceship.update(deltaT);
        for (Asteroid asteroid : asteroids) {
            asteroid.update(deltaT);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(new Color(0, 0, 0));
        g2.fillRect(0, 0, SCREEN_SIZE.width, SCREEN_SIZE.height);

        spaceship.draw(g2, SCREEN_SCALE);

        for (Asteroid asteroid : asteroids) {
            if (asteroid != null) {
                asteroid.draw(g2, SCREEN_SCALE);
            }
            else {
                System.out.println("null asteroid");
            }
        }
        g2.setColor(new Color(255, 0, 0));
        g2.drawString("Asteroids: " + updateFrequency, 10, 20);


        g2.dispose();
    }
}