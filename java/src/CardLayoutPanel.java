import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CardLayoutPanel implements ActionListener{
    private CardLayoutPanelListener listener;
    private static JPanel cards;
    private static CardLayout cardLayout = new CardLayout();
    private JButton removeC, removeB, newMessage, block;
    private static String contactUsername = "none";
    private static String blockUsername = "none";
    private static String chatId= null;
    private static JLabel contactLabel = new JLabel(contactUsername);
    private static JLabel blockLabel = new JLabel(blockUsername);
    private static JPanel contactPanel, blockPanel, messagePanel;
    private static JList messageList;
    private JTextField textField;
    private static JScrollBar scrollBar;
    private JPanel topStatus;
    private static JButton picLabel;
    private static JTable table;
    private static JButton addToChat;
    private static JButton removeFromChat, refreshAll;
    private static Random rand;
    private Action action = new AbstractAction() {
        public void actionPerformed(ActionEvent e){
            String text = textField.getText();
            if(chatId != null){
                MessengerGui.getInstance().AddNewMessageToChat(text, chatId);
                messageList.setListData(MessengerGui.getInstance().GetAllMessagesInChat(chatId).toArray());
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run() {
                        scrollBar.setValue(scrollBar.getMaximum());
                    }
                });
                textField.setText("");
            }else {
                JOptionPane.showMessageDialog(cards, "No chat is selected. Please select or create new chat.",
                        "No selected chat", JOptionPane.WARNING_MESSAGE);
            }
        }
    };

    public static int randInt(int min, int max){
        rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    public CardLayoutPanel() {
        removeC = new JButton("Remove From Contacts");
        removeB = new JButton("Remove From Blocked");
        newMessage = new JButton("New Message");

        removeC.addActionListener(this);
        removeB.addActionListener(this);
        newMessage.addActionListener(this);

        picLabel = new JButton(Mainframe.userImages[randInt(0, 6)]);

        cards = new JPanel(cardLayout);
        messagePanel = makeMessagePanel();
        contactPanel = makeContactPanel();
        blockPanel = makeBlockPanel();

        cards.add(messagePanel, "messagePanel");
        cards.add(contactPanel, "contactPanel");
        cards.add(blockPanel, "blockPanel");
    }

    public static String getChatId() throws Exception{
        if(chatId != null){
            return chatId;
        }else {
            throw new Exception("No chat is selected. Please select or create new chat.");
        }
    }

    public static void setChatId(String s){
        chatId = s;
    }

    private JPanel makeContactPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        panel.add(contactLabel, c);
        c.gridy++;
//        panel.add(new JButton("TEST"), c);
        panel.add(picLabel, c);
        c.gridy++;
        panel.add(removeC, c);
        c.gridy++;
        panel.add(newMessage, c);
        return panel;
    }

    private JPanel makeBlockPanel(){
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = c.gridy = 0;
        panel.add(blockLabel, c);
        c.gridy++;
        panel.add(picLabel, c);
        c.gridy++;
        panel.add(removeB, c);
        return panel;
    }

    private JPanel makeMessagePanel(){
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(0,0));

        //messageList
        Vector<String> vector = new Vector<String>();
        messageList = new JList();
        messageList.setCellRenderer(new MyCellRenderer(300));


        //scrollPane
        JScrollPane scrollPane = new JScrollPane(messageList);
        scrollBar = scrollPane.getVerticalScrollBar();

        //splitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, scrollPane, makeTextInputPanel());
        splitPane.setOpaque(false);
        splitPane.setDividerLocation(200);

        //Message and status
        FlowLayout fl = new FlowLayout();
        topStatus = new JPanel(fl);
        topStatus.setOpaque(false);
        fl.setAlignment(FlowLayout.TRAILING);
        topStatus.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        ImageIcon add = new ImageIcon("images/add_16.gif");
//        ImageIcon add = new ImageIcon("images/user1.png");
        ImageIcon remove = new ImageIcon("images/block_16.gif");
        ImageIcon refresh = new ImageIcon("images/refresh_16.png");

        addToChat = new JButton(add);
        removeFromChat = new JButton(remove);
        refreshAll = new JButton(refresh);

        addToChat.setVisible(false);
        removeFromChat.setVisible(false);
        refreshAll.setVisible(true);

        addToChat.addActionListener(this);
        removeFromChat.addActionListener(this);
        refreshAll.addActionListener(this);

        topStatus.add(removeFromChat);
        topStatus.add(addToChat);
        topStatus.add(refreshAll);
//        String[] column = {"1", "2", "3"};
//        String[][] data = {{"a", "a2", "a3"}};
//        table = new JTable(data, column);
//        table.setPreferredScrollableViewportSize(table.getPreferredSize());
//        table.setTableHeader(null);
//
//        JButton btn = new JButton();
//        JScrollPane pane = new JScrollPane(table);

