Давай разберём этот UDP-клиент и сервер подробно, так, чтобы это можно было использовать для обучения и документации в Markdown. Я постараюсь объяснить каждую ключевую часть кода и общую концепцию.

---

# UDP Client/Server на Java с JSON и ACK

## 1. Общая идея

Этот пример показывает:

* Клиент отправляет **объекты User** на сервер через **UDP** в формате JSON.
* Сервер принимает эти объекты, десериализует их, выводит на консоль и сохраняет в лог-файл.
* Клиент получает **подтверждение (ACK)** от сервера, чтобы убедиться, что пакет доставлен.
* Реализован механизм **повторной отправки** при таймауте.

---

## 2. Классы данных

### `User.java`

Класс описывает пользователя:

```java
private String username;
private String email;
private int age;
private boolean isActive;
private LocalDateTime registrationDate;
```

* `registrationDate` автоматически присваивается текущей датой.
* Имеются **геттеры и сеттеры** для всех полей.
* Можно выводить пользователя через `toString()` и `toFormattedString()`.

---

### `Packet.java`

Класс для отправки пакета с уникальным ID:

```java
public class Packet {
    public String id;
    public User user;

    public Packet() {} // нужно для Gson

    public Packet(String id, User user) {
        this.id = id;
        this.user = user;
    }
}
```

* `id` — уникальный идентификатор пакета (UUID), нужен для подтверждений ACK.
* `user` — объект User, который мы отправляем.

**Важно:** Класс `Packet` вынесен в отдельный файл, чтобы клиент и сервер могли использовать один и тот же класс.
Static inner class так использовать нельзя, потому что сервер не видит внутренние классы клиента.

---

## 3. UDP Клиент (`UdpClient.java`)

### Основные шаги

1. Создаём **DatagramSocket** для отправки пакетов.
2. Устанавливаем **таймаут на ответ** (`socket.setSoTimeout(TIMEOUT_MS)`), чтобы знать, если ACK не пришёл.
3. Для каждого пользователя:

   * Генерируем **уникальный ID пакета**.
   * Сериализуем объект `Packet` в JSON с помощью **Gson**.
   * Отправляем пакет через UDP.
   * Ждём **ACK от сервера**. Если таймаут, повторяем отправку до MAX_RETRIES.

---

### Пример отправки пакета с повторной отправкой

```java
boolean acknowledged = false;
int attempt = 0;

while (!acknowledged && attempt < MAX_RETRIES) {
    attempt++;
    socket.send(packet);
    System.out.printf("Sent user '%s' (packetId=%s, attempt %d)%n", user.getUsername(), packetId, attempt);

    try {
        byte[] ackBuffer = new byte[256];
        DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);
        socket.receive(ackPacket);

        String ackMessage = new String(ackPacket.getData(), 0, ackPacket.getLength());
        if (ackMessage.equals("ACK:" + packetId)) {
            System.out.println("Server acknowledged packetId: " + packetId);
            acknowledged = true;
        }
    } catch (SocketTimeoutException e) {
        System.out.println("Timeout waiting for ACK. Retrying...");
    }
}
```

**Пояснения:**

* Мы ждём `ACK:<packetId>` от сервера.
* Если ответ не пришёл (`SocketTimeoutException`), повторяем отправку.
* Если после MAX_RETRIES подтверждение не получено, выводим ошибку.

---

## 4. UDP Сервер (`UdpServer.java`)

### Основные шаги

1. Создаём **DatagramSocket** на порту 8888.
2. Ждём пакета от клиента через `socket.receive()`.
3. Преобразуем данные в JSON и десериализуем в `Packet` через Gson.
4. Получаем `User` из пакета.
5. Выводим данные в консоль.
6. Сохраняем пользователя в лог-файл.
7. Отправляем ACK клиенту с ID пакета.

---

### Обработка полученного пакета

```java
Packet receivedPacket = gson.fromJson(json, Packet.class);
User user = receivedPacket.user;

displayUserInfo(user, clientAddress, clientPort);
logUserToFile(user);

// Отправка ACK
String ack = "ACK:" + receivedPacket.id;
DatagramPacket ackPacket = new DatagramPacket(
        ack.getBytes(), ack.getBytes().length, packet.getAddress(), packet.getPort());
socket.send(ackPacket);
```

**Пояснения:**

* Gson автоматически преобразует JSON в объект `Packet`.
* ACK отправляется обратно на **адрес и порт отправителя**.
* Это позволяет клиенту понять, что пакет успешно доставлен.

---

### Логирование пользователей

```java
private static void logUserToFile(User user) {
    try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        writer.printf("%s | %s | %s | %d | %s | %s%n",
                timestamp, user.getUsername(), user.getEmail(),
                user.getAge(), user.isActive(), user.getRegistrationDate());
        writer.flush();
    } catch (IOException e) {
        System.err.println("Error writing to log file: " + e.getMessage());
    }
}
```

* Лог-файл содержит **время приёма**, имя пользователя, email, возраст, активность и дату регистрации.
* Используется **PrintWriter с append=true**, чтобы не перезаписывать файл.

---

## 5. Важные моменты и тонкости

1. **UDP — это ненадёжный протокол**

   * Пакеты могут потеряться.
   * Решение: механика **ACK + повторная отправка**.
2. **JSON сериализация**

   * Gson требует TypeAdapter для `LocalDateTime` (как у вас через `LocalDateTimeAdapter`).
3. **UUID для пакетов**

   * Позволяет отличать пакеты и сопоставлять ACK.
4. **Timeout и MAX_RETRIES**

   * Обеспечивают надёжность доставки без блокировки клиента навсегда.
5. **Буфер для DatagramPacket**

   * На клиенте и сервере нужно предусмотреть размер буфера (`BUFFER_SIZE`) достаточный для JSON.

---

## 6. Пример работы

### Клиент выводит:

```
UDP Client starting...
Sent user 'john_doe' (packetId=123e4567..., attempt 1)
Server acknowledged packetId: 123e4567...
Sent user 'alice_smith' (packetId=...)
...
All users processed.
```

### Сервер выводит:

```
UDP Server starting on port 8888
Server is ready...

===== RECEIVED USER DATA =====
From: 127.0.0.1:55555
Username: john_doe
Email: john@example.com
Age: 25
Active: true
Registered: 2025-10-22T05:12:34.123
====================================
User data saved to: users.log
```

---

## 7. Заключение

Этот пример идеально подходит для обучения:

* Как работать с **UDP-сокетами** в Java.
* Как передавать **сложные объекты через JSON**.
* Как реализовать **подтверждение доставки (ACK)**.
* Как логировать данные и обрабатывать таймауты и повторные отправки.
