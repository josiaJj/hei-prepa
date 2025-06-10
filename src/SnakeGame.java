import java.util.*;

public class SnakeGame {

    enum Direction { UP, DOWN, LEFT, RIGHT }

    static class Point {
        int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }

        Point move(Direction d) {
            return switch (d) {
                case UP -> new Point(x, y - 1);
                case DOWN -> new Point(x, y + 1);
                case LEFT -> new Point(x - 1, y);
                case RIGHT -> new Point(x + 1, y);
            };
        }

        public boolean equals(Object o) {
            if (!(o instanceof Point p)) return false;
            return this.x == p.x && this.y == p.y;
        }

        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    interface MoveStrategy {
        Point computeNextHead(List<Point> body, Direction direction);
    }

    static class SimpleMoveStrategy implements MoveStrategy {
        public Point computeNextHead(List<Point> body, Direction direction) {
            return body.get(0).move(direction);
        }
    }

    static class Snake {
        List<Point> body;
        Direction direction;
        MoveStrategy strategy;

        Snake(List<Point> body, Direction dir, MoveStrategy s) {
            this.body = new ArrayList<>(body);
            this.direction = dir;
            this.strategy = s;
        }

        Point nextHead() {
            return strategy.computeNextHead(body, direction);
        }

        void grow(Point nextHead) {
            body.add(0, nextHead);
        }

        void move(Point nextHead) {
            body.add(0, nextHead);
            body.remove(body.size() - 1);
        }

        boolean collides(Point p) {
            return body.contains(p);
        }
    }

    static class SnakeBuilder {
        public static Snake build() {
            List<Point> body = new ArrayList<>();
            body.add(new Point(4, 2));
            body.add(new Point(3, 2));
            body.add(new Point(2, 2));
            return new Snake(body, Direction.RIGHT, new SimpleMoveStrategy());
        }
    }

    static class FoodFactory {
        public static Point generateFood(List<Point> snakeBody) {
            Random rand = new Random();
            Point food;
            do {
                food = new Point(rand.nextInt(10), rand.nextInt(10));
            } while (snakeBody.contains(food));
            return food;
        }
    }

    abstract static class GameState {
        abstract GameState update(Game game);
    }

    static class MenuState extends GameState {
        GameState update(Game game) {
            System.out.println("=== SNAKE GAME ===");
            System.out.println("Appuyez sur Entrée pour démarrer...");
            new Scanner(System.in).nextLine();
            return new RunningState();
        }
    }

    static class RunningState extends GameState {
        GameState update(Game game) {
            game.render();
            System.out.print("Direction (WASD) : ");
            char c = new Scanner(System.in).next().toUpperCase().charAt(0);
            switch (c) {
                case 'W' -> game.snake.direction = Direction.UP;
                case 'S' -> game.snake.direction = Direction.DOWN;
                case 'A' -> game.snake.direction = Direction.LEFT;
                case 'D' -> game.snake.direction = Direction.RIGHT;
            }

            Point next = game.snake.nextHead();
            if (next.x < 0 || next.x >= 10 || next.y < 0 || next.y >= 10 || game.snake.collides(next)) {
                return new GameOverState();
            }

            if (next.equals(game.food)) {
                game.snake.grow(next);
                game.food = FoodFactory.generateFood(game.snake.body);
            } else {
                game.snake.move(next);
            }
            return this;
        }
    }

    static class GameOverState extends GameState {
        GameState update(Game game) {
            System.out.println("💀 GAME OVER 💀");
            System.out.println("Score: " + (game.snake.body.size() - 3));
            return null; // End of game
        }
    }

    static class Game {
        GameState state;
        Snake snake;
        Point food;

        Game() {
            this.snake = SnakeBuilder.build();
            this.food = FoodFactory.generateFood(snake.body);
            this.state = new MenuState();
        }

        void run() {
            while (state != null) {
                state = state.update(this);
            }
        }

        void render() {
            char[][] grid = new char[10][10];
            for (char[] row : grid) Arrays.fill(row, '.');

            for (Point p : snake.body) grid[p.y][p.x] = '*';
            grid[food.y][food.x] = '@';

            for (char[] row : grid) {
                for (char c : row) System.out.print(c + " ");
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        new Game().run();
    }
}
