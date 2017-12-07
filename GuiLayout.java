package gomoku;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.nio.charset.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;


class GomokuJPanel extends JPanel {
    private Image whiteTileImage, blackTileImage;
    private BufferedImage bufferImage;
    private int gameboard[][] = new int[15][15];

    // tile placement constants
    private final int boardWidth = 15;
    private final int cellWidth = 35;
    private final int tileWidth = 20;
    private final int horizontalOffset = 9;
    private final int verticalOffset = 12;

    public GomokuJPanel(int[][] gameboard) {
        this.gameboard = gameboard;
        bufferImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);

        try {
            whiteTileImage = ImageIO.read(new File("white_tile.png"));
            blackTileImage = ImageIO.read(new File("black_tile.png"));
        } catch (IOException e) {
            System.out.println("Could not load tile image");
        }
    }

    @Override
    public void paint(Graphics graphics) {
        Graphics graphicsBuffer = bufferImage.createGraphics();
        super.paint(graphicsBuffer); // call to JFrame paint()
        drawTiles(graphicsBuffer);
        graphics.drawImage(bufferImage, 0, 0, null);
    }

    private void drawTiles(Graphics graphics) {
        for (int row = 0; row < boardWidth; row++) {
            for (int col = 0; col < boardWidth; col++) {
                if (gameboard[row][col] == 1) {
                    System.out.println("drawing white tile at " + row + ", " + col);
                    // draw a white oval
                    int tileX = horizontalOffset + col * cellWidth;
                    int tileY = verticalOffset + row * cellWidth;
                    graphics.drawImage(whiteTileImage, tileX, tileY, null);
                }
                else if (gameboard[row][col] == 2) {
                    System.out.println("drawing black tile at " + row + ", " + col);
                    // draw a black oval
                    int tileX = horizontalOffset + col * cellWidth;
                    int tileY = verticalOffset + row * cellWidth;
                    graphics.drawImage(blackTileImage, tileX, tileY, null);
                }
            }
        }
    }
}

class GuiLayout extends JFrame implements KeyListener, ActionListener, MouseListener {

    private GomokuClient client;
    private JPanel panelControl;
    private JPanel panelGame;
    private GomokuJPanel panelGomoku;
    private JPanel panelChat;

    // panelControl components
    private JButton buttonGiveUp;
    private JButton buttonReset;

    // panelGomoku constants
    private final int boardWidth = 15;
    private final int cellWidth = 35;

    // panelChat components
    private TextArea chatArea;
    private TextArea typeArea;
    private JButton sendButton;

    public int gameboard[][] = new int[15][15];
    public boolean isMyTurn = false;
    private boolean isAI = false;
    private boolean justReset = false;

    public void printGameBoard(){
        for (int i = 0; i<15; i++){
            for (int j = 0; j<15; j++){
                System.out.print(gameboard[i][j]);
            }
            System.out.println();
        }
    }

    public void resetGameBoard(){
        justReset = true;
        for (int i = 0; i<15; i++){
            for (int j = 0; j<15; j++){
                gameboard[i][j] = 0;
            }
        }
    }

    public GuiLayout(GomokuClient client, boolean isAI) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        setUpPanelControl();
        setUpPanelGame();
        setUpPanelGomoku();
        setUpPanelChat();

        setTitle("Gomoku");
        setSize(new Dimension(800, 600));
        setVisible(true);
        setResizable(false);

