    package com.wibowo.simplewebserver;

    import javafx.scene.control.TextArea;

    import java.io.*;
    import java.net.ServerSocket;
    import java.net.Socket;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.Properties;

    public class WebServer {
        private TextArea logTextArea;
        private ServerSocket serverSocket;
        private boolean serverRunning = false;

        // Menyimpan konfigurasi port server, direktori web, direktori log
        private static final String CONFIG_FILE = "config.properties";

        public WebServer(TextArea logTextArea) {
            this.logTextArea = logTextArea;
        }

        public void start(int port, String webDirectory, String logDirectory) {
            new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(port);
                    serverRunning = true;
                    log("Server started on port " + port, logDirectory);
                    while (serverRunning) {
                        Socket clientSocket = serverSocket.accept();
                        handleClientRequest(clientSocket, webDirectory, logDirectory);
                    }
                } catch (IOException e) {
                    log("Error starting server: " + e.getMessage(), logDirectory);
                }
            }).start();
        }

        public void stop(String logDirectory) {
            new Thread(() -> {
                try {
                    serverRunning = false;
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                        log("Server stopped", logDirectory);
                    }
                } catch (IOException e) {
                    log("Error stopping server: " + e.getMessage(), logDirectory);
                }
            }).start();
        }

        private void handleClientRequest(Socket clientSocket, String webDirectory, String logDirectory) {
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String request = in.readLine();
                    log("Request from " + clientSocket.getInetAddress() + ": " + request, logDirectory);

                    String fileName = parseRequest(request);

                    if (fileName.equals("")) {
                        String fileListHTML = generateFileListHTML(webDirectory);
                        byte[] fileContent = fileListHTML.getBytes();
                        sendResponse(clientSocket, fileContent, "text/html", webDirectory);
                    } else {
                        File requestedFile = new File(webDirectory, fileName);
                        if (requestedFile.isDirectory()) {
                            String fileListHTML = generateFileListHTML(requestedFile.getAbsolutePath());
                            byte[] fileContent = fileListHTML.getBytes();
                            sendResponse(clientSocket, fileContent, "text/html", webDirectory);
                        } else {
                            byte[] fileContent = readFileFromWebDirectory(fileName, webDirectory);
                            String contentType = getContentType(fileName);
                            sendResponse(clientSocket, fileContent, contentType, webDirectory);
                        }
                    }

                    clientSocket.close();
                } catch (IOException e) {
                    log("Error handling client request: " + e.getMessage(), logDirectory);
                }
            }).start();
        }

        private void sendResponse(Socket clientSocket, byte[] content, String contentType, String webDirectory) throws IOException {
            OutputStream out = clientSocket.getOutputStream();
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write(("Content-Type: " + contentType + "\r\n").getBytes());
            out.write(("Content-Length: " + content.length + "\r\n").getBytes());
            out.write("\r\n".getBytes());
            out.write(content);
            out.flush();
        }

        private byte[] readFileFromWebDirectory(String fileName, String webDirectory) throws IOException {
            Path filePath = Paths.get(webDirectory, fileName);
            if (Files.exists(filePath) && Files.isReadable(filePath)) {
                return Files.readAllBytes(filePath);
            } else {
                String errorContent = "404 Not Found | The requested resource is not available.";
                return errorContent.getBytes();
            }
        }

        private String parseRequest(String request) {
            String[] parts = request.split(" ");
            String fileName = parts[1].substring(1);
            if (fileName.isEmpty() || fileName.equals("/")) {
                return "";
            }
            return fileName;
        }

        private void log(String message, String logDirectory) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String logMessage = "[" + timestamp + "] " + message + "\n";

            logTextArea.appendText(logMessage);
            saveLogToFile(logMessage, logDirectory);
        }

        private void saveLogToFile(String logMessage, String logDirectory) {
            try {
                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                Path logPath = Paths.get(logDirectory, "log_" + date + ".txt");
                Files.write(logPath, logMessage.getBytes(), logPath.toFile().exists() ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE);
            } catch (IOException e) {
                log("Error saving log to file: " + e.getMessage(), logDirectory);
            }
        }

        // Menyimpan konfigurasi server ke dalam sebuah file properti (C:\Users\Wibowo\OneDrive\Dokumen\Kuliah\Semester 4\PBO\Early2\SimpleWebServer\config.properties)
        public void saveConfig(int port, String webDirectory, String logDirectory) {
            Properties prop = new Properties();
            OutputStream output = null;

            try {
                // Mengatur properti server
                prop.setProperty("port", String.valueOf(port));
                prop.setProperty("webDirectory", webDirectory);
                prop.setProperty("logDirectory", logDirectory);

                // Menyimpan properti ke file config.properties di direktori resources
                output = new FileOutputStream("src/main/resources/config.properties");
                prop.store(output, "Server Configuration");
            } catch (IOException io) {
                io.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Mendapatkan nilai port dari file konfigurasi config.properties
        public int getPort() {
            try {
                Properties prop = new Properties();
                prop.load(App.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
                return Integer.parseInt(prop.getProperty("port"));
            } catch (IOException e) {
                e.printStackTrace();
                return 8080; // Default port
            }
        }

        // Mendapatkan letak file2 web dari file konfigurasi config.properties
        public String getWebDirectory() {
            try {
                Properties prop = new Properties();
                prop.load(App.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
                return prop.getProperty("webDirectory");
            } catch (IOException e) {
                e.printStackTrace();
                return ""; // Default web directory
            }
        }

        // Mendapatkan letak log server dari file konfigurasi config.properties
        public String getLogDirectory() {
            try {
                Properties prop = new Properties();
                prop.load(App.class.getClassLoader().getResourceAsStream(CONFIG_FILE));
                return prop.getProperty("logDirectory");
            } catch (IOException e) {
                e.printStackTrace();
                return ""; // Default log directory
            }
        }

        private String generateFileListHTML(String directoryPath) {
            File directory = new File(directoryPath);
            // Mengambil daftar file yang ada dalam File directory menggunakan metode listFiles() dari objek File. Hasilnya disimpan dalam array files.
            File[] files = directory.listFiles();
            StringBuilder htmlBuilder = new StringBuilder();
            htmlBuilder.append("<html><body><h1>File list of ").append(directoryPath).append("</h1><ul>");

            // Jika bukan di halaman root, tambahkan tautan untuk kembali ke direktori induk
            if (!directoryPath.equals("/")) {
                htmlBuilder.append("<li><a href=\"../\">.. (Back)</a></li>");
            }

            for (File file : files) {
                // Mendapatkan nama file dari objek File saat ini.
                String fileName = file.getName();
                String filePath = directoryPath.equals("/") ? fileName : directoryPath + "/" + fileName; // Membuat path lengkap untuk setiap file. Jika kita berada di halaman root
                if (file.isDirectory()) {
                    // Jika ini adalah direktori, server akan mengirimkan daftar file dalam direktori tersebut sebagai respons
                    htmlBuilder.append("<li><a href=\"").append(fileName).append("/\">").append(fileName).append("</a></li>");
                } else {
                    // Jika ini adalah file,  membaca file tersebut dan mengirimkannya ke klien sebagai respons
                    htmlBuilder.append("<li><a href=\"").append(fileName).append("\">").append(fileName).append("</a></li>");
                }
            }

            htmlBuilder.append("</ul></body></html>");
            return htmlBuilder.toString();
        }

        // Menentukan tipe konten (content type) berdasarkan ekstensi file
        private String getContentType(String fileName) {
            if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                return "text/html";
            } else if (fileName.endsWith(".css")) {
                return "text/css";
            } else if (fileName.endsWith(".js")) {
                return "application/javascript";
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                return "image/png";
            } else if (fileName.endsWith(".gif")) {
                return "image/gif";
            } else if (fileName.endsWith(".pdf")) {
                return "application/pdf";
            } else {
                return "text/plain";
            }
        }
    }
