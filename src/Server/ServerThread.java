package Server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;

public class
ServerThread extends Thread {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter pr;
    private final OutputStream outputStream;

    public
    ServerThread(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.pr = new PrintWriter(this.socket.getOutputStream());
        this.outputStream = this.socket.getOutputStream();
    }
    @Override
    public void
    run() {
        try {
            String input = in.readLine();
            Server.writeInLog("Received Request: "+input);
            if (input == null || input.isEmpty()) return;
            if (input.startsWith("GET")) {
                generateResponse(input);
            } else if (input.startsWith("UPLOAD")) {
                uploadFile(input);
            } else {
                sendNotFoundResponse();
                Server.writeInLog("Sent Response: Invalid request type");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                System.out.println("Closing Socket "+ this.socket.getPort());
                socket.close();
            } catch (IOException e) {
                System.out.println("Error while closing socket");
            }
        }
    }

    private void
    generateResponse(String input) {
        String[] parts = input.trim().split(" ");
        String basePath = parts[1].trim();
        String filePath = basePath.substring(1);
        File file = new File("./root", filePath);
        if (!file.exists()) {
            sendNotFoundResponse();
            Server.writeInLog("Sent Response: "+basePath+" not found");
        } else if (file.isDirectory()) {
            String content = generateContentForDirectory(file, basePath);
            sendHtmlResponse(content);
            Server.writeInLog("Sent Response: "+basePath+" directory accessed");
        } else {
            handleFileResponse(file);
        }
    }

    private String
    getImageContent(File file) throws IOException {
        String fileExtension = getFileExtension(file);

        String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));

        Server.writeInLog("Sent Response: "+file.getName()+" image accessed");

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\t<head>\n" +
                "\t\t<meta http-equiv=\"Content-Type\" content=\""+Files.probeContentType(file.toPath())+"\">\n" +
                "\t\t<title>File Service - " + file.getName() + "</title>\n" +
                "\t\t<link rel=\"icon\" href=\"data:,\">\n" +
                "\t\t<style>\n" +
                "\t\t\tbody {\n" +
                "\t\t\t\tfont-family: 'Segoe UI', Arial, sans-serif;\n" +
                "\t\t\t\tbackground-color: #f5f5f5;\n" +
                "\t\t\t\tmargin: 0;\n" +
                "\t\t\t\tpadding: 20px;\n" +
                "\t\t\t\tdisplay: flex;\n" +
                "\t\t\t\tflex-direction: column;\n" +
                "\t\t\t\talign-items: center;\n" +
                "\t\t\t\tjustify-content: center;\n" +
                "\t\t\t\tmin-height: 100vh;\n" +
                "\t\t\t}\n" +
                "\t\t\t.image-container {\n" +
                "\t\t\t\tbackground-color: white;\n" +
                "\t\t\t\tborder-radius: 8px;\n" +
                "\t\t\t\tbox-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);\n" +
                "\t\t\t\tpadding: 20px;\n" +
                "\t\t\t\tmax-width: 90%;\n" +
                "\t\t\t\ttext-align: center;\n" +
                "\t\t\t}\n" +
                "\t\t\timg {\n" +
                "\t\t\t\tmax-width: 100%;\n" +
                "\t\t\t\theight: auto;\n" +
                "\t\t\t\tborder-radius: 4px;\n" +
                "\t\t\t}\n" +
                "\t\t\th2 {\n" +
                "\t\t\t\tcolor: #333;\n" +
                "\t\t\t\tmargin-bottom: 20px;\n" +
                "\t\t\t}\n" +
                "\t\t\t.back-link {\n" +
                "\t\t\t\tmargin-top: 20px;\n" +
                "\t\t\t\tcolor: #0066cc;\n" +
                "\t\t\t\ttext-decoration: none;\n" +
                "\t\t\t}\n" +
                "\t\t\t.back-link:hover {\n" +
                "\t\t\t\ttext-decoration: underline;\n" +
                "\t\t\t}\n" +
                "\t\t</style>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<div class=\"image-container\">\n" +
                "\t\t\t<h2>" + file.getName() + "</h2>\n" +
                "\t\t\t<img src=\"data:image/" + fileExtension + ";base64," + base64Image + "\" alt=\"" + file.getName() + "\" />\n" +
                "\t\t</div>\n" +
                "\t\t<a href=\"javascript:history.back()\" class=\"back-link\">← Back to directory</a>\n" +
                "\t</body>\n" +
                "</html>";
    }

    private String
    getFileExtension(File file) {
        String fileName = file.getName();
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            return ""; // Empty extension
        }
        return fileName.substring(lastIndexOfDot + 1);
    }

    private String
    getTextContent(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        String line;
        while(( line = br.readLine()) != null ) {
            sb.append( line );
            sb.append( "\r\n" );
        }

        String content = sb.toString();
        Server.writeInLog("Sent Response: "+file.getName()+" text accessed");

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\t<head>\n" +
                "\t\t<meta http-equiv=\"Content-Type\" content=\""+Files.probeContentType(file.toPath())+"\">\n" +
                "\t\t<title>File Service - " + file.getName() + "</title>\n" +
                "\t\t<link rel=\"icon\" href=\"data:,\">\n" +
                "\t\t<style>\n" +
                "\t\t\tbody {\n" +
                "\t\t\t\tfont-family: 'Segoe UI', Arial, sans-serif;\n" +
                "\t\t\t\tbackground-color: #f5f5f5;\n" +
                "\t\t\t\tmargin: 0;\n" +
                "\t\t\t\tpadding: 20px;\n" +
                "\t\t\t}\n" +
                "\t\t\t.container {\n" +
                "\t\t\t\tmax-width: 800px;\n" +
                "\t\t\t\tmargin: 0 auto;\n" +
                "\t\t\t\tbackground-color: white;\n" +
                "\t\t\t\tborder-radius: 8px;\n" +
                "\t\t\t\tbox-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);\n" +
                "\t\t\t\tpadding: 30px;\n" +
                "\t\t\t}\n" +
                "\t\t\th2 {\n" +
                "\t\t\t\tcolor: #333;\n" +
                "\t\t\t\tborder-bottom: 1px solid #eee;\n" +
                "\t\t\t\tpadding-bottom: 10px;\n" +
                "\t\t\t\tmargin-top: 0;\n" +
                "\t\t\t}\n" +
                "\t\t\t.content {\n" +
                "\t\t\t\tline-height: 1.6;\n" +
                "\t\t\t\toverflow-wrap: break-word;\n" +
                "\t\t\t\twhite-space: pre-wrap;\n" +
                "\t\t\t\tpadding: 15px;\n" +
                "\t\t\t\tbackground-color: #f9f9f9;\n" +
                "\t\t\t\tborder-radius: 4px;\n" +
                "\t\t\t}\n" +
                "\t\t\t.back-link {\n" +
                "\t\t\t\tdisplay: inline-block;\n" +
                "\t\t\t\tmargin-top: 20px;\n" +
                "\t\t\t\tcolor: #0066cc;\n" +
                "\t\t\t\ttext-decoration: none;\n" +
                "\t\t\t}\n" +
                "\t\t\t.back-link:hover {\n" +
                "\t\t\t\ttext-decoration: underline;\n" +
                "\t\t\t}\n" +
                "\t\t</style>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<div class=\"container\">\n" +
                "\t\t\t<h2>" + file.getName() + "</h2>\n" +
                "\t\t\t<div class=\"content\">" + content.replace("<", "&lt;").replace(">", "&gt;") + "</div>\n" +
                "\t\t\t<a href=\"javascript:history.back()\" class=\"back-link\">← Back to directory</a>\n" +
                "\t\t</div>\n" +
                "\t</body>\n" +
                "</html>";
    }

    private void
    handleFileResponse(File file) {
        try {
            if(isImageFile(file)){
                String content = getImageContent(file);
                sendHtmlResponse(content);
            } else if (file.getName().endsWith(".txt")) {
                String content = getTextContent(file);
                sendHtmlResponse(content);
            } else {
                sendFileResponse(file, "application/octet-stream", true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void
    sendFileResponse(File file,
                     String contentType,
                     boolean isDownload) throws IOException {
        pr.write("HTTP/1.1 200 OK\r\n");
        pr.write("Server: Java HTTP Server: 1.0\r\n");
        pr.write("Date: " + new Date() + "\r\n");
        pr.write("Content-Type: " + contentType + "\r\n");
        pr.write("Content-Length: " + file.length() + "\r\n");
        if (isDownload) {
            pr.write("Content-Disposition: attachment; filename=\"" + file.getName() + "\"\r\n");
        }
        pr.write("\r\n");
        pr.flush();

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
        Server.writeInLog("Sent Response: "+ file.getName() +" sent for download");

    }

    private void
    sendHtmlResponse(String content) {
        pr.write("HTTP/1.1 200 OK\r\n");
        pr.write("Server: Java HTTP Server: 1.0\r\n");
        pr.write("Date: " + new Date() + "\r\n");
        pr.write("Content-Type: text/html\r\n");
        pr.write("Content-Length: " + content.length() + "\r\n");
        pr.write("\r\n");
        pr.write(content);
        pr.flush();

    }

    private void
    sendNotFoundResponse() {
        String notFoundContent = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\t<head>\n" +
                "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "\t\t<title>404 Not Found</title>\n" +
                "\t\t<link rel=\"icon\" href=\"data:,\">\n" +
                "\t\t<style>\n" +
                "\t\t\tbody {\n" +
                "\t\t\t\tfont-family: 'Segoe UI', Arial, sans-serif;\n" +
                "\t\t\t\tbackground-color: #f5f5f5;\n" +
                "\t\t\t\tmargin: 0;\n" +
                "\t\t\t\tpadding: 0;\n" +
                "\t\t\t\tdisplay: flex;\n" +
                "\t\t\t\tjustify-content: center;\n" +
                "\t\t\t\talign-items: center;\n" +
                "\t\t\t\theight: 100vh;\n" +
                "\t\t\t}\n" +
                "\t\t\t.error-container {\n" +
                "\t\t\t\ttext-align: center;\n" +
                "\t\t\t\tpadding: 40px;\n" +
                "\t\t\t\tbackground-color: white;\n" +
                "\t\t\t\tborder-radius: 8px;\n" +
                "\t\t\t\tbox-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);\n" +
                "\t\t\t}\n" +
                "\t\t\th1 {\n" +
                "\t\t\t\tfont-size: 72px;\n" +
                "\t\t\t\tcolor: #e74c3c;\n" +
                "\t\t\t\tmargin: 0;\n" +
                "\t\t\t}\n" +
                "\t\t\tp {\n" +
                "\t\t\t\tfont-size: 18px;\n" +
                "\t\t\t\tcolor: #555;\n" +
                "\t\t\t\tmargin: 20px 0 30px;\n" +
                "\t\t\t}\n" +
                "\t\t\t.home-link {\n" +
                "\t\t\t\tdisplay: inline-block;\n" +
                "\t\t\t\tpadding: 10px 20px;\n" +
                "\t\t\t\tbackground-color: #3498db;\n" +
                "\t\t\t\tcolor: white;\n" +
                "\t\t\t\ttext-decoration: none;\n" +
                "\t\t\t\tborder-radius: 4px;\n" +
                "\t\t\t\ttransition: background-color 0.3s;\n" +
                "\t\t\t}\n" +
                "\t\t\t.home-link:hover {\n" +
                "\t\t\t\tbackground-color: #2980b9;\n" +
                "\t\t\t}\n" +
                "\t\t</style>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<div class=\"error-container\">\n" +
                "\t\t\t<h1>404</h1>\n" +
                "\t\t\t<p>The requested resource could not be found on this server.</p>\n" +
                "\t\t\t<a href=\"/\" class=\"home-link\">Return to Home</a>\n" +
                "\t\t</div>\n" +
                "\t</body>\n" +
                "</html>";

        pr.write("HTTP/1.1 404 Not Found\r\n");
        pr.write("Server: Java HTTP Server: 1.0\r\n");
        pr.write("Date: " + new Date() + "\r\n");
        pr.write("Content-Type: text/html\r\n");
        pr.write("Content-Length: " + notFoundContent.length() + "\r\n");
        pr.write("\r\n");
        pr.write(notFoundContent);
        pr.flush();
    }

    private String
    generateContentForDirectory(File directory, String basePath) {
        File[] files = directory.listFiles();
        StringBuilder listItems = new StringBuilder();

        if (files != null) {
            for (File file : files) {
                String relativePath = basePath + (basePath.endsWith("/") ? "" : "/") + file.getName();
                String fileIcon = getFileIcon(file);

                if (file.isDirectory()) {
                    listItems.append("<li class=\"directory-item\">\n")
                            .append("\t<div class=\"file-icon\">📁</div>\n")
                            .append("\t<a href=\"").append(relativePath).append("\">")
                            .append(file.getName()).append("</a>\n")
                            .append("</li>\n");
                } else if (isImageFile(file)) {
                    listItems.append("<li class=\"file-item image-file\">\n")
                            .append("\t<div class=\"file-icon\">").append(fileIcon).append("</div>\n")
                            .append("\t<a href=\"").append(relativePath).append("\">")
                            .append(file.getName()).append("</a>\n")
                            .append("</li>\n");
                } else if (file.getName().endsWith(".txt")) {
                    listItems.append("<li class=\"file-item text-file\">\n")
                            .append("\t<div class=\"file-icon\">").append(fileIcon).append("</div>\n")
                            .append("\t<a href=\"").append(relativePath).append("\">")
                            .append(file.getName()).append("</a>\n")
                            .append("</li>\n");
                } else {
                    listItems.append("<li class=\"file-item other-file\">\n")
                            .append("\t<div class=\"file-icon\">").append(fileIcon).append("</div>\n")
                            .append("\t<a href=\"").append(relativePath).append("\" download>")
                            .append(file.getName()).append("</a>\n")
                            .append("</li>\n");
                }
            }
        }

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "\t<head>\n" +
                "\t\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
                "\t\t<title>File Service - " + directory.getName() + "</title>\n" +
                "\t\t<link rel=\"icon\" href=\"data:,\">\n" +
                "\t\t<style>\n" +
                "\t\t\tbody {\n" +
                "\t\t\t\tfont-family: 'Segoe UI', Arial, sans-serif;\n" +
                "\t\t\t\tbackground-color: #f5f5f5;\n" +
                "\t\t\t\tmargin: 0;\n" +
                "\t\t\t\tpadding: 20px;\n" +
                "\t\t\t}\n" +
                "\t\t\t.container {\n" +
                "\t\t\t\tmax-width: 900px;\n" +
                "\t\t\t\tmargin: 0 auto;\n" +
                "\t\t\t\tbackground-color: white;\n" +
                "\t\t\t\tborder-radius: 8px;\n" +
                "\t\t\t\tbox-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);\n" +
                "\t\t\t\tpadding: 30px;\n" +
                "\t\t\t}\n" +
                "\t\t\th1 {\n" +
                "\t\t\t\tcolor: #333;\n" +
                "\t\t\t\tborder-bottom: 1px solid #eee;\n" +
                "\t\t\t\tpadding-bottom: 10px;\n" +
                "\t\t\t\tmargin-top: 0;\n" +
                "\t\t\t}\n" +
                "\t\t\t.file-list {\n" +
                "\t\t\t\tlist-style-type: none;\n" +
                "\t\t\t\tpadding: 0;\n" +
                "\t\t\t}\n" +
                "\t\t\t.file-list li {\n" +
                "\t\t\t\tdisplay: flex;\n" +
                "\t\t\t\talign-items: center;\n" +
                "\t\t\t\tpadding: 12px;\n" +
                "\t\t\t\tborder-bottom: 1px solid #eee;\n" +
                "\t\t\t\ttransition: background-color 0.2s;\n" +
                "\t\t\t}\n" +
                "\t\t\t.file-list li:hover {\n" +
                "\t\t\t\tbackground-color: #f9f9f9;\n" +
                "\t\t\t}\n" +
                "\t\t\t.file-list li:last-child {\n" +
                "\t\t\t\tborder-bottom: none;\n" +
                "\t\t\t}\n" +
                "\t\t\t.file-icon {\n" +
                "\t\t\t\tfont-size: 20px;\n" +
                "\t\t\t\tmargin-right: 15px;\n" +
                "\t\t\t\twidth: 24px;\n" +
                "\t\t\t\ttext-align: center;\n" +
                "\t\t\t}\n" +
                "\t\t\t.directory-item {\n" +
                "\t\t\t\tfont-weight: bold;\n" +
                "\t\t\t}\n" +
                "\t\t\t.file-list a {\n" +
                "\t\t\t\tcolor: #0066cc;\n" +
                "\t\t\t\ttext-decoration: none;\n" +
                "\t\t\t\tflex-grow: 1;\n" +
                "\t\t\t}\n" +
                "\t\t\t.file-list a:hover {\n" +
                "\t\t\t\ttext-decoration: underline;\n" +
                "\t\t\t}\n" +
                "\t\t\t.breadcrumb {\n" +
                "\t\t\t\tpadding: 8px 0;\n" +
                "\t\t\t\tmargin-bottom: 20px;\n" +
                "\t\t\t\tcolor: #666;\n" +
                "\t\t\t}\n" +
                "\t\t\t.breadcrumb a {\n" +
                "\t\t\t\tcolor: #0066cc;\n" +
                "\t\t\t\ttext-decoration: none;\n" +
                "\t\t\t}\n" +
                "\t\t\t.breadcrumb a:hover {\n" +
                "\t\t\t\ttext-decoration: underline;\n" +
                "\t\t\t}\n" +
                "\t\t</style>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<div class=\"container\">\n" +
                "\t\t\t<div class=\"breadcrumb\">\n" +
                "\t\t\t\t<a href=\"/\">Home</a>" + generateBreadcrumb(basePath) + "\n" +
                "\t\t\t</div>\n" +
                "\t\t\t<h1>Directory: " + (directory.getName().isEmpty() ? "Root" : directory.getName()) + "</h1>\n" +
                "\t\t\t<ul class=\"file-list\">\n" +
                listItems.toString() +
                "\t\t\t</ul>\n" +
                "\t\t</div>\n" +
                "\t</body>\n" +
                "</html>";
    }

    private String
    generateBreadcrumb(String path) {
        if (path.equals("/")) return "";

        StringBuilder breadcrumb = new StringBuilder();
        String[] parts = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String part : parts) {
            if (part.isEmpty()) continue;
            currentPath.append("/").append(part);
            breadcrumb.append(" > <a href=\"").append(currentPath).append("\">").append(part).append("</a>");
        }

        return breadcrumb.toString();
    }

    private String
    getFileIcon(File file) {
        if (file.isDirectory()) {
            return "📁";
        }

        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") ||
                fileName.endsWith(".gif") || fileName.endsWith(".bmp")) {
            return "🖼️";
        } else if (fileName.endsWith(".txt")) {
            return "📄";
        } else if (fileName.endsWith(".pdf")) {
            return "📑";
        } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav")) {
            return "🎵";
        } else if (fileName.endsWith(".mp4") || fileName.endsWith(".avi") || fileName.endsWith(".mov")) {
            return "🎬";
        } else if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
            return "🗜️";
        } else if (fileName.endsWith(".exe")) {
            return "⚙️";
        } else {
            return "📎";
        }
    }

    private boolean
    isImageFile(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") ||
                fileName.endsWith(".gif") || fileName.endsWith(".bmp");
    }

    private void
    uploadFile(String input) {
        String[] tokens = input.split(" ");
        if (tokens.length < 2) {
            pr.write("Bad Request\r\n");
            pr.flush();
            return;
        }
        String fileName = tokens[1];
        boolean valid = fileName.endsWith(".txt") ||
                fileName.endsWith(".png") ||
                fileName.endsWith(".jpg") ||
                fileName.endsWith(".mp4");
        if(!valid){
            System.out.println("Invalid File Format");
            Server.writeInLog("Sent Response: Invalid File Format");
            pr.write("Invalid File Format\r\n");
            pr.flush();
            return;
        }

        pr.write("Valid\r\n");
        pr.flush();
        File file = new File("./root/uploaded", fileName);

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            byte[] buffer = new byte[4096]; // Increased buffer size for faster transfers
            InputStream ins = this.socket.getInputStream();
            int bytesRead;

            while ((bytesRead = ins.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead); // Writing bytes directly without converting to string
            }
            bos.flush(); // Ensure all data is written to file

            pr.write(fileName+" Sent Successfully\r\n");
            pr.flush();
            Server.writeInLog("Sent Response: uploaded file " + file.getName() + " successfully.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}