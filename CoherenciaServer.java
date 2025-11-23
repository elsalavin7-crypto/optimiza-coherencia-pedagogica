import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.zip.*;

public class CoherenciaServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            File htmlFile = new File("index.html");
            byte[] bytes = Files.readAllBytes(htmlFile.toPath());
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        });
        server.createContext("/analizar", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
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
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=Informe_Coherencia_Completo.zip");
                exchange.sendResponseHeaders(200, zipData.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(zipData); }
            }
        });
        server.start();
        System.out.println("Servidor en http://localhost:" + port);
        System.in.read();
        server.stop(0);
    }
}