//        topStatus.add(pane, BorderLayout.CENTER);
//        topStatus.add(btn, BorderLayout.LINE_END);

        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(topStatus, BorderLayout.PAGE_START);
        return panel;
    }

    private JPanel makeTextInputPanel(){
        JPanel panel = new JPanel();
        panel.setOpaque(false);

        JButton button = new JButton(new ImageIcon("images/mail_48.png"));
        button.addActionListener(action);

        textField = new JTextField();
        textField.addActionListener(action);

        panel.setLayout(new BorderLayout());
        panel.add(textField, BorderLayout.CENTER);
        panel.add(button, BorderLayout.LINE_END);

        return panel;
    }

    public static void setContactUsername(String name){
        contactUsername = name;
        picLabel.setIcon(Mainframe.userImages[randInt(0, 6)]);
        picLabel.repaint();
        contactLabel.setText(name);
        contactLabel.repaint();
        cardLayout.show(cards, "contactPanel");
    }

    public static void setBlockUsername(String name){
        blockUsername = name;
        picLabel.setIcon(Mainframe.userImages[randInt(0, 6)]);
        picLabel.repaint();
        blockLabel.setText(name);
        blockPanel.repaint();
        cardLayout.show(cards, "blockPanel");
    }

    public static void setMessageList(String s){
        chatId = s;
        if(MessengerGui.getInstance().IsInitSender(s)){
            addToChat.setVisible(true);
            removeFromChat.setVisible(true);
        }else{
            addToChat.setVisible(false);
            removeFromChat.setVisible(false);
        }
        List<List<String>> tmp = MessengerGui.getInstance().GetAllMessagesInChat(s);
//        System.out.println(tmp.size());
        if(tmp == null){
            //new message, so no data
            return;
        }
        messageList.setListData(tmp.toArray());
        messageList.validate();
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                scrollBar.setValue(scrollBar.getMaximum());
            }
        });
//        scrollBar.setValue(scrollBar.getMaximum());
//        List<List<String>> tmp = MessengerGui.getInstance().AllUsersInChat(s);
//        int size = tmp.get(0).size();
//        String[][] data = new String[1][size];
//        for(int i = 0; i < size; i++){
//            data[0][i] = tmp.get(i).get(0);
//        }
//        DefaultTableModel model = new DefaultTableModel(data, new Object[size]);
//        table.setModel(model);
//        model.fireTableDataChanged();
        cardLayout.show(cards, "messagePanel");
    }

    public static void clearMessageList(){
        messageList.setListData(new Object[0]);
        setChatId(null);
        cardLayout.show(cards, "messagePanel");
    }

    public JPanel getPanel() {
        return cards;
    }

    public void setListener(CardLayoutPanelListener listener){
        this.listener = listener;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == removeC){
            System.out.println("Remove Contact button clicked");
            listener.removeContactEventOccurred(contactUsername);
        } else if (e.getSource() == removeB){
            System.out.println("Remove Blocked button clicked");
            listener.removeBlockEventOccurred(blockUsername);
        }else if (e.getSource() == newMessage){
            System.out.println("New Message clicked");
            listener.newMessageEventOccurred(contactUsername);
            cardLayout.show(cards, "messagePanel");
        }else if (e.getSource() == addToChat){
            System.out.println("Add user to chat");
            try {
                listener.addUserToChatEventOccurred(getChatId());
            } catch (Exception ex){
                JOptionPane.showMessageDialog(cards, ex.getMessage(),
                        "No selected chat", JOptionPane.WARNING_MESSAGE);
            }
            cardLayout.show(cards, "messagePanel");
        }else if (e.getSource() == removeFromChat){
            System.out.println("Remove user from chat");
            try{
                listener.removeUserFromChatEventOccurred(getChatId());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(cards, ex.getMessage(),
                        "No selected chat", JOptionPane.WARNING_MESSAGE);
                cardLayout.show(cards, "messagePanel");
            }
        }else if(e.getSource() == refreshAll){
            listener.refreshAllEventOccurred();
            try {
                setMessageList(getChatId());
            } catch (Exception e1) {
                System.err.println(e1.getMessage());
            }
        }
    }

    class MyCellRenderer extends DefaultListCellRenderer {
        public static final String HTML_1 = "<html><body style='width: ";
        public static final String HTML_2 = "px'>";
        private int width;

        public MyCellRenderer(int width) {
            this.width = width;
        }

        private String htmlFormatterMessage(List<String> l){
            String html = HTML_1 + String.valueOf(width) + "\n" + HTML_2;
            html += String.format("<b>%s</b> <small><em>%s:</small></em><br>%s\n", l.get(3), l.get(2), l.get(1));
            html += "</html>";
            return html;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
//            String text = HTML_1 + String.valueOf(width) + HTML_2 + value.toString()
//                    + HTML_3;
//                    cellHasFocus);
            String text = htmlFormatterMessage((List<String>)value);
//            setText(value.toString());

            Component c = super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            if(index % 2 == 0) {
                c.setBackground(Color.pink);
            } else {
                c.setBackground(Color.white);
            }

//            return this;
            return c;
        }

//        public Component getListCellRendererComponent(JList list, List<String> value,
//                                                      int index, boolean isSelected, boolean cellHasFocus) {
////            String text = HTML_1 + String.valueOf(width) + HTML_2 + value.toString()
////                    + HTML_3;
////            return super.getListCellRendererComponent(list, text, index, isSelected,
////                    cellHasFocus);
//            String text = htmlFormatterMessage(value);
//            System.out.println(text);
////            if(index % 2 == 0) setBackground(Color.pink);
////            else setBackground(Color.white);
//
////            return this;
//            return super.getListCellRendererComponent(list, text, index, isSelected,
//                    cellHasFocus);
//        }

    }
}
