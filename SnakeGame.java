import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGame extends JFrame {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int SNAKE_SIZE = 10;
    private static final int INFO_WIDTH = WIDTH / 4;
    private static final int GAME_WIDTH = WIDTH - INFO_WIDTH;
    private static final int BORDER_THICKNESS = 5;

    private GamePanel gamePanel;

    public SnakeGame() {
        setTitle("Snake Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getWidth(), getHeight());

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 18));
                g.drawString("Счёт: " + gamePanel.getScore(), 20, 50);
                g.drawString("Скорость: " + gamePanel.getSpeedLevel(), 20, 80);
            }
        };
        infoPanel.setPreferredSize(new Dimension(INFO_WIDTH + BORDER_THICKNESS, HEIGHT));
        add(infoPanel, BorderLayout.WEST);

        gamePanel = new GamePanel(infoPanel);
        gamePanel.setBorder(BorderFactory.createMatteBorder(BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, BORDER_THICKNESS, Color.BLUE));
        add(gamePanel, BorderLayout.CENTER);

        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SnakeGame game = new SnakeGame();
            game.setVisible(true);
            game.startGame();
        });
    }

    public void startGame() {
        gamePanel.startGame();
    }

    private class GamePanel extends JPanel implements ActionListener, KeyListener {
        private LinkedList<Point> snake;
        private int direction = KeyEvent.VK_RIGHT;
        private Timer timer;
        private boolean running = false;
        private Point food;
        private int score = 0;
        private int speed = 100;
        private final Random rand = new Random();
        private final JPanel infoPanel;
        private boolean showGrid = true;  // новое поле - показывать сетку или нет


        public GamePanel(JPanel infoPanel) {
            this.infoPanel = infoPanel;
            setPreferredSize(new Dimension(GAME_WIDTH, HEIGHT));
            timer = new Timer(speed, this);
            addKeyListener(this);
        }

        public int getScore() {
            return score;
        }

        public int getSpeedLevel() {
            return 11 - speed / 10;
        }

        public void startGame() {
            running = true;
            snake = new LinkedList<>();
            direction = KeyEvent.VK_RIGHT;
            score = 0;
            speed = 100;

            // Центр игрового поля, кратный SNAKE_SIZE
            int startX = ((getWidth() / SNAKE_SIZE) / 2) * SNAKE_SIZE;
            int startY = ((getHeight() / SNAKE_SIZE) / 2) * SNAKE_SIZE;
            snake.add(new Point(startX, startY));

            placeFood();
            timer.setDelay(speed);
            timer.start();

            setFocusable(true);
            requestFocusInWindow();
        }

        private void placeFood() {
            int maxX = (getWidth() / SNAKE_SIZE) - 1;
            int maxY = (getHeight() / SNAKE_SIZE) - 1;
            int x, y;
            do {
                x = rand.nextInt(maxX + 1) * SNAKE_SIZE;
                y = rand.nextInt(maxY + 1) * SNAKE_SIZE;
                food = new Point(x, y);
            } while (snake.contains(food));
        }

        public void stopGame() {
            running = false;
            timer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.GREEN);
            g.fillRect(0, 0, getWidth(), getHeight());

            if (showGrid) {
                g.setColor(new Color(0, 100, 0));
                for (int y = 0; y <= getHeight(); y += SNAKE_SIZE) {
                    g.drawLine(0, y, getWidth(), y);
                }
                for (int x = 0; x <= getWidth(); x += SNAKE_SIZE) {
                    g.drawLine(x, 0, x, getHeight());
                }
            }

            g.setColor(Color.YELLOW);
            g.fillRect(food.x, food.y, SNAKE_SIZE, SNAKE_SIZE);

            g.setColor(Color.RED);
            for (Point p : snake) {
                g.fillRect(p.x, p.y, SNAKE_SIZE, SNAKE_SIZE);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (running) {
                moveSnake();
                checkCollision();
                repaint();
                infoPanel.repaint();
            }
        }

        private void moveSnake() {
            Point head = new Point(snake.getFirst());
            switch (direction) {
                case KeyEvent.VK_UP -> head.y -= SNAKE_SIZE;
                case KeyEvent.VK_DOWN -> head.y += SNAKE_SIZE;
                case KeyEvent.VK_LEFT -> head.x -= SNAKE_SIZE;
                case KeyEvent.VK_RIGHT -> head.x += SNAKE_SIZE;
            }

            snake.addFirst(head);

            if (head.equals(food)) {
                score++;
                if (score % 5 == 0 && speed > 20) {
                    speed -= 10;
                    timer.setDelay(speed);
                }
                placeFood();
            } else {
                snake.removeLast(); // если еда не съедена — убираем хвост
            }
        }

        private void checkCollision() {
            Point head = snake.getFirst();

            // Проверка столкновения со стенами
            if (head.x < 0 || head.y < 0 || head.x >= getWidth() || head.y >= getHeight()) {
                endGame();
            }

            // Проверка столкновения с телом змейки
            for (int i = 1; i < snake.size(); i++) {
                if (head.equals(snake.get(i))) {
                    endGame();
                }
            }
        }

        private void endGame() {
            stopGame();
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Игра окончена!\nСчёт: " + score + "\nПопробовать снова?",
                    "Конец игры",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"Заново", "Выход"},
                    "Заново"
            );

            if (choice == JOptionPane.YES_OPTION) {
                startGame();
            } else {
                System.exit(0);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            // Переключение видимости сетки по пробелу
            if (key == KeyEvent.VK_SPACE) {
                showGrid = !showGrid;
                repaint();
                return; // выходим, не меняем направление
            }

            // Изменение направления змейки
            if ((direction == KeyEvent.VK_LEFT && key != KeyEvent.VK_RIGHT) ||
                    (direction == KeyEvent.VK_RIGHT && key != KeyEvent.VK_LEFT) ||
                    (direction == KeyEvent.VK_UP && key != KeyEvent.VK_DOWN) ||
                    (direction == KeyEvent.VK_DOWN && key != KeyEvent.VK_UP)) {
                direction = key;
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {}
        @Override
        public void keyTyped(KeyEvent e) {}
    }
}