        this.client = client;
        this.isAI = isAI;
    }

    private void setUpPanelControl() {
        panelControl = new JPanel();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = .05;
        add(panelControl, constraints);
        panelControl.setLayout(new FlowLayout());


        buttonGiveUp = new JButton("Give Up");
        buttonGiveUp.addMouseListener(this);
        panelControl.add(buttonGiveUp);

        buttonReset = new JButton("Reset");
        buttonReset.addMouseListener(this);
        panelControl.add(buttonReset);
    }

    private void setUpPanelGame() {
        panelGame = new JPanel();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = .95;
        add(panelGame, constraints);
        panelGame.setLayout(new GridBagLayout());
    }

    private void setUpPanelGomoku() {
        panelGomoku = new GomokuJPanel(gameboard);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        panelGame.add(panelGomoku, constraints);

        ImageIcon boardImage = new ImageIcon("board_small.png");
        JLabel boardLabel = new JLabel("", boardImage, JLabel.HORIZONTAL);
        boardLabel.addMouseListener(this);
        panelGomoku.add(boardLabel);
    }

    private void setUpPanelChat() {
        panelChat = new JPanel();
        GridBagConstraints constraintsGame = new GridBagConstraints();
        // constraintsGame.insets = new Insets(2, 2, 2, 2);
        constraintsGame.fill = GridBagConstraints.BOTH;
        constraintsGame.weightx = 1;
        constraintsGame.weighty = 1;
        constraintsGame.gridx = 1;
        constraintsGame.gridy = 0;
        panelGame.add(panelChat, constraintsGame);

        panelChat.setLayout(new GridBagLayout());
        panelChat.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        GridBagConstraints constraintsChat = new GridBagConstraints();
        constraintsChat.insets = new Insets(2, 2, 2, 2);
        constraintsChat.fill = GridBagConstraints.BOTH;
        constraintsChat.weightx = 1;

        // add chat area
        chatArea = new TextArea("", 1, 1, TextArea.SCROLLBARS_VERTICAL_ONLY);
        chatArea.setEditable(false);
        constraintsChat.gridx = 0;
        constraintsChat.gridy = 0;
        constraintsChat.gridwidth = 2;
        constraintsChat.weighty = 0.95;
        panelChat.add(chatArea, constraintsChat);

        // add text area
        typeArea = new TextArea("", 1, 1, TextArea.SCROLLBARS_VERTICAL_ONLY);
        constraintsChat.fill = GridBagConstraints.BOTH;
        constraintsChat.gridx = 0;
        constraintsChat.gridy = 1;
        constraintsChat.gridwidth = 1;
        constraintsChat.weightx = 0.9;
        constraintsChat.weighty = 0.05;
        panelChat.add(typeArea, constraintsChat);
        typeArea.addKeyListener(this);

        // add send button
        sendButton = new JButton("Send");
        constraintsChat.fill = GridBagConstraints.BOTH;
        constraintsChat.gridx = 1;
        constraintsChat.gridy = 1;
        constraintsChat.gridwidth = 1;
        constraintsChat.weightx = 0.1;
        constraintsChat.weighty = 0.05;
        panelChat.add(sendButton, constraintsChat);
        sendButton.addActionListener(this);
    }

    // client-accessible methods
    public void startGame(boolean isBlack) {
        isMyTurn = isBlack;
    }

    public void placeGamePiece(int row, int col, int val) {
        if (!justReset) {
            gameboard[row][col] = val + 1; // 0 -> 1 for white, 1 -> 2 for black
            // swap turns
            isMyTurn = !isMyTurn;
            validate();
            repaint();
        } else {
            justReset = false;
        }
    }

    public void chatMessage(String sender, String message) {
        chatArea.setText(chatArea.getText() + sender + ": " + message + '\n');
        chatArea.setCaretPosition(chatArea.getText().length());
    }

    // MouseListener methods
    public void mouseClicked(MouseEvent mouseEvent) {
        if (isAI) {
            return;
        }

        try {
            JButton button = (JButton) (mouseEvent.getSource());
            if (button == buttonReset) {
                client.resetGame();
            } else if (button == buttonGiveUp) {
                client.quit();
            }
        }
        catch (Exception ex) {
            // not a JButton i guess lol
        }

        if (!isMyTurn) {
            return;
        }

        // place game piece
        if (mouseEvent.getX() > 0 &&
            mouseEvent.getX() < (boardWidth * cellWidth) &&
            mouseEvent.getY() > 0 &&
            mouseEvent.getY() < (boardWidth * cellWidth))
        {
            int row = mouseEvent.getY() / cellWidth;
            int col = mouseEvent.getX() / cellWidth;

            if (gameboard[row][col] == 0) {
                client.placeGamePiece(row, col);
            }
        }
    }
    public void mouseEntered(MouseEvent MouseEvent) {}
    public void mouseExited(MouseEvent MouseEvent) {}
    public void mousePressed(MouseEvent MouseEvent) {}
    public void mouseReleased(MouseEvent MouseEvent) {}

    // KeyListener methods
    public void keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getSource() == typeArea) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)  {
                send(typeArea.getText());
                // prevent enter from typing in the box
                keyEvent.consume();
            }
        }
    }
    public void keyReleased(KeyEvent keyEvent) {}
    public void keyTyped(KeyEvent keyEvent) {}

    // ActionListener methods
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == sendButton) {
            send(typeArea.getText());
        }
    }

    private void send(String text) {
        if (!text.isEmpty()) {
            // send the chat message to client to handle
            client.sendChat(text);
            // clear text
            typeArea.setText("");
        }
    }
}
