package gomoku;

class GomokuClientMain {

    public static void main(String[] args) {

        if(args.length < 2) {
            System.out.println("Please pass host name and port number as command line arguments");
            System.exit(0);
        }

        String host = args[0];
        int portNumber = Integer.parseInt(args[1]);

        GomokuClient client = new GomokuClient();
        client.setupConnection(host, portNumber);
    }
}
