import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;



public class Client {
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;
    private int[][] gameBoard, shootBoard;
    private OutputStream outputStream;
    private ObjectOutputStream objectOutputStream;

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
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
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

    // Cala logika kladzenia statkow jest tutaj
    // Masakryczna robota
    public boolean placeShips(int shipLength, String positions) {
        // Na poczatku zmieniamy podane przez ziomsa pozycje na duze litery, zeby moc pozniej zrobic konwersje liter na int pozycje dla tablicy
        positions = positions.toUpperCase();

        // Tworzymy tabele, gdzie kazdy wyraz w tabeli to np. [0] = "a1", [1] = "a2"
        String[] positionsArray = positions.split(" ");

        // Sprawdzanie czy gracz podal dobra ilosc pozycji
        if(positionsArray.length != shipLength){
            System.out.println("Wrong ship size!");
            return false;
        }

        // Z stringa przykladowego a1 a2 a3 a4, zostawiamy same cyfy = 1234
        String onlyNumbers = positions.replaceAll("[^0-9]", "");

        // Tutaj robimy nowa tabele takiej samej wielkosci jak ta w ktorej zapisalismy pozycje
        String[] sortedPositions = new String[positionsArray.length];
        System.arraycopy(positionsArray, 0, sortedPositions, 0, positionsArray.length); // Do nowej tablicy kopiujemy stara
        Arrays.sort(sortedPositions); // Ta nowa tablice sortujemy, jesli bylyby pozycje a2 a3 a1 to bedzie a1 a2 a3, a jak a1 c1 d1 to bedzie a1 c1 d1

        // Warto rozkminic ze mamy tylko dwie mozliwe kierunki ukladania statkow w poziomie albo pionowo
        // Wiec kiedy jest poziomo to litera musi byc ta sama a zmienia sie tylko liczba
        // A kiedy jest pionowo to liczba jest ta sama a zmienia sie tylko litera
        // Jesli mamy sytuacje ze cyfry sa te same to znaczy ze aby sprawdzic czy pozycja jest legalna musimy zajmowac sie literami
        // Natomiast jesli cyfry nie sa te same to znaczy ze musimy zajac sie cyframi zeby sprawdzic czy pozycja jest legalna
        // Tutaj sprawdzamy tylko czy kolejne pozycje znajduja sie obok siebie, np. czy statek o wielkosci 3 blokow nie jest a1 a3 a4 bo bylaby dziura
        if(onlyCharacters(onlyNumbers)){ // Zobacz do funkcji onlyCharacters
            for(int i = 1; i<sortedPositions.length; i++){
                // Filip wykminił ze aby pozycja byla poprawna to roznica miedzy kolejna pozycja a poprzednai musi byc == 1
                // Wiec po prostu to sprawdzamy
                int letterToNumber = (int) sortedPositions[i].charAt(0) - (int) 'A';
                int prevLetterToNumber = (int) sortedPositions[i-1].charAt(0) - (int) 'A';
                if(letterToNumber - prevLetterToNumber != 1){
                    System.out.println("Position unavailable");
                    return false;
                }
            }
        }
        else{
            for(int i = 1; i<sortedPositions.length; i++) {
                int letterToNumber = sortedPositions[i].charAt(1) - '0';
                int prevLetterToNumber = sortedPositions[i - 1].charAt(1) - '0';
                if (letterToNumber - prevLetterToNumber != 1) {
                    System.out.println("Position unavailable");
                    return false;
                }
            }
        }
        // Ogolnie potrzebujemy zapasowej tablicy gry, poniewaz przy kladzeniu kolejnych pozycji jednego statku
        // Mielibysmy problem z sprawdzaniem czy mozna go tutaj polozyc bo poprzednia pozycja by to zaklocala
        // Jak cos to wytlumacze to slownie bo duzo pisania
        int[][] tempGameBoard = new int[10][10];
        copy2DArray(gameBoard,tempGameBoard);

        // Dla każdej pozycji statku
        for (String position : positionsArray) {
            // Wyodrebniamy literę i cyfrę
            char letter;
            int number;
            if(position.length() == 3){
                letter = position.charAt(0);
                number = Integer.parseInt(String.format("%c%c",position.charAt(1),position.charAt(2))) - 1;
            }
            else if(position.length() == 2){
                letter = position.charAt(0);
                number = Character.getNumericValue(position.charAt(1)) - 1;
            }
            else{
                System.out.println("Position unavailable");
                return false;
            }

            // Przeksztalcamy literę na indeks wiersza (np. A -> 0, B -> 1, C -> 2)
            int row = (int) letter - (int) 'A';

            // Sprawdź, czy pole jest dostępne i zapisz na planszy
            if (isPositionAvailable(row, number)) {
                // Zapisujemy pozycje do temp tablicy
                tempGameBoard[row][number] = shipLength;
            } else {
                // Pole jest niedostępne
                System.out.println("Position unavailable");
                return false;
            }
        }
        // Jesli wszystkie pozycje sa okej to mozemy skopiowac nasza zapasowa tablice na faktyczna tablice gry
        copy2DArray(tempGameBoard,gameBoard);
        return true;
    }

