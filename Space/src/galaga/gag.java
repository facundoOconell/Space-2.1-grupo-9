package galaga;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class gag extends JPanel implements ActionListener {

    private static final int GAME_WIDTH = 1000;
    private static final int GAME_HEIGHT = 800;

    private Timer gameTimer, enemyMovementTimer, enemyShootingTimer, levelTransitionTimer;
    private List<Rectangle> bullets;
    private List<Rectangle> enemyBullets;
    private Rectangle player;
    private List<Enemy> enemies;
    private List<Barrier> barriers;
    private boolean gameWon = false;
    private boolean leftPressed = false, rightPressed = false, spacePressed = false;
    private Random random = new Random();
    private int waveNumber = 0;
    private int maxWaves = 5; // Aumentado a 5 oleadas
    private int enemyMoveSpeed = 2; // Velocidad general de los enemigos
    private int bulletSpeed = 10;
    private int enemyBulletSpeed = 5;
    private boolean movingRight = true;
    private int enemyRowCount = 2;
    private int enemiesPerRow = 6;
    private int enemyVerticalStep = 50;
    private int playerLives = 3;
    private boolean transitioningToNextLevel = false;
    private int levelTransitionDelay = 5000; // 5 segundos

    private boolean showVictoryMessage = false;
    
    // Lista de colores para las oleadas
    private List<Color> enemyColors;

    // Contador de balas disparadas y máximo permitido
    private int bulletsShot = 0;
    private final int MAX_BULLETS = 500 ;
    
    private Image playerSprite;
    private Image enemySprite1;
    private Image enemySprite2;
    private Image bossSprite;
    private Image backgroundImage;
    private Image enemySprite3; 
    private Image enemySprite4;
    private Image enemySprite5;
    
    public gag() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.BLUE);
        setFocusable(true);
        playerSprite = new ImageIcon("C:\\Users\\Sergio\\Documents\\Space\\src\\sprays\\nave-galaga.png").getImage();
        
        enemySprite1 = new ImageIcon("C:\\Users\\Sergio\\Documents\\Space\\src\\sprays\\enemigo1.png").getImage();
        enemySprite2 = new ImageIcon("C:\\Users\\Sergio\\Documents\\Space\\src\\sprays\\enemigo2.png").getImage();
        bossSprite = new ImageIcon("C:\\Users\\Sergio\\Documents\\Space\\src\\sprays\\boss-final.png").getImage();
        backgroundImage = new ImageIcon("C:\\Users\\Sergio\\Documents\\Space\\src\\sprays\\fondo.png").getImage();    
        enemySprite3 = new ImageIcon("C:\\Users\\Sergio\\Documents\\Space\\src\\sprays\\enemigo2.png").getImage();
        enemySprite4 = new ImageIcon("C:\\Users\\Sergio\\Documents\\Space\\src\\sprays\\ocho-final.png").getImage();
        enemySprite5 = new ImageIcon("C:\\Users\\Sergio\\Documents\\Space\\src\\sprays\\enemigo1.png").getImage();

        
        
        player = new Rectangle(GAME_WIDTH / 2 - 25, GAME_HEIGHT - 100, 50, 30);
        bullets = new ArrayList<>();
        enemyBullets = new ArrayList<>();
        enemies = new ArrayList<>();
        barriers = new ArrayList<>();
        enemyColors = new ArrayList<>();
        setupBarriers();
        spawnEnemies();

        // Configuración de los temporizadores
        gameTimer = new Timer(16, this); // Aproximadamente 60 FPS
        enemyMovementTimer = new Timer(16, e -> moveEnemies());
        enemyShootingTimer = new Timer(1000, e -> shootEnemyBullets());

        gameTimer.start();
        enemyMovementTimer.start();
        enemyShootingTimer.start();

     // Temporizador para la transición de niveles
        levelTransitionTimer = new Timer(levelTransitionDelay, e -> {
            transitioningToNextLevel = false;
            waveNumber++;
            
            // Limpiar las balas del jugador y de los enemigos al iniciar una nueva oleada
            bullets.clear();
            enemyBullets.clear();
            
            if (waveNumber >= maxWaves) {
                gameWon = true;
                stopTimers();
            } else {
                updateEnemySettings();
                setupBarriers(); // Restablecer barreras al iniciar una nueva oleada
                spawnEnemies();
                // No reiniciar el contador de balas disparadas ni las balas
                // bulletsShot = 0; // Esta línea se ha eliminado
            }
            levelTransitionTimer.stop();
        });




        // Manejador de teclado
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameWon || transitioningToNextLevel) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        leftPressed = true;
                        break;
                    case KeyEvent.VK_RIGHT:
                        rightPressed = true;
                        break;
                    case KeyEvent.VK_SPACE:
                        spacePressed = true;
                        shoot();
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        leftPressed = false;
                        break;
                    case KeyEvent.VK_RIGHT:
                        rightPressed = false;
                        break;
                    case KeyEvent.VK_SPACE:
                        spacePressed = false;
                        break;
                }
            }
        });
    }

    private void updateEnemySettings() {
        switch (waveNumber) {
            case 0:
                enemyRowCount = 2;
                enemiesPerRow = 6;
                break;
            case 1:
                enemyRowCount = 3;
                enemiesPerRow = 6;
                break;
            case 2:
                enemyRowCount = 4;
                enemiesPerRow = 6;
                break;
            case 3:
                enemyRowCount = 1; // Solo una fila para la cuarta oleada
                enemiesPerRow = 1; // Solo un enemigo en la fila
                enemyMoveSpeed = 5; // Aumentar la velocidad para la cuarta oleada
                break;
            case 4:
                enemyRowCount = 1; // Solo una fila para la quinta oleada (jefe)
                enemiesPerRow = 1; // Solo un jefe
                enemyMoveSpeed = 5; // El jefe se moverá más rápido en la oleada 5
                break;
            default:
                enemyRowCount = 2;
                enemiesPerRow = 6;
                break;
        }
    }



    private void setupBarriers() {
        barriers.clear(); // Limpiar barreras anteriores
        int barrierCount = 3;
        int barrierWidth = 120;
        int barrierHeight = 40;
        int barrierSpacing = 200;
        int edgeSpacing = 200;

        int totalBarriersWidth = (barrierCount - 1) * barrierSpacing + barrierWidth * barrierCount;
        int totalSpacingWidth = GAME_WIDTH - 2 * edgeSpacing - totalBarriersWidth;

        int startX = edgeSpacing + totalSpacingWidth / 2;
        int startY = GAME_HEIGHT - 200;

        for (int i = 0; i < barrierCount; i++) {
            int x = startX + i * (barrierWidth + barrierSpacing);
            barriers.add(new Barrier(x, startY, barrierWidth, barrierHeight));
        }
    }

    private void shoot() {
        if (!gameWon && !transitioningToNextLevel && bulletsShot < MAX_BULLETS) {
            bullets.add(new Rectangle(player.x + player.width / 2 - 2, player.y, 4, 10));
            bulletsShot++;
        }
    }
                   

    private void shootEnemyBullets() {
        if (!gameWon && !transitioningToNextLevel && waveNumber > 0) {
            for (Enemy enemy : enemies) {
                int bulletX = enemy.getX() + enemy.getWidth() / 2 - 2;
                int bulletY = enemy.getY() + enemy.getHeight();

                if (enemy.isBoss()) {
                    // Jefe dispara 10 balas en diferentes direcciones
                    int numBullets = 10; // Número de balas a disparar
                    int bulletSpacing = 20; // Espaciado entre balas
                    for (int i = -numBullets / 2; i <= numBullets / 2; i++) {
                        enemyBullets.add(new Rectangle(bulletX + i * bulletSpacing, bulletY, 4, 10));
                    }
                } else if (waveNumber == 3) {
                    // Cuarta oleada, nave dispara 3 balas
                    int numBullets = 3;
                    int bulletSpacing = 20; // Ajustado para mayor separación
                    for (int i = -numBullets / 2; i <= numBullets / 2; i++) {
                        enemyBullets.add(new Rectangle(bulletX + i * bulletSpacing, bulletY, 4, 10));
                    }
                } else {
                    // Enemigos normales disparan una bala
                    enemyBullets.add(new Rectangle(bulletX, bulletY, 4, 10));
                }
            }
        }
    }



    private void spawnEnemies() {
        if (waveNumber >= maxWaves) return;

        enemies.clear();
        enemyColors.clear(); // Limpiar colores antiguos
        int startX = 50;
        int startY = 50;

        Color[] colors = {Color.YELLOW, Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA};
        Color currentWaveColor = colors[waveNumber % colors.length];

        if (waveNumber == 3) { // Cuarta oleada
            int x = GAME_WIDTH / 2 - 200 / 2; // Centrando el enemigo (ancho 80)
            int y = 80;
            Enemy toughEnemy = new Enemy(x, y, 80, 50, 20, false, gameWon); // Enemigo con 20 de vida
            enemies.add(toughEnemy);
            enemyColors.add(currentWaveColor);
        } else if (waveNumber == 4) {
            int x = GAME_WIDTH / 2 - 300 / 2; // Centrando el jefe (ancho 300)
            int y = 50;
            Enemy boss = new Enemy(x, y, 300, 150, 50, true, gameWon); // Jefe más grande, con más vida
            enemies.add(boss);
            enemyColors.add(currentWaveColor);
            enemyMoveSpeed = 2; // Velocidad de movimiento del jefe
        } else {
            for (int row = 0; row < enemyRowCount; row++) {
                for (int col = 0; col < enemiesPerRow; col++) {
                    int x = startX + col * 100;
                    int y = startY + row * 70;

                    boolean isKamikaze = (waveNumber == 2 && row == 0 && col < 3); // 3 kamikazes en la primera fila
                    int health = isKamikaze ? 3 : 1; // Si es kamikaze, vida = 3, de lo contrario, vida = 1
                    Enemy enemy = new Enemy(x, y, 80, 50, health, false, isKamikaze); // Agregar parámetro kamikaze
                    enemies.add(enemy);
                    enemyColors.add(currentWaveColor); 
                }
            }
        }
    }


    




    private void moveEnemies() {
        if (transitioningToNextLevel) return;

        boolean changeDirection = false;

        for (Enemy enemy : enemies) {
            if (enemy.isBoss()) {
                // Movimiento del jefe en la oleada 5
                if (movingRight) {
                    enemy.x += enemyMoveSpeed;
                    if (enemy.x + enemy.width > GAME_WIDTH) {
                        movingRight = false; // Cambiar dirección
                    }
                } else {
                    enemy.x -= enemyMoveSpeed;
                    if (enemy.x < 0) {
                        movingRight = true; // Cambiar dirección
                    }
                }
            } else if (enemy.isKamikaze()) {
                // Movimiento kamikaze hacia el jugador
                if (enemy.x < player.x) {
                    enemy.x += enemyMoveSpeed;
                } else if (enemy.x > player.x) {
                    enemy.x -= enemyMoveSpeed;
                }

                if (enemy.y < player.y) {
                    enemy.y += enemyMoveSpeed;
                }
            } else {
                // Movimiento de enemigos normales
                if (movingRight) {
                    enemy.x += enemyMoveSpeed;
                    if (enemy.x + enemy.width > GAME_WIDTH) {
                        changeDirection = true;
                    }
                } else {
                    enemy.x -= enemyMoveSpeed;
                    if (enemy.x < 0) {
                        changeDirection = true;
                    }
                }
            }
        }

        if (waveNumber != 4) { // No mover enemigos en la oleada 5 (jefe)
            if (changeDirection) {
                movingRight = !movingRight;
                for (Enemy enemy : enemies) {
                    if (!enemy.isBoss() && !enemy.isKamikaze()) {
                        enemy.y += enemyVerticalStep;
                    }
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
            
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        g.drawImage(playerSprite, player.x, player.y, player.width, player.height, this);
        
        
        for (Enemy enemy : enemies) {
            if (enemy.isBoss()) {
                g.drawImage(bossSprite, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(), this);
            } else {
                Image spriteToUse;
                switch (waveNumber) {
                    case 0:
                        spriteToUse = enemySprite1;
                        break;
                    case 1:
                        spriteToUse = enemySprite2;
                        break;
                    case 2:
                        spriteToUse = enemySprite3;
                        break;
                    case 3:
                        spriteToUse = enemySprite4;
                        break;
                    case 4:
                        spriteToUse = enemySprite5;
                        break;
                    default:
                        continue; // Saltar el dibujo si el número de oleada no es reconocido
                }
                g.drawImage(spriteToUse, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(), this);
            }
        }

       
        
        if (gameWon) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("¡Ganaste!", GAME_WIDTH / 2 - 150, GAME_HEIGHT / 2);
            return;
        }

        if (playerLives <= 0) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("¡Perdiste!", GAME_WIDTH / 2 - 150, GAME_HEIGHT / 2);
            return;
        }

        if (transitioningToNextLevel) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("Oleada " + (waveNumber + 2), GAME_WIDTH / 2 - 150, GAME_HEIGHT / 2);
            return;
        }

        // Dibujar jugador
    

        // Dibujar balas del jugador
        g.setColor(Color.RED);
        for (Rectangle bullet : bullets) {
            g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
        }

        // Dibujar balas de los enemigos
        g.setColor(Color.GREEN);
        for (Rectangle enemyBullet : enemyBullets) {
            g.fillRect(enemyBullet.x, enemyBullet.y, enemyBullet.width, enemyBullet.height);
        }

       

        // Dibujar barreras
        g.setColor(Color.GRAY);
        for (Barrier barrier : barriers) {
            g.fillRect(barrier.getX(), barrier.getY(), barrier.getWidth(), barrier.getHeight());
        }

        // Dibujar vidas del jugador
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Vidas: " + playerLives, 10, 30);

        // Mostrar cantidad de balas restantes
        g.drawString("Balas restantes: " + (MAX_BULLETS - bulletsShot), 10, 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (transitioningToNextLevel || gameWon) return;

        // Movimiento del jugador
        if (leftPressed && player.x > 0) {
            player.x -= 5;
        }
        if (rightPressed && player.x + player.width < GAME_WIDTH) {
            player.x += 5;
        }

        // Movimiento de las balas del jugador
        List<Rectangle> bulletsToRemove = new ArrayList<>();
        for (Rectangle bullet : bullets) {
            bullet.y -= bulletSpeed;
            if (bullet.y < 0) {
                bulletsToRemove.add(bullet);
            }
        }
        bullets.removeAll(bulletsToRemove);

        // Movimiento de las balas de los enemigos
        List<Rectangle> enemyBulletsToRemove = new ArrayList<>();
        for (Rectangle enemyBullet : enemyBullets) {
            enemyBullet.y += enemyBulletSpeed;
            if (enemyBullet.y > GAME_HEIGHT) {
                enemyBulletsToRemove.add(enemyBullet);
            }
        }
        enemyBullets.removeAll(enemyBulletsToRemove);

        // Detección de colisiones y eliminación de enemigos o barreras
        detectCollisions();

        // Verificación de si se ha ganado el juego
        if (waveNumber >= maxWaves) {
            gameWon = true;
            stopTimers();
        }

        repaint();
    }

    private void detectCollisions() {
        Set<Enemy> enemiesToRemove = new HashSet<>();
        Set<Rectangle> bulletsToRemove = new HashSet<>();
        Set<Rectangle> enemyBulletsToRemove = new HashSet<>();
        Set<Barrier> barriersToRemove = new HashSet<>();

        // Colisiones de balas del jugador con enemigos
        for (Rectangle bullet : bullets) {
            for (Enemy enemy : enemies) {
                if (bullet.intersects(enemy.getBounds())) {
                    enemy.damage();
                    bulletsToRemove.add(bullet);
                    if (enemy.getHealth() <= 0) {
                        enemiesToRemove.add(enemy);
                    }
                }
            }
        }

        bullets.removeAll(bulletsToRemove);
        enemies.removeAll(enemiesToRemove);

        if (enemies.isEmpty() && !transitioningToNextLevel) {
            if (waveNumber >= maxWaves - 1) {
                gameWon = true;
                stopTimers();
            } else {
                transitioningToNextLevel = true;
                levelTransitionTimer.start();
            }
        }

        // Colisiones de balas de enemigos con el jugador
        for (Rectangle enemyBullet : enemyBullets) {
            if (enemyBullet.intersects(player)) {
                enemyBulletsToRemove.add(enemyBullet);
                playerLives--;
                if (playerLives <= 0) {
                    stopTimers();
                }
            }
        }
        enemyBullets.removeAll(enemyBulletsToRemove);

        // Colisiones de balas del jugador con las barreras
        for (Rectangle bullet : bullets) {
            for (Barrier barrier : barriers) {
                if (bullet.intersects(barrier.getBounds())) {
                    bulletsToRemove.add(bullet);
                    barrier.damage();
                    if (barrier.getHealth() <= 0) {
                        barriersToRemove.add(barrier);
                    }
                }
            }
        }
        bullets.removeAll(bulletsToRemove);
        barriers.removeAll(barriersToRemove);

        // Colisiones de balas de enemigos con las barreras
        for (Rectangle enemyBullet : enemyBullets) {
            for (Barrier barrier : barriers) {
                if (enemyBullet.intersects(barrier.getBounds())) {
                    enemyBulletsToRemove.add(enemyBullet);
                    barrier.damage();
                    if (barrier.getHealth() <= 0) {
                        barriersToRemove.add(barrier);
                    }
                }
            }
        }
        enemyBullets.removeAll(enemyBulletsToRemove);
        barriers.removeAll(barriersToRemove);

        // Colisiones de enemigos con las barreras
        for (Enemy enemy : enemies) {
            for (Barrier barrier : barriers) {
                if (enemy.getBounds().intersects(barrier.getBounds())) {
                    enemiesToRemove.add(enemy);
                    barrier.damage();
                    if (barrier.getHealth() <= 0) {
                        barriersToRemove.add(barrier);
                    }
                }
            }
        }
        enemies.removeAll(enemiesToRemove);
        barriers.removeAll(barriersToRemove);

        // Colisiones de enemigos con el jugador
        for (Enemy enemy : enemies) {
            if (enemy.getBounds().intersects(player)) {
                enemiesToRemove.add(enemy);
                playerLives--;
                if (playerLives <= 0) {
                    stopTimers();
                }
            }
        }
        enemies.removeAll(enemiesToRemove);
    }                       

    private void stopTimers() {
        gameTimer.stop();
        enemyMovementTimer.stop();
        enemyShootingTimer.stop();
    }

    private class Enemy {
        private int x, y, width, height, health;
        private boolean isBoss;
        private boolean isKamikaze;

        public Enemy(int x, int y, int width, int height, int health, boolean isBoss, boolean isKamikaze) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.health = health;
            this.isBoss = isBoss;
            this.isKamikaze = isKamikaze;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getHealth() {
            return health;
        }

        public void damage() {
            health--; // Disminuye la salud en 1
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        public boolean isBoss() {
            return isBoss;
        }

        public boolean isKamikaze() {
            return isKamikaze;
        }
    }


    private class Barrier {
        private int x, y, width, height, health;

        public Barrier(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.health = 20; // Salud inicial
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getHealth() {
            return health;
        }

        public void damage() {
            health--;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Galaga");
        gag gamePanel = new gag();
        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}