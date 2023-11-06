import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static ArrayList<String> roomIds = new ArrayList<>();
    private Socket socket;
    private String username;
    private Server server;
    private Room currentRoom;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private InputStream inputStream;

    /**
     * Constructor for ClientHandler
     * @param socket from client
     * @param server server that client is connected to
     */
    public ClientHandler(Socket socket, Server server){
        try{
            this.socket = socket;
            this.server = server;

            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.inputStream = socket.getInputStream();

            this.username = bufferedReader.readLine();
            clientHandlers.add(this);

        }
        catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Function to draw 2d int array
     * @param board 2d int array to draw
     */
    public void drawBoard(int[][] board){
        int boardSize = 10;
        for(int i = 0; i<boardSize; i++){
            for(int j = 0; j<boardSize; j++){
                System.out.print(board[i][j] + " ");
            }
            System.out.println(" ");
        }
    }

    /**
     * Function that request message from client by sending him "REQUEST_DATA" message
     */
    public void requestMessage() {
        try {
            bufferedWriter.write("REQUEST_DATA");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Function that reads single line from client
     * @return String message that is send by client
     */
    public String readMessageFromClient() throws IOException {
        return bufferedReader.readLine();
    }

    /**
     * Function that sends message to client that this ClientHandler is handling
     * @param message string message to send
     */
    public void sendMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Function that is used to send message to direct client by passing ClientHandler object
     * @param message string message to send
     * @param clientHandler specified recipient
     */
    public void sendMessageToClient(String message, ClientHandler clientHandler) {
        try {
            clientHandler.bufferedWriter.write(message);
            clientHandler.bufferedWriter.newLine();
            clientHandler.bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
        }
    }

    /**
     * Function that is used to remove clientHandler from clientHandlers array
     */
    public void removeClientHandler(){
        clientHandlers.remove(this);
    }

    /**
     * Function to close socket, bufferedReader, bufferedWriter
     * @param socket to close
     * @param bufferedReader to close
     * @param bufferedWriter to close
     */
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try{
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Function to convert Bytes to 2 dimensional intArray
     * @param bytes to convert from
     * @return 2d int array
     */
    public int[][] convertBytesToIntArray(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DataInputStream dis = new DataInputStream(bis);

        int[][] array = new int[10][10];

        try {
            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array[i].length; j++) {
                    array[i][j] = dis.readInt();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return array;
    }

    /**
     * Function that is autostarting when thread is started
     */
    @Override
    public void run() {
        String messageFromClient;   // String to save read message from client
        Boolean step1 = false;      // Flag to stop 1st while loop TODO mysle ze da sie bez flagi za pomoca break

        try{
            while(socket.isConnected() && !step1){
                requestMessage();
                messageFromClient = readMessageFromClient();

                // First option is to create room
                if(messageFromClient.equals("1")){
                    String roomName = String.format(this.username + "'s Room");
                    currentRoom = server.createRoom(roomName, this);
                    sendMessage("Room created: " + roomName);
                    step1 = true;   // Break from loop when room created
                }
                // Second option is to join to existing room
                else if (messageFromClient.equals("2")){
                    sendMessage("Enter room name: ");
                    requestMessage();
                    String roomName = bufferedReader.readLine();
                    Room roomToJoin = server.getRooms().stream()
                            .filter(room -> room.getRoomName().equals(roomName))
                            .findFirst()
                            .orElse(null);  // Checking if room with entered name exists
                    if (roomToJoin != null){
                        // Checking if room is not full already
                        if(roomToJoin.getPlayer2() == null){
                            roomToJoin.addPlayer2(this);
                            currentRoom = roomToJoin;
                            sendMessage("Joined room: " + roomToJoin.getRoomName());
                            sendMessageToClient("Player has joined your room!",roomToJoin.getHost());
                            step1 = true;   // Break from loop when joined room
                        }
                        else{
                            sendMessage("Room: " + roomName + " is full");
                        }
                    }
                    else {
                        sendMessage("Room not found: " + roomName);
                    }
                }
                // Third option is to view (send view) of existing rooms
                else if (messageFromClient.equals("3")){
                    sendMessage(server.getRooms().toString());
                }
            }

            sendMessage("SHIPS_PLACEMENT_PHASE");
            int shipsAmount = 4;
            String ack;
            sendMessage(String.format("%d",shipsAmount));
            for(int i = 1; i <= shipsAmount; i++){
                sendMessage(String.format("%d",i));
                ack = bufferedReader.readLine();
            }

            while(socket.isConnected()){
                /*byte[] receivedData = new byte[400];
                inputStream.read(receivedData);

                int[][] receivedGameBoard = convertBytesToIntArray(receivedData);
                drawBoard(receivedGameBoard);*/
            }
        }
        catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }


}