    // Ta funkcja ma za zadanie sprawdzic czy podany String skalda sie z tych samych znakow wszedzie
    // W praktyce jest ona uzywana do sprawdzenia dla przykladowego stringa: 1111
    // Czy pierwsza 1ka jest jedynym znakiem w calym stringu
    // I w ten sposob sprawdzamy czy mamy statek w pozycji pionowej czy poziomej
    // Zajmujemy sie tylko cyframi bo jak cyfry nie beda zgodne to wiadomo ze musimy zajac sie literami
    public boolean onlyCharacters(String input){
        if(input.isEmpty()) return false;
        char firstChar = input.charAt(0);
        for(int i = 1; i < input.length(); i++){
            if(input.charAt(i) != firstChar) return false;
        }
        return true;
    }


    public boolean isPositionEmpty(int x, int y) {
        int fillingCharacter = 0;
        int gameBoardSize = 10;
        if (x >= 0 && x < gameBoardSize && y >= 0 && y < gameBoardSize) {
            return gameBoard[x][y] == fillingCharacter;
        }
        return false;
    }

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
                // Przypadek srodka, sprawdzamy gora dol
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
            // Przypadek srodka, sprawdzamy lewo prawo
            else{
                if(gameBoard[x][y+1] != 0 || gameBoard[x][y-1] != 0){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void drawGameBoard(){
        char[] rowLabels = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
        int boardSize = 10;

        // Print column labels
        System.out.print(" \t");
        for (int i = 1; i<=10; i++) {
            System.out.print(i + "\t");
        }
        System.out.println();

        for(int i = 0; i < boardSize; i++){
            System.out.print((rowLabels[i]) + "\t"); // Print row label
            for(int j = 0; j < boardSize; j++){
                System.out.print(gameBoard[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static void copy2DArray(int[][] source, int[][] destination) {
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, destination[i], 0, source[i].length);
        }
    }

    // Idk jak to dziala, ale zamysl jest taki zeby przeslac tablice dwuwymiarowa do serwera
    // Ta funkcja po prostu zmienia tablice intArray 2d na bajty
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
        boolean keepGoing = true;
            try {
                while (keepGoing) {
                    String messageFromServer = bufferedReader.readLine();
                    keepGoing = processServerMessage(messageFromServer);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    public boolean processServerMessage(String messageFromServer) throws IOException {
        if (messageFromServer.equals("REQUEST_DATA")) {
            System.out.print("Player input: ");
            Scanner scannerProcess = new Scanner(System.in);
            String messageToServer = scannerProcess.nextLine();
            sendMessage(messageToServer);
            return true;
        }
        // Tutaj jak odbierzemy taki komunikat, to wchodzimy w faze kladzenia statkow
        else if(messageFromServer.equals("SHIPS_PLACEMENT_PHASE")){
            ArrayList<ArrayList<String>> arrayToSend = new ArrayList<>();
            String positions;
            Scanner scannerShips = new Scanner(System.in);
            int shipLength = 1;

            System.out.println("PLACE YOUR SHIPS");
            System.out.println("Placing ship example format for 3 block ship: a1 a2 a3\n");

            shipLength = Integer.parseInt(bufferedReader.readLine());
            while(true){
                drawGameBoard(); // Rysujemy obecna plansze
                System.out.println("Place " + shipLength + " block size ship:");
                positions = scannerShips.nextLine();
                if(placeShips(shipLength, positions)){
                    positions = positions.toUpperCase();
                    arrayToSend.add(new ArrayList<String>((Arrays.asList(positions.split(" ")))));
                    sendMessage("ack");
                    shipLength = Integer.parseInt(bufferedReader.readLine());
                    clearScreen();
                    if(shipLength == 123456789) break;
                }
            }
            System.out.println("Your final board:\n");
            drawGameBoard();

            // Wyslanie arrayToSend do serwera:
            SerializableArrayList serializableArrayToSend = new SerializableArrayList(arrayToSend);
            objectOutputStream.writeObject(serializableArrayToSend);

            return true;
        }
        else if(messageFromServer.equals("GAME_OVER_PHASE")){
            return false;
        }
        else {
            System.out.println("\n" + messageFromServer);
            return true;
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
        client.sendMessage(username); // Ogolnie musimy podac najpierw username do serwera, bo mamy w konstruktorze ClientHandlera od razu nasluchiwanie
                                      // Ktore oczekuje zebysmy to podali
        client.start(); // Ropoczynamy nasluchiwanie w oddzielnym watku, zeby nie zacinac wszystkiego
        // W sumie to nie jestem pewny czy to trzeba w osobnym watku, ale tak jest cool B)
        // Wyjasnienie dzialania klienta warto zaczac od funkcji start
        System.out.println("Thank you for playing <3");
    }
}
