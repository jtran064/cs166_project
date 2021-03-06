import javax.swing.*;
import java.awt.GridLayout;
import java.awt.event.*;
import java.util.*;

public class UIContactList extends UIAbstractList implements ActionListener
{
    protected ImageIcon userIcon = new ImageIcon("images/user_32.png");

    UIContactList(){
        super();
    }

    protected void setList(){
        list = esql.ListContacts();
    }

    protected String htmlFormatter(int index){
        return String.format("<html><b>%s</b><br />%s</html>", list.get(index).get(0).trim(), list.get(index).get(1).trim());
    }

    protected void customizeButton(JButton button){
    }

    protected void setAllActionListeners(){
        for(JButton a: users){
            a.addActionListener(this);
        }
    }

    @Override
    protected ImageIcon imageSelector(int index) {
        return userIcon;
    }

    public void actionPerformed(ActionEvent e){
        String cmd = e.getActionCommand();
        CardLayoutPanel.setContactUsername(list.get(Integer.parseInt(cmd)).get(0));
    }

}
