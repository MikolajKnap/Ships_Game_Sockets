import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<Room> rooms;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.rooms = new ArrayList<>();
    }
    public void startServer(){
        try{
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("****New client has connected****");
                ClientHandler clientHandler = new ClientHandler(socket, this);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

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
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }

    public synchronized List<Room> getRooms() {
        return rooms;
    }

    public synchronized Room createRoom(String roomName, ClientHandler host) {
        Room room = new Room(roomName, host);
        return room;
    }
    public void wyswietl(){
        for (Room room : rooms){
            System.out.println(room);
        }
    }
}
