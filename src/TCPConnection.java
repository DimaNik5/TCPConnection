import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Класс, реализующий TCP-соединение с возможностью отправки и получения строковых сообщений.
 * Обрабатывает события соединения через интерфейс TCPConnectionListener.
 */
class TCPConnection {

    private final Socket socket;               // Сокет для соединения
    private final Thread rxThread;             // Поток для приема сообщений
    private final TCPConnectionListener eventListener; // Обработчик событий соединения
    private final BufferedReader in;           // Поток чтения из сокета
    private final BufferedWriter out;          // Поток записи в сокет

    /**
     * Создает новое TCP-соединение с указанным обработчиком событий, IP-адресом и портом.
     * @param eventListener обработчик событий соединения
     * @param ipAddr IP-адрес для подключения
     * @param port порт для подключения
     * @throws IOException если произошла ошибка при создании соединения
     */
    TCPConnection(TCPConnectionListener eventListener, String ipAddr, int port) throws IOException {
        this(eventListener, new Socket(ipAddr, port));
    }

    /**
     * Создает новое TCP-соединение с указанным обработчиком событий и существующим сокетом.
     * @param eventListener обработчик событий соединения
     * @param socket существующий сокет для соединения
     * @throws IOException если произошла ошибка при инициализации потоков ввода/вывода
     */
    TCPConnection(TCPConnectionListener eventListener, Socket socket) throws IOException {
        this.eventListener = eventListener;
        this.socket = socket;
        // Инициализация потоков чтения и записи с указанием кодировки UTF-8
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

        // Создание и запуск потока для приема сообщений
        rxThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    // Цикл приема сообщений до прерывания потока
                    while (!rxThread.isInterrupted()){
                        eventListener.onReceiveString(TCPConnection.this, in.readLine());
                    }
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                } finally {
                    eventListener.onDisconnect(TCPConnection.this);
                }
            }
        });
        rxThread.start();
    }

    /**
     * Отправляет строковое сообщение через соединение.
     * @param value строка для отправки
     */
    public synchronized void sendString(String value){
        try {
            out.write(value + "\r\n");  // Добавляем символы конца строки
            out.flush();               // Принудительно отправляем буфер
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnect();
        }
    }

    /**
     * Закрывает соединение, прерывает поток приема и закрывает сокет.
     */
    public synchronized void disconnect(){
        rxThread.interrupt();  // Прерываем поток приема
        try {
            socket.close();    // Закрываем сокет
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }
    }

    /**
     * Возвращает строковое представление соединения в формате "TCPConnection: IP; port".
     * @return строковое представление соединения
     */
    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + "; " + socket.getPort();
    }
}