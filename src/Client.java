import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;



public class Client {
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;
    private int[][] gameBoard, shootBoard;
    private OutputStream outputStream;

    public Client() {
        this.gameBoard = new int[10][10];
        this.shootBoard = new int[10][10];
        for (int i = 0; i < gameBoard.length; i++) {
            Arrays.fill(gameBoard[i], 0);
            Arrays.fill(shootBoard[i], 0);
        }
    }

    public Client(Socket socket, String username){
        try{
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
            this.gameBoard = new int[10][10];
            this.shootBoard = new int[10][10];
            for (int i = 0; i < gameBoard.length; i++) {
                Arrays.fill(gameBoard[i], 0);
                Arrays.fill(shootBoard[i], 0);
            }
            this.outputStream = socket.getOutputStream();
        }
        catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage(String messageToSend){
        try{
            bufferedWriter.write(messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        catch (IOException e ){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
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

    public void placeShips(int shipLength, String positions) {
        positions = positions.toUpperCase();
        String[] positionsArray = positions.split(" ");
        int[][] tempGameBoard = new int[10][10];
        copy2DArray(gameBoard,tempGameBoard);
        // Dla każdej pozycji statku
        for (String position : positionsArray) {
            // Wyodrebniamy literę i cyfrę
            char letter = position.charAt(0);
            int number = Integer.parseInt(position.substring(1)) - 1;

            // Przeksztalcamy literę na indeks wiersza (np. A -> 0, B -> 1, C -> 2)
            int row = (int) letter - (int) 'A';

            // Sprawdź, czy pole jest dostępne i zapisz na planszy
            if (isPositionAvailable(row, number)) {
                // Zapisujemy pozycje do temp tablicy
                tempGameBoard[row][number] = shipLength;
            } else {
                // Pole jest niedostępne
                System.out.println("Position unavailable");
                break;
            }
        }
        copy2DArray(tempGameBoard,gameBoard);
    }
    public boolean isPositionEmpty(int x, int y) {
        int fillingCharacter = 0;
        int gameBoardSize = 10;
        if (x >= 0 && x < gameBoardSize && y >= 0 && y < gameBoardSize) {
            return gameBoard[x][y] == fillingCharacter;
        }
        return false;
    }
    // 1 - 4 kratki
    // 2 - 3 kratki
    // 3 - 2 kratki
    // 4 - 1 kratka
    public boolean isPositionAvailable(int x, int y){
        if(isPositionEmpty(x, y)){
            // Przypadek pierwszego wiersza
            if(x == 0){
                // Sprawdzamy wiersz nizej
                if(gameBoard[x+1][y] != 0){
                    return false;
                }
            }
            // Przypadek ostatniego wiersza
            else if(x == 9){
                // Sprawdzamy wiersz wyzej
                if(gameBoard[x-1][y] != 0){
                    return false;
                }
            }
            else{
                if(gameBoard[x-1][y] != 0 || gameBoard[x+1][y] != 0){
                    return false;
                }
            }
            // Przypadek lewego brzegu
            if(y == 0){
                // Sprawdzamy prawy brzeg
                if(gameBoard[x][y+1] != 0){
                    return false;
                }
            }
            // Przypadek prawego brzegu
            else if(y == 9){
                // Sprawdzamy lewy brzeg
                if(gameBoard[x][y-1] != 0){
                    return false;
                }
            }
            else{
                if(gameBoard[x][y+1] != 0 || gameBoard[x][y-1] != 0){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void drawGameBoard(){
        int boardSize = 10;
        for(int i = 0; i<boardSize; i++){
            for(int j = 0; j<boardSize; j++){
                System.out.print(gameBoard[i][j] + " ");
            }
            System.out.println(" ");
        }
    }

    public static void copy2DArray(int[][] source, int[][] destination) {
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, destination[i], 0, source[i].length);
        }
    }

    public byte[] convertIntArrayToBytes(int[][] array) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            for (int i = 0; i < array.length; i++) {
                for (int j = 0; j < array[i].length; j++) {
                    dos.writeInt(array[i][j]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    public void start() {
        // Wątek nasłuchujący serwera
        Thread serverListenerThread = new Thread(() -> {
            try {
                while (true) {
                    String messageFromServer = bufferedReader.readLine();
                    // Przekazanie wiadomości do wątku obsługi klienta
                    processServerMessage(messageFromServer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverListenerThread.start();
    }

    public void processServerMessage(String messageFromServer){
        if (messageFromServer.equals("REQUEST_DATA")) {
            // Serwer prosi o przesłanie danych
            System.out.println("Player input: ");
            Scanner scannerProcess = new Scanner(System.in);
            String messageToServer = scannerProcess.nextLine();
            sendMessage(messageToServer);
        } else {
            System.out.println("\n" + messageFromServer);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", 1234);

        System.out.println("WELCOME TO THE BATTLESHIPS GAME");
        System.out.println("Enter your username: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        System.out.println("\nChoose 1 - create room\nChoose 2 - join room\nChoose 3 - view rooms\n");

        Client client = new Client(socket, username);
        client.sendMessage(username);
        client.start();
    }
}
