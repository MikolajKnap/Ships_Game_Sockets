import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class ClientHandler implements Runnable {

    public static Set<ClientHandler> clientHandlers = new HashSet<>();
    private Socket socket;
    private String username;
    private Server server;
    private Room currentRoom;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    ObjectInputStream objectInputStream;


    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;

            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // To jest wykorzystywane, zeby moc przesylac Stringi do serwera
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())); // To jest wykorzystywane, zeby moc odbierac Stringi od serwera
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            this.username = bufferedReader.readLine(); // Po polaczeniu sie clienta z serwerem automatycznie w klient handlerze czekamy na wiadomosc od clienta z usernamem
            clientHandlers.add(this); // Dodajemy kazdego ClientHandlera do listy clientHandlerow zeby moc w razie czego wyszukiwac

        }
        catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter, objectInputStream);
        }
    }

    public void requestMessage() throws IOException {
        bufferedWriter.write("REQUEST_DATA");
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public String readMessageFromClient() throws IOException {
        return bufferedReader.readLine();
    }

    public void sendMessage(String message) throws IOException {
        bufferedWriter.write(message);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public void sendMessageToClient(String message, ClientHandler clientHandler) throws IOException {
        clientHandler.bufferedWriter.write(message);
        clientHandler.bufferedWriter.newLine();
        clientHandler.bufferedWriter.flush();
    }

    public void menuPhase() throws IOException {
        String messageFromClient;
        while (socket.isConnected()) {
            requestMessage();
            messageFromClient = readMessageFromClient();
            if (messageFromClient.equals("1")) {
                String roomName = String.format(this.username + "'s Room"); // Tworzymy pokoj o nazwie, przyklad: Gieniek's Room
                currentRoom = server.createRoom(roomName, this); // TODO sprawdzanie nazwy czy sie powtarza
                sendMessage("Room created: " + roomName);
                break;
            }
            else if (messageFromClient.equals("2")) {
                sendMessage("Enter room name: ");
                requestMessage();
                String roomName = bufferedReader.readLine();
                Room roomToJoin = server.getRooms().stream()
                        .filter(room -> room.getRoomName().equals(roomName))
                        .findFirst()
                        .orElse(null);  // Checking if room with entered name exists
                if (roomToJoin != null) {
                    if (roomToJoin.getPlayer2() == null) {
                        roomToJoin.addPlayer2(this);
                        currentRoom = roomToJoin;
                        sendMessage("Joined room: " + roomToJoin.getRoomName());
                        sendMessageToClient("Player has joined your room!", roomToJoin.getHost());
                        currentRoom.getLatchRoomPhase().countDown();
                        break;
                    } else {
                        sendMessage("Room: " + roomName + " is full");
                    }
                } else {
                    sendMessage("Room not found: " + roomName);
                }
            }
            else if (messageFromClient.equals("3")) {
                sendMessage(server.getRooms().toString());
            }
        }
    }

    public void shipPlacementPhase() throws IOException {
        sendMessage("SHIPS_PLACEMENT_PHASE");
        int biggestShipSize = 1;
        for(int i = biggestShipSize; i >= 1; i --){
            for(int j = (biggestShipSize + 1) - i; j >= 1; j--){
                sendMessage(String.format("%d", i));
                bufferedReader.readLine();
            }
        }
        sendMessage(String.format("%d", 123456789)); // Wiadomosc sygnalizujaca koniec
    }


    public void gameBoardsSetter() throws IOException, ClassNotFoundException {
        SerializableArrayList receivedData = (SerializableArrayList) objectInputStream.readObject();
        ArrayList<ArrayList<String>> data = receivedData.getData();
        if(currentRoom.getHost() == this){
            currentRoom.setHostArrayList(data);
        }
        else if(currentRoom.getPlayer2() == this){
            currentRoom.setPlayer2ArrayList(data);
            currentRoom.getLatchPlacingPhase().countDown();
        }
    }

    public void latchWaiter(CountDownLatch latchToCheck, int valueToCheck) throws InterruptedException {
        while(latchToCheck.getCount() > valueToCheck){
            Thread.sleep(1000);
        }
    }

    @Override
    public void run() {
        try {
            // Rozpoczecie fazy menu
            menuPhase();

            // Oczekiwanie na drugiego gracza w pokoju
            latchWaiter(currentRoom.getLatchRoomPhase(), 0);

            // Rozpoczecie fazy ship placement
            shipPlacementPhase();

            // Ustawienie tablic (plansz gier)
            gameBoardsSetter();

            // Oczekiwanie na ustawienie pozycji drugiego gracza
            latchWaiter(currentRoom.getLatchPlacingPhase(),0);

            while (!currentRoom.isGameOver() && socket.isConnected()) {
                Thread.sleep(1000);
                if (currentRoom.getWhoToPlay() == this) {
                    sendMessage("Enter position to shot: ");
                    requestMessage();
                    String position = bufferedReader.readLine(); //a1
                    try {
                        String processedShot = processShot(position, currentRoom.getArrayBasedOnPlayerWhoDoesntPlay());
                        if(processedShot.equals("SHOT")){
                            sendMessage("You have shot opponent's ship!");
                        }
                        else if(processedShot.equals("SINKED")){
                            sendMessage("You have sinked opponent's ship!");
                        }
                        else if(processedShot.equals("MISS")){
                            sendMessage("You have missed!");
                            currentRoom.setWhoToPlay(currentRoom.getPlayerWhoDoesntPlay());
                        }
                        if(currentRoom.getArrayBasedOnPlayerWhoDoesntPlay().isEmpty()){
                            currentRoom.setGameOver(true);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        sendMessage("Position unavailable");
                    }
                }
            }

            if(this == currentRoom.getWhoToPlay()){
                sendMessage("YOU HAVE WON!!!!! :)");
                sendMessage("GAME_OVER_PHASE");
            }
            else{
                sendMessage("You have lost :(");
                sendMessage("GAME_OVER_PHASE");
            }
            this.closeEverything(socket,bufferedReader,bufferedWriter, objectInputStream);


        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            closeEverything(socket, bufferedReader, bufferedWriter, objectInputStream);
        }
    }

    public String processShot(String position, ArrayList<ArrayList<String>> playerArrayPositions) {
        String pos = position.toUpperCase();
        ArrayList<String> foundArray = new ArrayList<String>();
        String searchedPosition;
        for(ArrayList<String> tempArray : playerArrayPositions){
            searchedPosition = tempArray.stream()
                    .filter(s -> s.equals(pos))
                    .findFirst()
                    .orElse("NULL");
            if(!searchedPosition.equals("NULL")) {
                tempArray.remove(pos);
                if(!tempArray.isEmpty()) return "SHOT";
                else {
                    playerArrayPositions.remove(tempArray);
                    return "SINKED";
                }
            }
        }
        return "MISS";
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
    }

    public void removeRoom(){
        server.getRooms().remove(currentRoom);
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter, ObjectInputStream objectInputStream) {
        removeClientHandler();
        removeRoom();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if(objectInputStream != null){
                objectInputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
