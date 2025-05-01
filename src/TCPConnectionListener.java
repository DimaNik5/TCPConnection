/**
 * Интерфейс слушателя событий TCP-соединения.
 * Определяет callback-методы для обработки различных событий соединения.
 */
public interface TCPConnectionListener {

    /**
     * Вызывается, когда TCP-соединение готово к работе.
     * @param tcpConnection соединение, которое стало готово
     */
    void onConnectionReady(TCPConnection tcpConnection);

    /**
     * Вызывается при получении строкового сообщения через соединение.
     * @param tcpConnection соединение, через которое получено сообщение
     * @param value полученная строка
     */
    void onReceiveString(TCPConnection tcpConnection, String value);

    /**
     * Вызывается при отключении TCP-соединения.
     * @param tcpConnection соединение, которое было отключено
     */
    void onDisconnect(TCPConnection tcpConnection);

    /**
     * Вызывается при возникновении исключения в работе TCP-соединения.
     * @param tcpConnection соединение, в котором произошла ошибка
     * @param e исключение, которое возникло
     */
    void onException(TCPConnection tcpConnection, Exception e);
}