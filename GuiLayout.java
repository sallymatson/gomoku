package gomoku;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.io.*;
import java.nio.charset.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;


class GuiLayout extends JFrame implements KeyListener, ActionListener, MouseListener {

    private GomokuClient client;
    private JPanel panelControl;
    private JPanel panelGame;
    private JPanel panelGomoku;
    private JPanel panelChat;

    // panelControl components
    private JButton buttonJoinGame;
    private JButton buttonGiveUp;
    private JButton buttonReset;

    // panelGomoku components
    private ArrayList<JButton> buttonList = new ArrayList<JButton>();

    // panelChat components
    private TextArea chatArea;
    private TextArea typeArea;
    private JButton sendButton;

    private final int boardWidth = 15;

    public GuiLayout(GomokuClient client) {
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

        buttonJoinGame = new JButton("Join Game");
        buttonJoinGame.addMouseListener(this);
        panelControl.add(buttonJoinGame);

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
        panelGomoku = new JPanel();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0;
        constraints.weighty = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        panelGame.add(panelGomoku, constraints);

        // panelGomoku.setLayout( new GridLayout( 1, 1, 0, 0) );
//        for (int i = 0; i < 15*15; i++) {
//            JButton button = new JButton(Integer.toString(i));
//            button.setBorderPainted(false);
//            button.setFocusPainted(false);
//            button.setMaximumSize(new Dimension(10, 10));
//            button.setMinimumSize(new Dimension(10, 10));
//            // button.setContentAreaFilled(false);
//            button.addMouseListener(this);
//            // button.setBorder(new LineBorder(Color.BLACK));
//            button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
//            buttonList.add(button);
//            panelGomoku.add(button);
//        }
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

    // MouseListener methods
    public void mouseClicked(MouseEvent mouseEvent) {
        try {
            JButton button = (JButton) (mouseEvent.getSource());
            if (button == buttonJoinGame) {
                // attempt to join a new game
            } else if (button == buttonReset) {
                // reset the game
            } else if (button == buttonGiveUp) {
                client.quit();
            } else if (buttonList.contains(button)) {
                int col = mouseEvent.getX();
                int row = mouseEvent.getY();
                client.placeGamePiece(col, row);
            }
        }
        catch (Exception ex) {
            System.out.println("Not a JButton dude");
        }

//        ImageIcon tileImage = new ImageIcon("tile.png");
//        JLabel tileLabel = new JLabel("", tileImage, JLabel.HORIZONTAL);
//        panelGomoku.add(tileLabel);
//        panelGomoku.revalidate();
//        panelGomoku.repaint();
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
            client.sendMessage(text);
            // clear text
            typeArea.setText("");
        }
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics); // call to JFrame paint()
        drawTiles(graphics);
    }

    private void drawTiles(Graphics graphics) {
        graphics.setColor(client.getColor());
        graphics.fillOval(7, 244, 28, 28);
    }
}
