import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static ArrayList<String> roomIds = new ArrayList<>();
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;

    private Server server;
    private Room currentRoom;

    public ClientHandler(Socket socket, Server server){
        try{
            this.socket = socket;
            this.server = server;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = bufferedReader.readLine();
            clientHandlers.add(this);

        }
        catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;
        Boolean step1 = false;
        try{
            while(socket.isConnected() && !step1){
                messageFromClient = bufferedReader.readLine();
                if(messageFromClient.equals("1")){
                    String roomName = String.format(this.username + "'s Room");
                    currentRoom = server.createRoom(roomName, this);
                    sendMessage("Room created: " + roomName);
                    step1 = true;
                }
                else if (messageFromClient.equals("2")){
                    String roomName = bufferedReader.readLine();
                    Room roomToJoin = server.getRooms().stream()
                            .filter(room -> room.getRoomName().equals(roomName))
                            .findFirst()
                            .orElse(null);
                    if (roomToJoin != null){
                        roomToJoin.addPlayer(this);
                        currentRoom = roomToJoin;
                        sendMessage("Joined room: " + roomToJoin.getRoomName());
                        sendMessageToClient("Player has joined your room!",roomToJoin.getPlayer2());
                        step1 = true;
                    }
                    else {
                        sendMessage("Room not found: " + roomName);
                    }
                }
                else if (messageFromClient.equals("3")){
                    server.wyswietl();
                }
            }
            messageFromClient = bufferedReader.readLine();

            while(socket.isConnected()){
                ;
            }
        }
        catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }

    }

    public void broadcastMessage(String messageToSend){
        for (ClientHandler clientHandler : clientHandlers){
            try{
                if(!clientHandler.username.equals(username)){
                    clientHandler.bufferedWriter.write((messageToSend));
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessageToClient(String message, ClientHandler clientHandler) {
        try {
            clientHandler.bufferedWriter.write(message);
            clientHandler.bufferedWriter.newLine();
            clientHandler.bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(clientHandler.socket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + username + " has left!");
    }

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


}
