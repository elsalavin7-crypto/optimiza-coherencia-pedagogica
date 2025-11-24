import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.zip.*;

public class CoherenciaServer {
    public static void main(String[] args) throws Exception {

        // Puerto dinámico (necesario para Render)
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // ---------------------- Página principal ----------------------
        server.createContext("/", exchange -> {
            try {
                File htmlFile = new File("index.html");
                if (!htmlFile.exists()) {
                    String msg = "<h1>Error 404: index.html no encontrado</h1>";
                    exchange.sendResponseHeaders(404, msg.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(msg.getBytes());
                    }
                    return;
                }
                byte[] bytes = Files.readAllBytes(htmlFile.toPath());
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String msg = "<h1>Error interno del servidor</h1><pre>" + e.getMessage() + "</pre>";
                exchange.sendResponseHeaders(500, msg.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(msg.getBytes());
                }
            }
        });

        // ---------------------- Procesamiento del PDI ----------------------
        server.createContext("/analizar", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    Files.createDirectories(Paths.get("output"));
                    Path informe = Paths.get("output/Informe.docx");
                    Path matriz = Paths.get("output/Matriz.docx");
                    Path resumen = Paths.get("output/Resumen.docx");

                    Files.writeString(informe, "Informe de coherencia pedagógica generado automáticamente.");
                    Files.writeString(matriz, "Matriz de coherencia pedagógica generada automáticamente.");
                    Files.writeString(resumen, "Resumen de observaciones y sugerencias.");

                    Path zipFile = Paths.get("output/Informe_Coherencia_Completo.zip");
                    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                        for (Path file : new Path[]{informe, matriz, resumen}) {
                            zos.putNextEntry(new ZipEntry(file.getFileName().toString()));
                            byte[] data = Files.readAllBytes(file);
                            zos.write(data, 0, data.length);
                            zos.closeEntry();
                        }
                    }

                    byte[] zipData = Files.readAllBytes(zipFile);
                    exchange.getResponseHeaders().set("Content-Type", "application/zip");
                    exchange.getResponseHeaders().set("Content-Disposition",
                            "attachment; filename=Informe_Coherencia_Completo.zip");
                    exchange.sendResponseHeaders(200, zipData.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(zipData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    String msg = "Error al generar el archivo ZIP: " + e.getMessage();
                    exchange.sendResponseHeaders(500, msg.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(msg.getBytes());
                    }
                }
            }
        });

        // ---------------------- Iniciar servidor ----------------------
        server.start();
        System.out.println("Servidor en http://localhost:" + port);

        // Mantener el servidor activo indefinidamente
        while (true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
