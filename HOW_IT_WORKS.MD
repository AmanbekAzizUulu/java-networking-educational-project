# Подробное руководство по коду `Main.java` (Markdown для обучения)

Ниже — готовый к чтению и удобный для изучения Markdown-файл, который подробно объясняет каждую часть программы, почему так сделано, какие есть допущения и ошибки, и какие улучшения можно сделать. Ты можешь сохранить этот текст как `README.md` и учиться по нему.

---

# Оглавление

- [Подробное руководство по коду `Main.java` (Markdown для обучения)](#подробное-руководство-по-коду-mainjava-markdown-для-обучения)
- [Оглавление](#оглавление)
- [Краткая суть программы](#краткая-суть-программы)
- [Какие классы и пакеты используются](#какие-классы-и-пакеты-используются)
- [Общий поток выполнения (high-level)](#общий-поток-выполнения-high-level)
- [Подробный разбор по методам](#подробный-разбор-по-методам)
	- [`public static void main(String[] args)`](#public-static-void-mainstring-args)
	- [`private static String fetchUrl(String host, String path) throws IOException`](#private-static-string-fetchurlstring-host-string-path-throws-ioexception)
	- [`private static String readHttpResponse(InputStream in) throws IOException`](#private-static-string-readhttpresponseinputstream-in-throws-ioexception)
		- [Что делает:](#что-делает)
	- [`private static void readFixedBody(InputStream in, OutputStream out, int length) throws IOException`](#private-static-void-readfixedbodyinputstream-in-outputstream-out-int-length-throws-ioexception)
	- [`private static void readChunkedBody(InputStream in, OutputStream out) throws IOException`](#private-static-void-readchunkedbodyinputstream-in-outputstream-out-throws-ioexception)
	- [`private static Map<String, String> parseHeaders(String headersStr)`](#private-static-mapstring-string-parseheadersstring-headersstr)
	- [`private static void saveToFile(String content, String filename)`](#private-static-void-savetofilestring-content-string-filename)
- [Как работает чтение HTTP (важные детали)](#как-работает-чтение-http-важные-детали)
	- [Заголовки и тело](#заголовки-и-тело)
	- [`Content-Length` vs `Transfer-Encoding: chunked`](#content-length-vs-transfer-encoding-chunked)
- [Что в коде сделано хорошо и что можно улучшить — баги и риски](#что-в-коде-сделано-хорошо-и-что-можно-улучшить--баги-и-риски)
		- [Хорошо:](#хорошо)
		- [Ограничения / баги:](#ограничения--баги)
- [Практические советы по модификации и улучшению](#практические-советы-по-модификации-и-улучшению)
- [Короткие упражнения для самопроверки](#короткие-упражнения-для-самопроверки)
- [Заключение](#заключение)

---

# Краткая суть программы

Программа делает два HTTP-запроса низкого уровня (через `Socket`) на порт 80:

1. Запрос к `google.com` по пути `/` — содержание ответа сохраняется в `src/main/resources/redirect_response.html`.
2. Запрос к `www.google.com` по пути `/` — содержание ответа сохраняется в `src/main/resources/final_response.html`.

Она вручную формирует HTTP-заголовки, читает ответ по байтам, парсит заголовки, определяет способ чтения тела (по `Content-Length`, по `chunked` или до EOF), и сохраняет тело в файл в кодировке UTF-8.

---

# Какие классы и пакеты используются

* `java.net.Socket` — низкоуровневое TCP-соединение.
* `java.io.*` — потоки ввода/вывода: `InputStream`, `OutputStream`, `BufferedInputStream`, `BufferedReader`, `ByteArrayOutputStream`, `BufferedWriter`.
* `java.nio.file.*` — операции с файловой системой: `Paths`, `Files`, `Path`.
* `java.nio.charset.StandardCharsets` — кодировки (UTF-8).
* `java.util.*` — контейнеры (`Map`, `HashMap`).

---

# Общий поток выполнения (high-level)

1. `main` вызывает `fetchUrl("google.com", "/")` → получает строку (HTML) → сохраняет в файл.
2. `main` вызывает `fetchUrl("www.google.com", "/")` → получает строку → сохраняет.
3. `fetchUrl`:

   * создаёт `Socket(host, 80)` и таймаут `socket.setSoTimeout(5000)`,
   * формирует и отправляет HTTP-запрос (строкой, с `\r\n`),
   * получает `InputStream` и передаёт его в `readHttpResponse`.
4. `readHttpResponse`:

   * читает заголовки до последовательности `\r\n\r\n`,
   * парсит заголовки в `Map`,
   * по заголовкам выбирает стратегию чтения тела:

     * `chunked` → `readChunkedBody`,
     * `content-length` → `readFixedBody`,
     * иначе → читаем до EOF (`bin.transferTo(body)`).
5. Полученное тело конвертируется в строку UTF-8 и возвращается вверх.
6. `saveToFile` сохраняет строку в `src/main/resources/<filename>`.

---

# Подробный разбор по методам

## `public static void main(String[] args)`

* Запускает программу.
* Блок `try { ... } catch (Exception e)` — ловит любые исключения и печатает стек.
* Вызывает `fetchUrl` два раза и `saveToFile` для результатов.
* `System.out.println("✓ Done.");` — индикатор завершения.

**Зачем два запроса?** — В примере автор сначала проверяет ответ от `google.com` (часто это редирект), затем делал запрос явно к `www.google.com`. Это демонстрация, а не строгая логика следования редиректам.

---

## `private static String fetchUrl(String host, String path) throws IOException`

* Создаёт TCP-сокет к `host` на порту `80`.
* Устанавливает `socket.setSoTimeout(5000)` — 5 секунд таймаут чтения; это предотвращает бесконечное зависание в `read`.
* Формирует HTTP-запрос в виде текста:

  ```
  GET /path HTTP/1.1\r\n
  Host: host\r\n
  User-Agent: JavaRawSocket\r\n
  Connection: close\r\n\r\n
  ```

  — и записывает его в `OutputStream` сокета в кодировке UTF-8.
* Затем вызывает `readHttpResponse(in)` для чтения и парсинга ответа.
* Закрывает сокет автоматически (try-with-resources).

**Замечание:** Для корректности запроса лучше:

* использовать кодировку ASCII для заголовков (`US-ASCII`);
* явно формировать CRLF (`\r\n`), что уже сделано.

---

## `private static String readHttpResponse(InputStream in) throws IOException`

### Что делает:

1. **Чтение заголовков**

   * Использует `BufferedInputStream` и читает байт за байтом в `ByteArrayOutputStream headersBuf`, пока не встретит последовательность `\r\n\r\n` (конец заголовков).
   * Это надёжнее, чем `readLine()`, т.к. гарантированно ловит точную границу `\r\n\r\n`.

2. **Преобразование заголовков в строку**

   * `headersStr = headersBuf.toString(StandardCharsets.UTF_8)` — печатает заголовки в консоль.

3. **Парсинг заголовков**

   * `parseHeaders` делает `Map<lowercase-header-name, value>`.

4. **Определение способа чтения тела**

   * Если `transfer-encoding: chunked` — вызывается `readChunkedBody`.
   * Иначе если есть `content-length` — вызывается `readFixedBody(length)`.
   * Иначе — читаем всё до EOF (`bin.transferTo(body)`).

5. **Возврат** — тело конвертируется в UTF-8 строку `body.toString(StandardCharsets.UTF_8)`.

**Важно:** Конвертация тела в UTF-8 предполагает, что сервер вернул HTML в UTF-8. На практике тело может быть в другой кодировке (см. `Content-Type: text/html; charset=...`) — это упрощение в учебных целях.

---

## `private static void readFixedBody(InputStream in, OutputStream out, int length) throws IOException`

* Читает ровно `length` байт из `in` в `out` (буфер 4096 байт).
* Контролирует общее прочитанное в `total`.

**Почему это нужно?** — Когда сервер указывает `Content-Length: N`, нужно прочитать ровно `N` байт тела, а не до EOF (потому что соединение может оставаться открыным для других целей).

---

## `private static void readChunkedBody(InputStream in, OutputStream out) throws IOException`

* Реализует чтение по протоколу **chunked transfer encoding** (HTTP/1.1).
* Алгоритм:

  1. Оборачиваем `InputStream` в `BufferedReader(new InputStreamReader(in, UTF-8))` и читаем строку `sizeLine` (HEX число длины чанка).
  2. Преобразуем `sizeLine` в `chunkSize = Integer.parseInt(sizeLine.trim(), 16)`.
  3. Если `chunkSize == 0` — конец тела чанков.
  4. Иначе читаем `chunkSize` символов с помощью `reader.read(...)` в `char[] chunk`.
  5. Пишем эти символы в `out` (после кодирования в UTF-8).
  6. Пропускаем строку `reader.readLine()` — это CRLF после чанка.
  7. Повторяем.

**Ограничения и предупреждения:**

* В спецификации chunked размер считывается в *байтах*, а не в символах. Использование `Reader` и `char[]` — упрощение для текстовых тел; если тело содержит multi-byte символы (UTF-8) или двоичные данные, читать побайтно через `InputStream` и записывать строго `chunkSize` байт — правильнее.
* Также после последнего чанка сервер может отправить трейлеры (доп. заголовки) — код читает одну пустую строку и выходит; трейлеры игнорируются.

---

## `private static Map<String, String> parseHeaders(String headersStr)`

* Делит `headersStr` по `\r\n`.
* Для каждой строки с `:` берёт имя заголовка (до `:`) и значение (после `:`), кладёт в `Map` с ключом в нижнем регистре.
* Возвращает `Map` заголовков.

**Замечание:** Не обрабатывает статусную строку (`HTTP/1.1 200 OK`) — она остаётся в первой строке headersStr. Но это не мешает обычной работе; можно отдельно распарсить статус при желании.

---

## `private static void saveToFile(String content, String filename)`

* Создаёт директорию `src/main/resources` (если не существует).
* Пишет содержимое `content` в файл `src/main/resources/filename` в UTF-8.
* Печатает место сохранения или ошибку.

**Замечание:** Запись в `src/main/resources` удобна во время разработки, но не всегда корректна для приложения в production (например, при запуске из jar). Можно сохранять в `user.dir` или `java.io.tmpdir`.

---

# Как работает чтение HTTP (важные детали)

## Заголовки и тело

* HTTP/1.1 ответ состоит из:

  1. статусной строки: `HTTP/1.1 200 OK\r\n`
  2. набора заголовков: `Header-Name: value\r\n` ...
  3. пустой строки `\r\n` (конец заголовков)
  4. тело ответа (HTML, JSON, байты)

Ключ — найти границу `\r\n\r\n` и затем корректно прочитать тело.

## `Content-Length` vs `Transfer-Encoding: chunked`

* Если сервер указывает `Content-Length: N` — тело имеет ровно `N` байт. Читаем точно `N` байт.
* Если `Transfer-Encoding: chunked` — тело разбито на чанки. Формат:

  ```
  <hex-size>\r\n
  <N байт данных>\r\n
  <hex-size>\r\n
  ...
  0\r\n
  (опциональные трейлеры)\r\n
  \r\n
  ```
* Если ни того ни другого — иногда читают до закрытия соединения (EOF).

---

# Что в коде сделано хорошо и что можно улучшить — баги и риски

### Хорошо:

* Чтение заголовков реализовано корректно и надёжно (по байтам до `\r\n\r\n`).
* Распознавание `chunked` и `content-length` реализовано, а значит код работает для многих серверов.
* Таймаут сокета предотвращает зависание.
* Логирование заголовков и шагов помогает учиться и отлаживать.

### Ограничения / баги:

1. **Chunked читает символы, а не байты.** Для UTF-8 и двоичных тел это может привести к ошибкам. Правильнее читать `chunkSize` байт с `InputStream`.
2. **Кодировка тела всегда UTF-8.** На практике заголовок `Content-Type` может указывать другую кодировку; надо смотреть `charset`.
3. **Нет обработки HTTPS.** Для `https://` нужен TLS (`SSLSocket` или `HttpClient`).
4. **Не обрабатывается статус-код.** Код не смотрит `HTTP/1.1 301` и не следует редиректу автоматически.
5. **Трейлеры в chunked игнорируются.**
6. **Запись в `src/main/resources`** — неудобно для production.

---

# Практические советы по модификации и улучшению

1. **Правильное чтение chunked (по байтам)**

   * Читай строку с размером чанка, затем `N` *байт* из `InputStream` и записывай их в `body` без преобразования через `Reader`.

2. **Учитывать `Content-Encoding`**

   * Если заголовок `Content-Encoding: gzip` — оборачивай `body` в `GZIPInputStream` и распаковывай.

3. **Определять кодировку тела**

   * Заголовок `Content-Type: text/html; charset=...` — используй указанный charset для преобразования байт в строку.

4. **Следовать редиректам**

   * После получения статус-кода 301/302/307 и заголовка `Location` — использовать `URI.resolve()` и повторять запрос (до ограничения, например 5 редиректов).

5. **Поддержка HTTPS**

   * Либо использовать `SSLSocketFactory`/`SSLSocket`, либо перейти на `java.net.http.HttpClient` (рекомендуется).

6. **Использовать `HttpClient` (JDK11+)**

   * Если цель — реальная сеть и надёжность, `HttpClient` решает все проблемы: TLS, редиректы, chunked, Content-Encoding и кодировки.

---

# Короткие упражнения для самопроверки

1. Измени `readChunkedBody` так, чтобы он читал чанки **по байтам**, а не по символам. Проверь на сайтах, возвращающих chunked.
2. Добавь в `readHttpResponse` чтение первой строки (статусной) и выведи статус-код. Сделай логику: если код 301/302, прочитать `Location` и следовать редиректу (максимум 5 раз).
3. Модифицируй `saveToFile` чтобы по умолчанию сохранять в `System.getProperty("java.io.tmpdir")` — проверь, где файлы окажутся.
4. Попробуй выполнить запрос к `httpbin.org/encoding/utf8` и сравни, правильно ли ты определил и вывел кодировку.

---

# Заключение

Этот код — отличный учебный пример работы с TCP-сокетами и базовыми механизмами HTTP: ручная отправка запросов, чтение заголовков, разбор `Content-Length` и `chunked`. В реальных приложениях большинство этих задач уже решено в `HttpClient` или сторонних библиотеках, но ручная реализация даёт глубокое понимание протокола.

Если хочешь, я:

* подготовлю **готовый патч** — исправлю `readChunkedBody` на чтение по байтам + обработку gzip + определение кодировки из `Content-Type`; **или**
* перепишу пример на `java.net.http.HttpClient` с поддержкой HTTPS и автоматическими редиректами.

Какой вариант предпочитаешь — исправленный низкоуровневый или современный `HttpClient`?
