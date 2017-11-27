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

    private JPanel panelGomoku;
    private JPanel panelChat;
    private ArrayList<JButton> buttonList = new ArrayList<JButton>();

    public GuiLayout() {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        setTitle("Gomoku");
        setUpPanelGomoku();
        setUpPanelChat();

        setSize(new Dimension(640, 480));
        setVisible(true);
    }

    private void setUpPanelGomoku() {
        System.out.println("HI");
        panelGomoku = new JPanel();
        add(panelGomoku, BorderLayout.LINE_END);
        panelGomoku.setLayout( new GridLayout( 15, 15, 0, 0) );
        for (int i=0; i<15*15; i++ ) {
            JButton button = new JButton(""+i);
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
    public void mouseClicked(MouseEvent MouseEvent){
        System.out.println(MouseEvent.getSource());
    };
    public void mouseEntered(MouseEvent MouseEvent){};
    public void mouseExited(MouseEvent MouseEvent){};
    public void mousePressed(MouseEvent MouseEvent){};
    public void mouseReleased(MouseEvent MouseEvent){};

    // KeyListener methods
    public void keyPressed(KeyEvent keyEvent) { }
    public void keyReleased(KeyEvent keyEvent) { }
    public void keyTyped(KeyEvent keyEvent) { }

    // ActionListener methods
    public void actionPerformed(ActionEvent actionEvent) { }
}
