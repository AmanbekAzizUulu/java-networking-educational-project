# Клиент — Сервер на `Socket` (подробный разбор для обучения)

Ниже — готовый Markdown-файл, который объясняет строчка в строчку два Java-класса: `ClientSocket` (клиент) и `ServerSocketDemo` (сервер). Этот материал сделан так, чтобы прямо по нему учиться: есть пояснения, объяснение протокола обмена, замечания о проблемах, советы по улучшению и упражнения.

---

# Оглавление

- [Клиент — Сервер на `Socket` (подробный разбор для обучения)](#клиент--сервер-на-socket-подробный-разбор-для-обучения)
- [Оглавление](#оглавление)
- [Краткая суть](#краткая-суть)
- [Описание используемых классов / библиотек](#описание-используемых-классов--библиотек)
- [Протокол обмена (формат сообщений)](#протокол-обмена-формат-сообщений)
- [Client — пошагово (разбор кода)](#client--пошагово-разбор-кода)
- [Server — пошагово (разбор кода)](#server--пошагово-разбор-кода)
- [Поведение при запуске (пример сценария)](#поведение-при-запуске-пример-сценария)
- [Почему так — объяснение деталей и мотивация](#почему-так--объяснение-деталей-и-мотивация)
- [Проблемы, ограничения и места, где код может «сломаться»](#проблемы-ограничения-и-места-где-код-может-сломаться)
- [Улучшения и рекомендации (конкретные патчи)](#улучшения-и-рекомендации-конкретные-патчи)
- [Упражнения для закрепления](#упражнения-для-закрепления)
- [Краткая шпаргалка по API](#краткая-шпаргалка-по-api)

---

# Краткая суть

* Это простая синхронная реализация TCP-клиента и TCP-сервера на Java.
* Сервер слушает порт `8081`, принимает одно соединение за другим (последовательно, в основном потоке), читает сообщение от клиента в формате: `[int length][bytes data]`, отвечает тем же способом: сначала длина `int`, затем байты.
* Клиент подключается к `localhost:8081`, формирует строковое сообщение, посылает сначала длину (4 байта, `int`), затем байты сообщения, затем ждёт ответа в том же бинарном формате.

Такой протокол называется **length-prefixed framing** (сообщение предваряется длиной) — он решает проблему, как понять «где конец сообщения» в TCP-потоке.

---

# Описание используемых классов / библиотек

* `java.net.Socket` — клиентский TCP-сокет (клиент и сервер при принятии соединения возвращают `Socket`).
* `java.net.ServerSocket` — серверный сокет, слушающий порт и принимающий входящие соединения.
* `java.io.DataOutputStream` / `DataInputStream` — удобные обёртки для записи и чтения примитивов (включая `int`) и байтов.
* `try (resource)` — try-with-resources (автоматическое закрытие ресурсов).
* `getBytes()` / `new String(byte[])` — преобразование между `String` и `byte[]` (по умолчанию — системная кодировка; лучше явно указывать `StandardCharsets.UTF_8`).

---

# Протокол обмена (формат сообщений)

Каждое сообщение (и от клиента к серверу, и от сервера к клиенту) отправляется в таком виде:

1. `int` (4 байта, big-endian) — длина данных в байтах `N`.
2. `N` байт — сами данные (payload), обычно UTF-8 текст.

Преимущество: получатель знает, сколько байт ждать, и может корректно прочитать полное сообщение (в отличие от чтения по строкам, которое неточно при бинарных данных).

---

# Client — пошагово (разбор кода)

Файл: `Client.java` (пакет `com.dandaev.edu.client`), класс `ClientSocket`.

```java
try (Socket socket = new Socket("localhost", 8081)) {
    var os = new DataOutputStream(socket.getOutputStream());
    var is = new DataInputStream(socket.getInputStream());

    // Отправляем запрос
    String request = "request from client";
    byte[] requestBytes = request.getBytes();
    os.writeInt(requestBytes.length); // Сначала длину
    os.write(requestBytes); // Затем данные
    os.flush();
    System.out.println("Request sent to server");

    // Читаем ответ
    int responseLength = is.readInt();
    byte[] responseBuffer = new byte[responseLength];
    is.readFully(responseBuffer);
    String serverResponse = new String(responseBuffer);
    System.out.println("Received from server: " + serverResponse);

} catch (IOException e) {
    e.printStackTrace();
}
```

1. `new Socket("localhost", 8081)` — устанавливает TCP-соединение с сервером на локальной машине и порту 8081.
2. `try-with-resources` автоматически закроет `socket`, а вместе с ним и связанные потоки (в конце блока).
3. `DataOutputStream os = new DataOutputStream(socket.getOutputStream())` — обёртка для удобной записи примитивов.
4. `DataInputStream is = new DataInputStream(socket.getInputStream())` — обёртка для удобного чтения примитивов.
5. Формируется строка `request` и конвертируется в `byte[]` через `request.getBytes()` (в идеале указать `UTF-8`).
6. `os.writeInt(requestBytes.length)` — записывает 4-байтовое `int` в сетевой поток (big-endian) — длину сообщения.
7. `os.write(requestBytes)` — записывает сами байты сообщения.
8. `os.flush()` — форсирует отправку данных (в большинстве случаев `write` уже отправляет, но `flush` гарантирует выталкивание буфера).
9. Клиент ожидает ответа: `int responseLength = is.readInt()` — блокирующий вызов, ждёт 4 байта.
10. Создаёт `responseBuffer` нужной длины и `is.readFully(responseBuffer)` — читает ровно `responseLength` байт (блокирующий, пока все байты не прочитаны или пока не произойдёт EOF/ошибка).
11. Конвертирует байты в `String serverResponse = new String(responseBuffer)` и печатает.

---

# Server — пошагово (разбор кода)

Файл: `ServerSocketDemo.java` (пакет `com.dandaev.edu.server`), класс `ServerSocketDemo`.

```java
try (var serverSocket = new ServerSocket(8081)) {
    System.out.println("Server started on port 8081...");

    while (true) {
        try (Socket acceptedClientSocket = serverSocket.accept()) {
            System.out.println("Client connected: " + acceptedClientSocket.getInetAddress());

            var responseToClient = new DataOutputStream(acceptedClientSocket.getOutputStream());
            var requestFromClient = new DataInputStream(acceptedClientSocket.getInputStream());

            // Читаем длину сообщения, затем само сообщение
            int messageLength = requestFromClient.readInt();
            byte[] buffer = new byte[messageLength];
            requestFromClient.readFully(buffer);
            String clientMessage = new String(buffer);
            System.out.println("Received from client: " + clientMessage);

            // Отправляем ответ
            String response = "hello from server!";
            byte[] responseBytes = response.getBytes();
            responseToClient.writeInt(responseBytes.length); // Сначала длину
            responseToClient.write(responseBytes); // Затем данные
            responseToClient.flush();
            System.out.println("Response sent to client");

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

} catch (IOException e) {
    e.printStackTrace();
}
```

1. `new ServerSocket(8081)` — открывает серверный сокет и начинает слушать порт 8081.
2. `while (true)` — сервер работает в бесконечном цикле, последовательно принимая клиентов.
3. `serverSocket.accept()` — блокирующий вызов; ждёт входящего соединения и возвращает `Socket acceptedClientSocket` при подключении клиента.
4. Внутри `try-with-resources (acceptedClientSocket)` — сокет клиента закроется автоматически после обработки.
5. Создаются `DataOutputStream responseToClient` и `DataInputStream requestFromClient` для обмена с клиентом.
6. Читаем `int messageLength = requestFromClient.readInt()` — ожидаем длину.
7. `byte[] buffer = new byte[messageLength]` и `requestFromClient.readFully(buffer)` — прочитать ровно указанное количество байт.
8. `String clientMessage = new String(buffer)` — преобразуем байты в строку (в идеале указывать кодировку).
9. Формируем ответ `hello from server!`, конвертируем в байты, сначала шлём длину `writeInt`, затем данные `write(...)`, затем `flush()`.
10. После выхода из внутреннего `try` (после отправки ответа) — клиентский `Socket` автоматически закрывается, и цикл `while` снова ждёт нового клиента.

---

# Поведение при запуске (пример сценария)

1. Запустить `ServerSocketDemo` — в консоли увидим:

   ```
   Server started on port 8081...
   ```
2. Запустить `ClientSocket` — клиент подключится к `localhost:8081`, отправит сообщение и дождётся ответа.
3. Консоль сервера покажет что-то вроде:

   ```
   Client connected: /127.0.0.1
   Received from client: request from client
   Response sent to client
   ```
4. Консоль клиента:

   ```
   Request sent to server
   Received from server: hello from server!
   ```

---

# Почему так — объяснение деталей и мотивация

* **Length-prefix framing** (`writeInt(length)` + bytes) — надёжный способ определить границу сообщений в TCP. TCP — это поток байт, без границ сообщений; без length-prefix получатель не знает, где заканчивается одно сообщение и начинается следующее.
* **DataInputStream.readFully(...)** — гарантирует, что вы прочитаете ровно `N` байт, иначе бросит `EOFException` если поток закончится раньше.
* **try-with-resources** — автоматически закрывает `Socket` и связанные ресурсы, чтобы не было утечек дескрипторов.
* **flush()** — заставляет отправить данные, особенно если используются буферизованные потоки.

---

# Проблемы, ограничения и места, где код может «сломаться»

1. **Кодировка `String.getBytes()` / `new String(byte[])`**

   * По умолчанию используется системная кодировка. Это может привести к проблемам при запуске на разных ОС. Всегда лучше явно указывать `StandardCharsets.UTF_8`:

     ```java
     byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);
     String clientMessage = new String(buffer, StandardCharsets.UTF_8);
     ```

2. **Ограничения на размер сообщения**

   * Если `messageLength` большой (например, отрицательный из-за ошибки), создание `new byte[messageLength]` может бросить `OutOfMemoryError` или `NegativeArraySizeException`. Стоит проверять разумные пределы:

     ```java
     if (messageLength < 0 || messageLength > MAX_ALLOWED) throw ...
     ```

3. **Сервер обрабатывает клиентов последовательно**

   * Пока один клиент подключён и не завершил работу, остальные ждут в очереди. Для параллельной обработки нужно использовать многопоточность (например, `ExecutorService`).

4. **Никакой аутентификации / шифрования**

   * Для реальных приложений данные идут в открытом виде. Если нужен безопасный канал — использовать TLS (`SSLSocket`) или перенести на HTTPS/SSL.

5. **Нет таймаутов на сокет у сервера/клиента**

   * Если клиент перестал отвечать, сервер может зависнуть при `readInt()` или `readFully()`. Можно установить `socket.setSoTimeout(ms)` и обработать `SocketTimeoutException`.

6. **Нет обработки фрагментированных и больших пакетов**

   * TCP может доставить данные фрагментированными; `readFully` справится, но важно понимать, что один `write` на стороне отправителя не равен одному `read` на стороне получателя.

7. **Исключения не всегда корректно логируются или обрабатываются**

   * Внутренний `catch` в сервере печатает сообщение, но не логирует стек — при отладке полезен `e.printStackTrace()`.

---

# Улучшения и рекомендации (конкретные патчи)

1. **Явная кодировка UTF-8**

   ```java
   byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);
   String serverResponse = new String(responseBuffer, StandardCharsets.UTF_8);
   ```

2. **Проверка длины сообщения**

   ```java
   int messageLength = requestFromClient.readInt();
   if (messageLength < 0 || messageLength > 10_000_000) {
       throw new IOException("Invalid message length: " + messageLength);
   }
   ```

3. **Обработка нескольких клиентов параллельно**

   * Использовать `ExecutorService`:

     ```java
     ExecutorService pool = Executors.newFixedThreadPool(10);
     while (true) {
         Socket s = serverSocket.accept();
         pool.submit(() -> handleClient(s));
     }
     ```

4. **Установка таймаутов**

   ```java
   socket.setSoTimeout(10_000); // 10 sec
   ```

5. **Более дружелюбные логи**

   * Добавить логирование времени и идентификаторов клиентов.

6. **Закрывать потоки явно в catch**

   * try-with-resources уже закрывает `Socket`, но внутренние потоки можно явно не хранить в переменных, а использовать `try (DataInputStream in = new DataInputStream(socket.getInputStream())) { ... }`.

7. **Обрабатывать частичные записи при больших нагрузках**

   * `DataOutputStream.write` обычно пишет всё, но при неблокирующих или специальных сценариях нужно быть аккуратным.

---

# Упражнения для закрепления

1. **Явно использовать UTF-8** — внеси правки и проверь работу.
2. **Добавь проверку длины** — ограничь `messageLength` до 1 MB и протестируй.
3. **Сделай сервер многопоточным** — используй `ExecutorService` и обработчик `handleClient(Socket s)`.
4. **Добавь таймауты** — выставь `setSoTimeout` на серверные сокеты и обработай `SocketTimeoutException`.
5. **Сделай клиент интерактивным** — читай строки из `System.in` и отправляй их серверу в цикле (до команды `exit`).
6. **Добавь логирование** — вместо `System.out.println` используй `java.util.logging` или `slf4j`.

---

# Краткая шпаргалка по API

* `ServerSocket(int port)` — создаёт слушающий серверный сокет.
* `serverSocket.accept()` — блокирует до входящего соединения; возвращает `Socket`.
* `Socket.getInputStream()` / `getOutputStream()` — потоки для чтения/записи.
* `DataOutputStream.writeInt(int)` — записывает 4 байта (big-endian).
* `DataInputStream.readInt()` — читает 4 байта и возвращает `int`.
* `DataInputStream.readFully(byte[])` — блокирующе читает ровно `byte[].length` байт или бросает исключение.
* `try (resource)` — автоматическое закрытие ресурсов (implements `AutoCloseable`).

