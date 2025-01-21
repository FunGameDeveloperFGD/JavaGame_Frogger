import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Game extends JPanel implements Runnable, KeyListener {

    Frog frog;
    Lane[] lanes;
    Winner[] winners;
    BoxCollider[] walls;

    boolean isRunning;
    Thread thread;
    BufferedImage view, background;

    public final int WIDTH = 224;
    public final int HEIGHT = 256;
    public final int SCALE = 2;

    public Game() {
        setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
        addKeyListener(this);
    }

    public static void main(String[] args) {
        JFrame w = new JFrame("Frogger");
        w.setResizable(false);
        w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        w.add(new Game());
        w.pack();
        w.setLocationRelativeTo(null);
        w.setVisible(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this);
            isRunning = true;
            thread.start();
        }
    }

    public void start() {
        try {
            view = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            background = ImageIO.read(getClass().getResource("/background.png"));
            frog = new Frog(112, 226, 14, 14);

            lanes = new Lane[10];
            lanes[0] = new Lane(TypeObstacle.LOG, 3, 3, -1.3f, 80);
            lanes[1] = new Lane(TypeObstacle.LOG, 4, 2, 0.5f, 80);
            lanes[2] = new Lane(TypeObstacle.LOG, 5, 3, -1.8f, 80);
            lanes[3] = new Lane(TypeObstacle.LOG, 6, 1, 2, 80);
            lanes[4] = new Lane(TypeObstacle.LOG, 7, 3, -1.2f, 80);
            lanes[5] = new Lane(TypeObstacle.CAR_5, 9, 3, 1, 60);
            lanes[6] = new Lane(TypeObstacle.CAR_3, 10, 4, 1, 3 * 16);
            lanes[7] = new Lane(TypeObstacle.CAR_4, 11, 2, 1, 30);
            lanes[8] = new Lane(TypeObstacle.CAR_2, 12, 4, 1.5f, 45);
            lanes[9] = new Lane(TypeObstacle.CAR_1, 13, 3, 1, 80);

            winners = new Winner[5];
            for (int i = 0; i < 5; i++) {
                winners[i] = new Winner((i * 48) + 8, 32);
            }

            walls = new BoxCollider[6];
            for (int i = 0; i < 6; i++) {
                walls[i] = new BoxCollider(-22 + (i * 48), 24, 28, 24);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        frog.update();

        for (Lane lane : lanes) {
            lane.update();
        }

        int laneIndex = (int) (frog.y / 16);
        if (laneIndex >= 9 && laneIndex <= 13) {
            laneIndex -= 4;
            lanes[laneIndex].check(frog);
        } else if (laneIndex >= 3 && laneIndex <= 7) {
            laneIndex -= 3;
            lanes[laneIndex].check(frog);
        }

        for (Winner winner : winners) {
            if (frog.intersects(winner)) {
                if (!winner.visible) {
                    winner.visible = true;
                } else {
                    frog.resetFrog();
                }
            }
        }

        for (BoxCollider wall : walls) {
            if (frog.intersects(wall)) {
                frog.resetFrog();
            }
        }
    }

    public void draw() {
        Graphics2D g2 = (Graphics2D) view.getGraphics();
        g2.drawImage(background, 0, 0, background.getWidth(), background.getHeight(), null);

        for (Lane lane : lanes) {
            lane.draw(g2);
        }

        for (Winner winner : winners) {
            winner.draw(g2);
        }

        frog.draw(g2);

        Graphics g = getGraphics();
        g.drawImage(view, 0, 0, WIDTH * SCALE, HEIGHT * SCALE, null);
    }

    @Override
    public void run() {
        try {
            requestFocus();
            start();
            while (isRunning) {
                update();
                draw();
                Thread.sleep(1000 / 60);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        frog.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    enum TypeObstacle {
        CAR_1, CAR_2, CAR_3, CAR_4, CAR_5, LOG
    }

    public class Obstacle extends BoxCollider {
        float speed = 0;
        BufferedImage obstacle;
        TypeObstacle type;

        public Obstacle(TypeObstacle type, float x, float y, float speed) {
            super(x, y, 15, 14);
            try {
                this.speed = speed;
                this.type = type;

                BufferedImage spriteSheets = ImageIO.read(getClass().getResource("/obstacles_sprite_sheet.png"));

                switch (type) {
                    case CAR_1:
                        obstacle = spriteSheets.getSubimage(0, 0, 15, 14);
                        break;
                    case CAR_2:
                        obstacle = spriteSheets.getSubimage(0, 14, 15, 14);
                        break;
                    case CAR_3:
                        width = 14;
                        obstacle = spriteSheets.getSubimage(0, 2 * 14, 14, 14);
                        break;
                    case CAR_4:
                        this.speed = -this.speed;
                        obstacle = spriteSheets.getSubimage(0, 3 * 14, 15, 14);
                        break;
                    case CAR_5:
                        this.speed = -this.speed;
                        width = 30;
                        obstacle = spriteSheets.getSubimage(0, 4 * 14, 30, 14);
                        break;
                    case LOG:
                        width = 3 * 14;
                        obstacle = spriteSheets.getSubimage(0, 5 * 14, 3 * 14, 14);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void update() {
            x = x + speed;
            if (speed > 0 && x > WIDTH) {
                x = -obstacle.getWidth();
            } else if (speed < 0 && x < -obstacle.getWidth()) {
                x = WIDTH;
            }
        }

        public void draw(Graphics2D g) {
            g.drawImage(obstacle, (int) x, (int) y, obstacle.getWidth(), obstacle.getHeight(), null);
        }
    }

    public class Lane extends BoxCollider {
        Obstacle[] obstacles;
        float speed;
        TypeObstacle type;

        public Lane(TypeObstacle type, int index, int n, float speed, float spacing) {
            super(0, index * 16, WIDTH, 16);
            obstacles = new Obstacle[n];
            this.speed = speed;
            this.type = type;
            for (int i = 0; i < n; i++) {
                obstacles[i] = new Obstacle(type, spacing * i, y, speed);
            }
        }

        public void check(Frog frog) {
            if (type != TypeObstacle.LOG) {
                for (Obstacle obstacle : obstacles) {
                    if (frog.intersects(obstacle)) {
                        frog.resetFrog();
                    }
                }
            } else {
                boolean ok = false;
                for (Obstacle obstacle : obstacles) {
                    if (frog.intersects(obstacle)) {
                        ok = true;
                        frog.x += obstacle.speed;
                    }
                }
                if (!ok) {
                    frog.resetFrog();
                }
            }
        }

        public void update() {
            for (Obstacle obstacle : obstacles) {
                obstacle.update();
            }
        }

        public void draw(Graphics2D g2) {
            for (Obstacle obstacle : obstacles) {
                obstacle.draw(g2);
            }
        }
    }

    public class Winner extends BoxCollider {
        boolean visible;
        BufferedImage image;

        public Winner(float x, float y) {
            super(x, y, 0, 0);
            try {
                image = ImageIO.read(getClass().getResource("/win.png"));
                width = image.getWidth();
                height = image.getHeight();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void draw(Graphics2D g) {
            if (visible) {
                g.drawImage(image, (int) x, (int) y, (int) width, (int) height, null);
            }
        }
    }

    public class Frog extends BoxCollider {
        BufferedImage frog;
        BufferedImage[] anim, frogAnimLeft, frogAnimRight, frogAnimUp, frogAnimDown;

        private int frameIndex;
        private boolean jumping;

        public Frog(float x, float y, float width, float height) {
            super(x, y, width, height);
            try {
                BufferedImage frogSpriteSheet = ImageIO.read(getClass().getResource("/frog_sprite_sheet.png"));
                frogAnimDown = new BufferedImage[3];
                frogAnimUp = new BufferedImage[3];
                frogAnimRight = new BufferedImage[3];
                frogAnimLeft = new BufferedImage[3];
                anim = new BufferedImage[3];

                int frogTileSize = 14;
                for (int i = 0; i < 3; i++) {
                    frogAnimDown[i] = frogSpriteSheet.getSubimage(
                            i * frogTileSize,
                            0,
                            frogTileSize,
                            frogTileSize
                    );
                    frogAnimUp[i] = frogSpriteSheet.getSubimage(
                            i * frogTileSize,
                            frogTileSize,
                            frogTileSize,
                            frogTileSize
                    );
                    frogAnimRight[i] = frogSpriteSheet.getSubimage(
                            i * frogTileSize,
                            frogTileSize * 2,
                            frogTileSize,
                            frogTileSize
                    );
                    frogAnimLeft[i] = frogSpriteSheet.getSubimage(
                            i * frogTileSize,
                            frogTileSize * 3,
                            frogTileSize,
                            frogTileSize
                    );
                }

                frog = frogAnimUp[2];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void resetFrog() {
            x = 112;
            y = 226;
            frog = frogAnimUp[2];
        }

        public void update() {
            if (jumping) {
                frameIndex++;
                frog = anim[(int) ((frameIndex / 5.0) * (anim.length - 1))];
                if (frameIndex > 5) {
                    frameIndex = 0;
                    jumping = false;
                }
            }
            x = Math.min(Math.max(x, 0), WIDTH - width);
            y = Math.min(Math.max(y, 0), HEIGHT - height);
        }

        public void draw(Graphics2D g) {
            g.drawImage(frog, (int) x, (int) y - 1, (int) width, (int) height, null);
        }

        public void jump(int xDir, int yDir) {
            frameIndex = 0;
            x += 16 * xDir;
            y += 16 * yDir;
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                jumping = true;
                anim = frogAnimUp;
                jump(0, -1);
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                jumping = true;
                anim = frogAnimDown;
                jump(0, 1);
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                jumping = true;
                anim = frogAnimRight;
                jump(1, 0);
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                jumping = true;
                anim = frogAnimLeft;
                jump(-1, 0);
            }
        }
    }

    public class BoxCollider {
        float x, y, width, height;

        public BoxCollider(float x, float y, float width, float height) {
            this.width = width;
            this.height = height;
            this.x = x;
            this.y = y;
        }

        boolean intersects(BoxCollider other) {
            float left = x;
            float right = x + width;
            float top = y;
            float bottom = y + height;

            float oLeft = other.x;
            float oRight = other.x + other.width;
            float oTop = other.y;
            float oBottom = other.y + other.height;

            return !(left >= oRight ||
                    right <= oLeft ||
                    top >= oBottom ||
                    bottom <= oTop);
        }
    }
}


































































































