import java.io.IOException;
import java.util.Scanner;

/**
 * Класс клиента для работы с TCP-соединением.
 * Реализует TCPConnectionListener для обработки событий соединения
 * и перенаправляет их через EventClientListener.
 */
public class Client implements TCPConnectionListener {

    private final EventClientListener eventClientListener;  // Слушатель событий клиента
    private final TCPConnection connection;                // TCP-соединение клиента

    /**
     * Создает новый клиент и устанавливает соединение с сервером.
     * @param ip IP-адрес сервера
     * @param listener слушатель событий клиента
     * @throws RuntimeException если не удалось установить соединение
     */
    public Client(String ip, EventClientListener listener) {
        this(ip, 8080, listener);
    }

    /**
     * Создает новый клиент и устанавливает соединение с сервером.
     * @param ip IP-адрес сервера
     * @param port порт соединения
     * @param listener слушатель событий клиента
     * @throws RuntimeException если не удалось установить соединение
     */
    public Client(String ip, int port, EventClientListener listener) {
        this.eventClientListener = listener;
        try {
            // Создаем новое TCP-соединение с сервером
            this.connection = new TCPConnection(this, ip, port);
        } catch (IOException e) {
            throw new RuntimeException("Failed to establish connection", e);
        }
    }

    /**
     * Отправляет сообщение через установленное соединение.
     * @param message текст сообщения для отправки
     */
    public void sendMessage(String message) {
        connection.sendString(message);
    }

    /**
     * Обработчик события готовности соединения.
     * @param tcpConnection соединение, которое стало готово
     */
    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        eventClientListener.onConnectionReady();
    }

    /**
     * Обработчик события получения сообщения.
     * @param tcpConnection соединение, через которое получено сообщение
     * @param value полученное текстовое сообщение
     */
    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        eventClientListener.onReceiveString(value);
    }

    /**
     * Обработчик события отключения соединения.
     * @param tcpConnection соединение, которое было отключено
     */
    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        eventClientListener.onDisconnect();
    }

    /**
     * Обработчик исключений в соединении.
     * @param tcpConnection соединение, в котором произошла ошибка
     * @param e исключение
     */
    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        eventClientListener.onException(e);
    }
}