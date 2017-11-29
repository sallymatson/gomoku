package gomoku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.io.*;
import java.nio.charset.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.LineBorder;


class GuiLayout extends JFrame implements KeyListener, ActionListener, MouseListener {

    private GomokuClient client;
    private JPanel panelControl;
    private JPanel panelGomoku;
    private JPanel panelChat;

    // panelControl components
    private JButton buttonJoinGame;
    private JButton buttonGiveUp;

    // panelGomoku components
    private ArrayList<JButton> buttonList = new ArrayList<JButton>();

    // panelChat components
    private TextArea chatArea;
    private TextArea typeArea;
    private JButton sendButton;

    private final int boardWidth = 15;

    public GuiLayout(GomokuClient client) {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        setUpPanelControl();
        setUpPanelGomoku();
        setUpPanelChat();

        setTitle("Gomoku");
        setSize(new Dimension(640, 480));
        setVisible(true);

        this.client = client;
    }

    private void setUpPanelControl() {
        panelControl = new JPanel();
        add(panelControl, BorderLayout.LINE_START);
        panelControl.setLayout(new BoxLayout(panelControl, BoxLayout.Y_AXIS));

        buttonJoinGame = new JButton("Join Game");
        buttonJoinGame.addMouseListener(this);
        panelControl.add(buttonJoinGame);

        buttonGiveUp = new JButton("Give Up");
        buttonGiveUp.addMouseListener(this);
        panelControl.add(buttonGiveUp);
    }

    private void setUpPanelGomoku() {
        panelGomoku = new JPanel();
        add(panelGomoku, BorderLayout.CENTER);
        panelGomoku.setLayout( new GridLayout( 15, 15, 0, 0) );
        for (int i = 0; i < 15*15; i++) {
            JButton button = new JButton(Integer.toString(i));
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setMaximumSize(new Dimension(30, 30));
            button.setMinimumSize(new Dimension(30, 30));
            button.setContentAreaFilled(false);
            button.addMouseListener(this);
            //button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            buttonList.add(button);
            panelGomoku.add(button);
        }
    }


    private void setUpPanelChat() {
        panelChat = new JPanel();
        add(panelChat, BorderLayout.LINE_END);
        panelChat.setLayout(new GridBagLayout());
        panelChat.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;

        // add chat area
        chatArea = new TextArea("", 1, 1, TextArea.SCROLLBARS_VERTICAL_ONLY);
        chatArea.setEditable(false);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.weighty = 0.9;
        panelChat.add(chatArea, constraints);

        // add text area
        typeArea = new TextArea("", 1, 1, TextArea.SCROLLBARS_VERTICAL_ONLY);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 0.9;
        constraints.weighty = 0.1;
        panelChat.add(typeArea, constraints);
        typeArea.addKeyListener(this);

        // add send button
        sendButton = new JButton("Send");
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 0.1;
        constraints.weighty = 0.1;
        panelChat.add(sendButton, constraints);
        sendButton.addActionListener(this);
    }

    // MouseListener methods
    public void mouseClicked(MouseEvent MouseEvent) {
        JButton button = (JButton)(MouseEvent.getSource());
        if (button == buttonGiveUp) {
            client.quit();
        } else if (buttonList.contains(button)) {
            client.placeGamePiece(button.getText());
            // client.placeGamePiece(row, col);
            // get row, col from MouseEvent.getSource(), or maybe from mouse position?
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
            client.sendMessage(text);
            // clear text
            typeArea.setText("");
        }
    }
}
