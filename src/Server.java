import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<Room> rooms;
    // TODO aktualnie serwer trzyma informacje o wszystkich pokojach w tej klasie
    // TODO Nie jestem pewny czy to dobre rozwiązanie i myślę, że trzeba to zmienić
    // TODO moim pierwszym pomyslem bylo zmienienie tego na static, ale jeszcze idk czy to bedzie dzialac, a nie chce mi sie tego robic

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.rooms = new ArrayList<>();
    }

    /**
     * Funkcja ktora jest wywolywana w mainie, aby rozpoczac prace serwera
     */
    public void startServer(){
        try{
            while(!serverSocket.isClosed()){ // Nieskonczona petla, chyba ze socket serwera sie zamknie
                Socket socket = serverSocket.accept(); // Czekamy az jakis klient bedzie chcial sie polaczyc
                System.out.println("****New client has connected****"); // Komunikat do debugowania
                ClientHandler clientHandler = new ClientHandler(socket, this); // Tworzymy dla kazdego nowego klienta nowy ClientHandler

                Thread thread = new Thread(clientHandler); // Tworzymy nowy watek i przekazujemy clientHandlera utworzonego do niego
                thread.start(); // Wlaczamy watek          // W ten sposob mamy dla kazdego Clienta osobny ClientHandler
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Funkcja do zatrzymywania pracy serwera, tutaj tylko zamykamy socket
     */
    public void stopServer(){
        try{
            if(serverSocket != null){
                serverSocket.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(1234); // Tworzymy socket servera, na porcie 1234
        Server server = new Server(serverSocket);
        server.startServer();
    }

    public synchronized List<Room> getRooms() {
        return rooms;
    }

    /**
     * Funkcja do tworzenia nowych pokoi, jest tutaj uzyte synchronized zeby tylko 1 watek na raz mogl uzyc tej funkcji
     * @param roomName String parameter to define room name
     * @param host ClientHandler parameter to define host of the room
     * @return
     */
    public synchronized Room createRoom(String roomName, ClientHandler host) {
        Room room = new Room(roomName, host);
        rooms.add(room);
        return room;
    }

    public void wyswietl(){
        for (Room room : rooms){
            System.out.println(room);
        }
    }
}
