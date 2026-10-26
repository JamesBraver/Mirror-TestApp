import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

public final class TetrisGame {
    static final private int BOARD_COLUMNS = 10;
    static final private int BOARD_ROWS = 20;
    static final private int CELL_SIZE = 28;
    static final Color[] PALETTE = new Color[]{new Color(35, 35, 42), new Color(50, 210, 205), new Color(72, 123, 255), new Color(255, 157, 57), new Color(253, 215, 70), new Color(92, 201, 108), new Color(187, 93, 214), new Color(237, 92, 102)};
    static final int[][][][] PIECE_SHAPES = new int[][][][]{new int[][][]{new int[][]{{0, 1}, {1, 1}, {2, 1}, {3, 1}}, new int[][]{{2, 0}, {2, 1}, {2, 2}, {2, 3}}}, new int[][][]{new int[][]{{0, 0}, {0, 1}, {1, 1}, {2, 1}}, new int[][]{{1, 0}, {2, 0}, {1, 1}, {1, 2}}}, new int[][][]{new int[][]{{2, 0}, {0, 1}, {1, 1}, {2, 1}}, new int[][]{{1, 0}, {1, 1}, {1, 2}, {2, 2}}}, new int[][][]{new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}}}, new int[][][]{new int[][]{{1, 0}, {2, 0}, {0, 1}, {1, 1}}, new int[][]{{1, 0}, {1, 1}, {2, 1}, {2, 2}}}, new int[][][]{new int[][]{{1, 0}, {0, 1}, {1, 1}, {2, 1}}, new int[][]{{1, 0}, {1, 1}, {2, 1}, {1, 2}}, new int[][]{{0, 1}, {1, 1}, {2, 1}, {1, 2}}, new int[][]{{1, 0}, {0, 1}, {1, 1}, {1, 2}}}, new int[][][]{new int[][]{{0, 0}, {1, 0}, {1, 1}, {2, 1}}, new int[][]{{2, 0}, {1, 1}, {2, 1}, {1, 2}}}};

    private TetrisGame() {
    }

    static void main(String[] stringArray) {
        if (stringArray.length == 1 && "--smoke".equals(stringArray[0])) {
            TetrisGame.runModelSmokeTest();
            return;
        }
        if (stringArray.length == 1 && "--render-smoke".equals(stringArray[0])) {
            TetrisGame.runRenderSmokeTest();
            return;
        }
        SwingUtilities.invokeLater(new LaunchTask());
    }

    private static void runModelSmokeTest() {
        TetrisModel class1 = new TetrisModel(new Random(20260828L));
        for (int i = 0; i < 600; ++i) {
            switch (i % 6) {
                case 0: {
                    class1.moveHorizontal(-1);
                    break;
                }
                case 1: {
                    class1.moveHorizontal(1);
                    break;
                }
                case 2: {
                    class1.rotatePiece();
                    break;
                }
                case 3: {
                    class1.tick();
                    break;
                }
                case 4: {
                    class1.softDrop();
                    break;
                }
                default: {
                    class1.hardDrop();
                }
            }
            if (class1.isGameOver()) {
                class1.reset();
            }
            class1.validateState();
        }
        System.out.println("Tetris smoke test passed: score=" + class1.getScore() + ", lines=" + class1.getClearedLines() + ", level=" + class1.getLevel());
    }

    private static void runRenderSmokeTest() {
        TetrisModel class1 = new TetrisModel(new Random(20260828L));
        if (class1.isGameOver() || class1.getCurrentColor() <= 0) {
            throw new IllegalStateException("Tetris render model has no active piece: gameOver=" + class1.isGameOver() + ", color=" + class1.getCurrentColor());
        }
        if (PALETTE[1] == null || PALETTE[1].getRGB() == PALETTE[0].getRGB()) {
            throw new IllegalStateException("Tetris render palette is invalid");
        }
        GameBoardPanel class10 = new GameBoardPanel(class1);
        class10.setSize(280, 560);
        BufferedImage bufferedImage = new BufferedImage(class10.getWidth(), class10.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = bufferedImage.getGraphics();
        class10.paint(graphics);
        class1.hardDrop();
        class10.paint(graphics);
        graphics.dispose();
        int n = 0;
        for (int i = 0; i < bufferedImage.getHeight(); ++i) {
            block1:
            for (int j = 0; j < bufferedImage.getWidth(); ++j) {
                int n2 = bufferedImage.getRGB(j, i) & 0xFFFFFF;
                for (int k = 1; k < PALETTE.length; ++k) {
                    if (n2 != (PALETTE[k].getRGB() & 0xFFFFFF)) continue;
                    ++n;
                    continue block1;
                }
            }
        }
        if (n < 250) {
            throw new IllegalStateException("Tetris board did not render colored cells: " + n);
        }
        System.out.println("Tetris render smoke test passed: coloredPixels=" + n);
    }

    static final class TetrisModel {
        final private int[][] board = new int[BOARD_ROWS][BOARD_COLUMNS];
        final private Random random;
        private int currentPieceType;
        private int rotation;
        private int pieceX;
        private int pieceY;
        private int nextPieceType;
        private int score;
        private int clearedLines;
        private boolean gameOver;

        TetrisModel(Random random) {
            this.random = random;
            this.reset();
        }

        void reset() {
            for (int i = 0; i < BOARD_ROWS; ++i) {
                for (int j = 0; j < BOARD_COLUMNS; ++j) {
                    this.board[i][j] = 0;
                }
            }
            this.score = 0;
            this.clearedLines = 0;
            this.gameOver = false;
            this.nextPieceType = this.random.nextInt(PIECE_SHAPES.length);
            this.spawnPiece();
        }

        boolean tick() {
            if (this.gameOver) {
                return false;
            }
            if (this.canPlace(this.pieceX, this.pieceY + 1, this.rotation)) {
                ++this.pieceY;
                return false;
            }
            this.lockPiece();
            this.clearCompletedLines();
            this.spawnPiece();
            return true;
        }

        void softDrop() {
            if (!this.tick() && !this.gameOver) {
                ++this.score;
            }
        }

        void hardDrop() {
            int n = 0;
            while (!this.gameOver && this.canPlace(this.pieceX, this.pieceY + 1, this.rotation)) {
                ++this.pieceY;
                ++n;
            }
            this.score += n * 2;
            this.tick();
        }

        void moveHorizontal(int n) {
            if (!this.gameOver && this.canPlace(this.pieceX + n, this.pieceY, this.rotation)) {
                this.pieceX += n;
            }
        }

        void rotatePiece() {
            if (this.gameOver) {
                return;
            }
            int n = (this.rotation + 1) % PIECE_SHAPES[this.currentPieceType].length;
            if (this.canPlace(this.pieceX, this.pieceY, n)) {
                this.rotation = n;
            } else if (this.canPlace(this.pieceX - 1, this.pieceY, n)) {
                --this.pieceX;
                this.rotation = n;
            } else if (this.canPlace(this.pieceX + 1, this.pieceY, n)) {
                ++this.pieceX;
                this.rotation = n;
            }
        }

        private void spawnPiece() {
            this.currentPieceType = this.nextPieceType;
            this.nextPieceType = this.random.nextInt(PIECE_SHAPES.length);
            this.rotation = 0;
            this.pieceX = 3;
            this.pieceY = 0;
            this.gameOver = !this.canPlace(this.pieceX, this.pieceY, this.rotation);
        }

        private boolean canPlace(int n, int n2, int n3) {
            for (int[] nArray : PIECE_SHAPES[this.currentPieceType][n3]) {
                int n4 = n + nArray[0];
                int n5 = n2 + nArray[1];
                if (n4 >= 0 && n4 < BOARD_COLUMNS && n5 >= 0 && n5 < BOARD_ROWS && this.board[n5][n4] == 0) continue;
                return false;
            }
            return true;
        }

        private void lockPiece() {
            for (int[] nArray : this.getCurrentShape()) {
                this.board[this.pieceY + nArray[1]][this.pieceX + nArray[0]] = this.currentPieceType + 1;
            }
        }

        private void clearCompletedLines() {
            int n = 0;
            for (int i = 19; i >= 0; --i) {
                int n2;
                boolean bl = true;
                for (n2 = 0; n2 < 10; ++n2) {
                    if (this.board[i][n2] != 0) continue;
                    bl = false;
                    break;
                }
                if (!bl) continue;
                for (n2 = i; n2 > 0; --n2) {
                    System.arraycopy(this.board[n2 - 1], 0, this.board[n2], 0, 10);
                }
                Arrays.fill(this.board[0], 0);
                ++n;
                ++i;
            }
            if (n > 0) {
                this.clearedLines += n;
                int[] nArray = new int[]{0, 100, 300, 500, 800};
                this.score += nArray[n] * this.getLevel();
            }
        }

        void validateState() {
            for (int i = 0; i < BOARD_ROWS; ++i) {
                for (int j = 0; j < BOARD_COLUMNS; ++j) {
                    if (this.board[i][j] >= 0 && this.board[i][j] < PALETTE.length) continue;
                    throw new IllegalStateException("Invalid board cell at " + j + "," + i);
                }
            }
            if (!this.gameOver && !this.canPlace(this.pieceX, this.pieceY, this.rotation)) {
                throw new IllegalStateException("Active piece overlaps the board");
            }
        }

        int getCell(int n, int n2) {
            return this.board[n2][n];
        }

        int[][] getCurrentShape() {
            return PIECE_SHAPES[this.currentPieceType][this.rotation];
        }

        int getPieceX() {
            return this.pieceX;
        }

        int getPieceY() {
            return this.pieceY;
        }

        int getCurrentColor() {
            return this.currentPieceType + 1;
        }

        int getNextPieceType() {
            return this.nextPieceType;
        }

        int getScore() {
            return this.score;
        }

        int getClearedLines() {
            return this.clearedLines;
        }

        int getLevel() {
            return this.clearedLines / 10 + 1;
        }

        int getDropDelay() {
            return Math.max(110, 700 - (this.getLevel() - 1) * 55);
        }

        boolean isGameOver() {
            return this.gameOver;
        }
    }

    static class LaunchTask
            implements Runnable {
        LaunchTask() {
        }

        @Override
        public void run() {
            new TetrisWindow().setVisible(true);
        }
    }

    record PauseTask(TetrisWindow this$0)
                implements Runnable {

        @Override
            public void run() {
                this.this$0.togglePause();
            }
        }

    record RestartTask(TetrisWindow this$0)
                implements Runnable {

        @Override
            public void run() {
                this.this$0.model.reset();
                this.this$0.paused = false;
                this.this$0.timer.start();
            }
        }

    record MoveLeftTask(TetrisWindow this$0)
                implements Runnable {

        @Override
            public void run() {
                this.this$0.model.moveHorizontal(-1);
            }
        }

    record MoveRightTask(TetrisWindow this$0)
                implements Runnable {

        @Override
            public void run() {
                this.this$0.model.moveHorizontal(1);
            }
        }

    record SoftDropTask(TetrisWindow this$0)
                implements Runnable {

        @Override
            public void run() {
                this.this$0.model.softDrop();
            }
        }

    record RotateTask(TetrisWindow this$0)
                implements Runnable {

        @Override
            public void run() {
                this.this$0.model.rotatePiece();
            }
        }

    record HardDropTask(TetrisWindow this$0)
                implements Runnable {

        @Override
            public void run() {
                this.this$0.model.hardDrop();
            }
        }

    static final class GameBoardPanel
            extends JPanel {
        final private TetrisModel model;

        GameBoardPanel(TetrisModel class1) {
            this.model = class1;
            this.setPreferredSize(new Dimension(BOARD_COLUMNS * CELL_SIZE, BOARD_ROWS * CELL_SIZE));
            this.setBackground(PALETTE[0]);
            this.setBorder(BorderFactory.createLineBorder(new Color(90, 94, 105), 2));
            this.setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            for (int i = 0; i < BOARD_ROWS; ++i) {
                for (int j = 0; j < BOARD_COLUMNS; ++j) {
                    this.drawCell(graphics, j, i, this.model.getCell(j, i));
                }
            }
            if (!this.model.isGameOver()) {
                for (int[] nArray : this.model.getCurrentShape()) {
                    this.drawCell(graphics, this.model.getPieceX() + nArray[0], this.model.getPieceY() + nArray[1], this.model.getCurrentColor());
                }
            }
        }

        private void drawCell(Graphics graphics, int n, int n2, int n3) {
            int n4 = n * CELL_SIZE;
            int n5 = n2 * CELL_SIZE;
            graphics.setColor(PALETTE[n3]);
            graphics.fillRect(n4, n5, CELL_SIZE, CELL_SIZE);
            graphics.setColor(n3 == 0 ? new Color(53, 56, 65) : PALETTE[n3].brighter());
            graphics.drawRect(n4, n5, CELL_SIZE - 1, CELL_SIZE - 1);
        }
    }

    static final class NextPiecePanel
            extends JPanel {
        static final private int PREVIEW_CELL_SIZE = 20;
        final private TetrisModel model;

        NextPiecePanel(TetrisModel class1) {
            this.model = class1;
            this.setPreferredSize(new Dimension(140, 92));
            this.setBackground(PALETTE[0]);
            this.setBorder(BorderFactory.createLineBorder(new Color(90, 94, 105)));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            for (int[] nArray2 : PIECE_SHAPES[this.model.getNextPieceType()][0]) {
                int n = 30 + nArray2[0] * 20;
                int n2 = 12 + nArray2[1] * 20;
                graphics.setColor(PALETTE[this.model.getNextPieceType() + 1]);
                graphics.fillRect(n, n2, 20, 20);
                graphics.setColor(PALETTE[this.model.getNextPieceType() + 1].brighter());
                graphics.drawRect(n, n2, 19, 19);
            }
        }
    }

    static final class TetrisWindow
            extends JFrame {
        final TetrisModel model = new TetrisModel(new Random());
        final private GameBoardPanel boardPanel = new GameBoardPanel(this.model);
        final private NextPiecePanel nextPiecePanel = new NextPiecePanel(this.model);
        final private JLabel scoreLabel = this.createValueLabel();
        final private JLabel linesLabel = this.createValueLabel();
        final private JLabel levelLabel = this.createValueLabel();
        final private JLabel statusLabel = new JLabel("Playing", 0);
        final Timer timer;
        boolean paused;

        TetrisWindow() {
            super("Tetris - Obfuscation Check");
            this.setDefaultCloseOperation(3);
            this.setResizable(false);
            this.setLayout(new BorderLayout(12, 12));
            this.add(this.boardPanel, "Center");
            this.add(this.createSidePanel(), "East");
            this.bindControls();
            this.timer = new Timer(this.model.getDropDelay(), new GameTimerAction(this));
            this.timer.start();
            this.refreshDisplay();
            this.pack();
            this.setLocationByPlatform(true);
        }

        private JPanel createSidePanel() {
            JPanel jPanel = new JPanel(new BorderLayout(0, 10));
            jPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 8));
            jPanel.setPreferredSize(new Dimension(170, 560));
            JLabel jLabel = new JLabel("TETRIS", 0);
            jLabel.setFont(new Font("SansSerif", 1, 22));
            jPanel.add(jLabel, "North");
            JPanel jPanel2 = new JPanel(new GridLayout(0, 1, 0, 6));
            jPanel2.add(new JLabel("Next", 0));
            jPanel2.add(this.nextPiecePanel);
            jPanel2.add(this.createStatRow("Score", this.scoreLabel));
            jPanel2.add(this.createStatRow("Lines", this.linesLabel));
            jPanel2.add(this.createStatRow("Level", this.levelLabel));
            this.statusLabel.setFont(new Font("SansSerif", 1, 13));
            jPanel2.add(this.statusLabel);
            jPanel.add(jPanel2, "Center");
            JPanel jPanel3 = new JPanel(new GridLayout(1, 2, 6, 0));
            JButton jButton = new JButton("Pause");
            jButton.addActionListener(new PauseButtonAction(this, jButton));
            JButton jButton2 = new JButton("Restart");
            jButton2.addActionListener(new RestartButtonAction(this, jButton));
            jPanel3.add(jButton);
            jPanel3.add(jButton2);
            jPanel.add(jPanel3, "South");
            return jPanel;
        }

        private JPanel createStatRow(String string, JLabel jLabel) {
            JPanel jPanel = new JPanel(new BorderLayout());
            jPanel.add(new JLabel(string), "West");
            jPanel.add(jLabel, "East");
            return jPanel;
        }

        private JLabel createValueLabel() {
            JLabel jLabel = new JLabel("0", 4);
            jLabel.setFont(new Font("Monospaced", 1, 15));
            return jLabel;
        }

        private void bindControls() {
            this.bindGameKey(37, "left", new MoveLeftTask(this));
            this.bindGameKey(39, "right", new MoveRightTask(this));
            this.bindGameKey(40, "down", new SoftDropTask(this));
            this.bindGameKey(38, "rotate", new RotateTask(this));
            this.bindGameKey(32, "drop", new HardDropTask(this));
            this.bindMenuKey(80, "pause", new PauseTask(this));
            this.bindMenuKey(82, "restart", new RestartTask(this));
        }

        private void bindGameKey(int n, String string, Runnable runnable) {
            this.boardPanel.getInputMap(2).put(KeyStroke.getKeyStroke(n, 0), string);
            this.boardPanel.getActionMap().put(string, new GameInputAction(this, runnable));
        }

        private void bindMenuKey(int n, String string, Runnable runnable) {
            this.boardPanel.getInputMap(2).put(KeyStroke.getKeyStroke(n, 0), string);
            this.boardPanel.getActionMap().put(string, new MenuInputAction(this, runnable));
        }

        void togglePause() {
            if (this.model.isGameOver()) {
                return;
            }
            boolean bl = this.paused = !this.paused;
            if (this.paused) {
                this.timer.stop();
            } else {
                this.timer.start();
            }
            this.refreshDisplay();
        }

        void onTimerTick() {
            if (this.model.tick()) {
                this.timer.setDelay(this.model.getDropDelay());
            }
            this.refreshDisplay();
        }

        void refreshDisplay() {
            this.scoreLabel.setText(Integer.toString(this.model.getScore()));
            this.linesLabel.setText(Integer.toString(this.model.getClearedLines()));
            this.levelLabel.setText(Integer.toString(this.model.getLevel()));
            this.statusLabel.setText(this.model.isGameOver() ? "Game over" : (this.paused ? "Paused" : "Playing"));
            this.boardPanel.repaint();
            this.nextPiecePanel.repaint();
        }
    }

    static class GameTimerAction
            extends AbstractAction {
        final TetrisWindow this$0;

        GameTimerAction(TetrisWindow class12) {
            this.this$0 = class12;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            this.this$0.onTimerTick();
        }
    }

    static class GameInputAction
            extends AbstractAction {
        final Runnable val$command;
        final TetrisWindow this$0;

        GameInputAction(TetrisWindow class12, Runnable runnable) {
            this.val$command = runnable;
            this.this$0 = class12;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if (!this.this$0.paused && !this.this$0.model.isGameOver()) {
                this.val$command.run();
                this.this$0.refreshDisplay();
            }
        }
    }

    static class MenuInputAction
            extends AbstractAction {
        final Runnable val$command;
        final TetrisWindow this$0;

        MenuInputAction(TetrisWindow class12, Runnable runnable) {
            this.val$command = runnable;
            this.this$0 = class12;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            this.val$command.run();
            this.this$0.refreshDisplay();
        }
    }

    static class PauseButtonAction
            extends AbstractAction {
        final JButton val$pause;
        final TetrisWindow this$0;

        PauseButtonAction(TetrisWindow class12, JButton jButton) {
            this.val$pause = jButton;
            this.this$0 = class12;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            this.this$0.togglePause();
            this.val$pause.setText(this.this$0.paused ? "Resume" : "Pause");
        }
    }

    static class RestartButtonAction
            extends AbstractAction {
        final JButton val$pause;
        final TetrisWindow this$0;

        RestartButtonAction(TetrisWindow class12, JButton jButton) {
            this.val$pause = jButton;
            this.this$0 = class12;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            this.this$0.model.reset();
            this.this$0.paused = false;
            this.this$0.timer.start();
            this.val$pause.setText("Pause");
            this.this$0.refreshDisplay();
        }
    }
}
