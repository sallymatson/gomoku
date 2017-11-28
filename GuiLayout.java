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
    private JPanel panelGomoku;
    private JPanel panelChat;
    private ArrayList<JButton> buttonList = new ArrayList<JButton>();

    public GuiLayout(GomokuClient client) {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        setTitle("Gomoku");
        setUpPanelGomoku();
        setUpPanelChat();

        setSize(new Dimension(640, 480));
        setVisible(true);

        this.client = client;
    }

    private void setUpPanelGomoku() {
        panelGomoku = new JPanel();
        add(panelGomoku, BorderLayout.LINE_END);
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
        add(panelChat, BorderLayout.CENTER);
    }

    // MouseListener methods
    public void mouseClicked(MouseEvent MouseEvent) {
        JButton button = (JButton)(MouseEvent.getSource());
        client.placeGamePiece(button.getText());
        // notify client
        // client.placeGamePiece(row, col); - get row, col from MouseEvent.getSource() ?
        // or from mouseX mouseY
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
