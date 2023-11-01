import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomName;
    private ClientHandler host;
    private ClientHandler player2;
    private ClientHandler whoToPlay;

    public Room(String roomName, ClientHandler host) {
        this.roomName = roomName;
        this.host = host;
        this.player2 = null;
        this.whoToPlay = host;
    }

    public String getRoomName() {
        return roomName;
    }

    public ClientHandler getHost() {
        return host;
    }

    public void setHost(ClientHandler host){
        this.host = host;
    }

    public ClientHandler getPlayer2() {
        return player2;
    }

    public void addPlayer(ClientHandler player) {
        this.player2 = player;
    }

    public ClientHandler getWhoToPlay() {
        return whoToPlay;
    }

    public void setWhoToPlay(ClientHandler whoToPlay) {
        this.whoToPlay = whoToPlay;
    }

    @Override
    public String toString() {
        return "Room: " + roomName;
    }
}
