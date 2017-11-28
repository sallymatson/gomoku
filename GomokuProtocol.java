package gomoku;

public class GomokuProtocol {
    
    private static final String SEPARATOR = "\0";
    private static final String MESSAGE_PLAY = SEPARATOR + "/play";
    private static final String MESSAGE_SET_BLACK = SEPARATOR + "/black";
    private static final String MESSAGE_SET_WHITE = SEPARATOR + "/white";
    private static final String MESSAGE_WIN= SEPARATOR + "/win";
    private static final String MESSAGE_LOSE = SEPARATOR + "/lose";
    private static final String MESSAGE_RESET = SEPARATOR + "/reset";
    private static final String MESSAGE_GIVEUP = SEPARATOR + "/giveup";
    private static final String MESSAGE_CHAT = SEPARATOR + "/chat";
    private static final String MESSAGE_CHANGE_NAME = SEPARATOR + "/nick";
    
    public static String generateChatMessage(String sender, String chat) {
        StringBuilder sb = new StringBuilder();
        sb.append(MESSAGE_CHAT).append(SEPARATOR).append(sender)
                            .append(SEPARATOR).append(chat);
        
        return sb.toString();
    }
    
    public static boolean isChatMessage(String msg) {
        return msg.startsWith(MESSAGE_CHAT);
    }
    
    public static String[] getChatDetail(String msg) {
        if (isChatMessage(msg)) {
            String[] tokens = msg.split(SEPARATOR);
            if (tokens.length >= 4) {
                return new String[]{tokens[2], tokens[3]};
            }
        }
        return null;
    }
    
    public static String generatePlayMessage(boolean isBlack, int row, int col) {
        StringBuilder sb = new StringBuilder();
        sb.append(MESSAGE_PLAY).append(SEPARATOR).append(isBlack ? 1: 0)
                            .append(SEPARATOR).append(row)
                            .append(SEPARATOR).append(col);
        return sb.toString();
    }
    
    public static boolean isPlayMessage(String msg) {
        return msg.startsWith(MESSAGE_PLAY);
    }
    
    public static int[] getPlayDetail(String msg) {
        if (isPlayMessage(msg)) {
            String[] tokens = msg.split(SEPARATOR);
            if (tokens.length >= 5) {
                return new int[]{
                        Integer.parseInt(tokens[2]), 
                        Integer.parseInt(tokens[3]), 
                        Integer.parseInt(tokens[4])};
            }
        }
        return null;
    }
    
    public static String generateChangeNameMessage(String oldName, String newName) {
        StringBuilder sb = new StringBuilder();
        sb.append(MESSAGE_CHANGE_NAME).append(SEPARATOR).append(oldName)
                            .append(SEPARATOR).append(newName);
        return sb.toString();
    }
    
    public static boolean isChangeNameMessage(String msg) {
        return msg.startsWith(MESSAGE_CHANGE_NAME);
    }
    
    public static String[] getChangeNameDetail(String msg) {
        if (isChangeNameMessage(msg)) {
            String[] tokens = msg.split(SEPARATOR);
            if (tokens.length >= 3) {
                return new String[]{tokens[2], tokens[3]};
            }
        }
        return null;
    }
    
    public static String generateSetBlackColorMessage() {
        return MESSAGE_SET_BLACK;
    }
    
    public static boolean isSetBlackColorMessage(String msg) {
        return msg.startsWith(MESSAGE_SET_BLACK);
    }
    
    public static String generateSetWhiteColorMessage() {
        return MESSAGE_SET_WHITE;
    }
    
    public static boolean isSetWhiteColorMessage(String msg) {
        return msg.startsWith(MESSAGE_SET_WHITE);
    }
    
    public static String generateWinMessage() {
        return MESSAGE_WIN;
    }
    
    public static boolean isWinMessage(String msg) {
        return msg.startsWith(MESSAGE_WIN);
    }
    
    public static String generateLoseMessage() {
        return MESSAGE_LOSE;
    }
    
    public static boolean isLoseMessage(String msg) {
        return msg.startsWith(MESSAGE_LOSE);
    }
    
    public static String generateResetMessage() {
        return MESSAGE_RESET;
    }
    
    public static boolean isResetMessage(String msg) {
        return msg.startsWith(MESSAGE_RESET);
    }
    
    public static String generateGiveupMessage() {
        return MESSAGE_GIVEUP;
    }
    
    public static boolean isGiveupMessage(String msg) {
        return msg.startsWith(MESSAGE_GIVEUP);
    }
    
    public static void main(String[] args) {
        // example how to generate a message and how to parse it 
        String msg = generatePlayMessage(true, 10, 4);
        if (isPlayMessage(msg)) {
            int[] detail = getPlayDetail(msg);
            // black is 1 and white is 0
            System.out.println("color is " + detail[0]);
            System.out.println("row is " + detail[1]);
            System.out.println("col is " + detail[2]);
        }
        
        msg = generateChatMessage("Fine Man", "winner winner chicken dinner");
        if (isChatMessage(msg)) {
            String[] detail = getChatDetail(msg);
            // black is 1 and white is 0
            System.out.println("sender is " + detail[0]);
            System.out.println("chat message is " + detail[1]);
        }
        
        msg = generateChangeNameMessage("Fine Man", "Winner Winner");
        if (isChangeNameMessage(msg)) {
            String[] detail = getChangeNameDetail(msg);
            // black is 1 and white is 0
            System.out.println("old name is " + detail[0]);
            System.out.println("new name is " + detail[1]);
        }
    }

}
