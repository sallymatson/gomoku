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
    private ArrayList<JButton> buttonList = new ArrayList<JButton>();
    private JButton buttonGiveUp;

    private final int boardWidth = 15;

    public GuiLayout(GomokuClient client) {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        setTitle("Gomoku");
        setupPanelControl();
        setUpPanelGomoku();
        setUpPanelChat();

        setSize(new Dimension(640, 480));
        setVisible(true);

        this.client = client;
    }

    private void setupPanelControl() {
        panelControl = new JPanel();
        add(panelControl, BorderLayout.LINE_START);
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
        // notify client
        // client.sendMessage(text in box);
    }
    public void keyReleased(KeyEvent keyEvent) {}
    public void keyTyped(KeyEvent keyEvent) {}

    // ActionListener methods
    public void actionPerformed(ActionEvent actionEvent) {}
}
