import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;
    private char[][] gameBoard, shootBoard;

    public Client(Socket socket, String username){
        try{
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
            this.gameBoard = new char[9][9];
            this.shootBoard = new char[9][9];
            for (int i = 0; i < gameBoard.length; i++) {
                Arrays.fill(gameBoard[i], 'O');
                Arrays.fill(shootBoard[i], 'O');
            }
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

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()){
                    try{
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    }
                    catch (IOException e){
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
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

    public void placeShips(int shipLength, String positions){
        String[] positionsArray = positions.split(" ");
        for (String pos : positionsArray){
            char letter = pos.charAt(0);
            int number = Integer.parseInt(pos.substring(1));
        }
    }

    public boolean isPositionEmpty(int x, int y) {
        if (x >= 0 && x < 10 && y >= 0 && y < 10) {
            return gameBoard[x][y] == 'X';
        }
        return false;
    }

    public boolean isPositionAvailable(int x, int y, char ch) {
        if (isPositionEmpty(x, y)) {
            if(!isCharIn2DArray(ch)){
                return true;
            }
            else{
                ; // Tu cos chcialem zrobic
            }
        }
        return false;
    }

    public boolean isCharIn2DArray(char target) {
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                if (gameBoard[row][col] == target) {
                    return true;
                }
            }
        }
        return false; // Nie znaleziono znaku w tablicy
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String messageFromServer;
        String messageToServer;
        System.out.println("WELCOME TO THE SHIPS GAME");
        System.out.println("Enter your username: ");

        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();

        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket, username);
        client.sendMessage(username);

        System.out.println("Choose 1 - create room\nChoose 2 - join room\nChoose 3 - view rooms\n");
        messageToServer = scanner.nextLine();
        client.sendMessage(messageToServer);
        messageFromServer = client.bufferedReader.readLine();
        System.out.println(messageFromServer);

        System.out.println("\nMasz dostepne 5 statkow:\nJeden statek o dlugosci 4\nJeden statek o dlugosci 3\nJeden statek o dlugosci 2\nDwa statki o dlugosci 1:");
        System.out.println("Wybierz pozycje dla statku o dlugosci 4 (format: A2 A3 A4 A5)");


    }
}
