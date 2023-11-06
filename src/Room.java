import java.util.Arrays;

public class Room {
    private final String roomName;
    private ClientHandler host;
    private ClientHandler player2;
    private ClientHandler whoToPlay;
    private int[][] hostBoard, player2Board;

    public Room(String roomName, ClientHandler host) {
        this.roomName = roomName;
        this.host = host;
        this.player2 = null;
        this.whoToPlay = host;
        this.hostBoard = new int[10][10];
        this.player2Board = new int[10][10];
        for (int i = 0; i < hostBoard.length; i++) {
            Arrays.fill(hostBoard[i], 0);
            Arrays.fill(player2Board[i], 0);
        }
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

    public void addPlayer2(ClientHandler player) {
        this.player2 = player;
    }

    public ClientHandler getWhoToPlay() {
        return whoToPlay;
    }

    public void setWhoToPlay(ClientHandler whoToPlay) {
        this.whoToPlay = whoToPlay;
    }

    public int[][] getHostBoard() {
        return hostBoard;
    }

    public void setHostBoard(int[][] hostBoard) {
        this.hostBoard = hostBoard;
    }

    public int[][] getPlayer2Board() {
        return player2Board;
    }

    public void setPlayer2Board(int[][] player2Board) {
        this.player2Board = player2Board;
    }

    @Override
    public String toString() {
        return "Room: " + roomName;
    }
}
