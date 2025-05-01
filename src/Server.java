import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс сервера для обработки множественных TCP-соединений.
 * Реализует TCPConnectionListener для управления подключениями клиентов.
 */
public class Server implements TCPConnectionListener {

    private final EventServerListener eventConnectionListener;  // Слушатель событий сервера
    private final List<TCPConnection> connection;               // Список активных соединений

    /**
     * Создает новый серверный экземпляр и запускает прием подключений.
     * @param listener слушатель событий сервера для callback-уведомлений
     */
    public Server(EventServerListener listener) {
        this.eventConnectionListener = listener;
        this.connection = new ArrayList<>();

        // Определение и установка собственного IP-адреса
        try {
            InetAddress me = InetAddress.getLocalHost();
            eventConnectionListener.setIP(me.getHostAddress());
        } catch (UnknownHostException e) {
            eventConnectionListener.onException(e);
        }

        // Основной цикл принятия подключений
        try (ServerSocket serverSocket = new ServerSocket(Constants.PORT)) {
            while (true) {
                try {
                    // Принимаем новое подключение и создаем TCPConnection
                    new TCPConnection(this, serverSocket.accept());
                } catch (IOException e) {
                    eventConnectionListener.onException(e);
                }
            }
        } catch (IOException e) {
            eventConnectionListener.onException(e);
        }
    }

    /**
     * Обработчик готовности нового соединения.
     * @param tcpConnection установленное соединение
     */
    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connection.add(tcpConnection);
        eventConnectionListener.onConnectionReady();
    }

    /**
     * Обработчик получения сообщения от клиента.
     * @param tcpConnection соединение-источник сообщения
     * @param value полученное текстовое сообщение
     */
    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        eventConnectionListener.onReceiveString(value);
    }

    /**
     * Обработчик отключения клиента.
     * @param tcpConnection разорванное соединение
     */
    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connection.remove(tcpConnection);
        eventConnectionListener.onDisconnect();
    }

    /**
     * Обработчик исключений в соединении.
     * @param tcpConnection соединение с ошибкой (может быть null)
     * @param e возникшее исключение
     */
    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        eventConnectionListener.onException(e);
    }

    /**
     * Рассылает сообщение всем подключенным клиентам.
     * @param value сообщение для рассылки
     */
    public void sendToAllConnection(String value) {
        for (TCPConnection tcpConnection : connection) {
            tcpConnection.sendString(value);
        }
    }
}