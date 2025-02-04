// chcp 65001 Para activar los emotes

// 🔎 ¿Qué hace este programa?
// Este programa es un escáner de puertos en red que permite detectar qué puertos están abiertos en un servidor o dispositivo. 
// Usa conexiones TCP para probar si un puerto responde, y al final muestra un listado con todos los puertos abiertos y su servicio asociado.

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class AdvancedPortScanner {
    private static final int THREAD_POOL_SIZE = 32; // Número de hilos concurrentes
    private static final int TOTAL_PORTS = 1024; // Total de puertos a escanear
    private static final AtomicInteger scannedPorts = new AtomicInteger(0); // Contador de progreso
    private static final List<String> openPorts = Collections.synchronizedList(new ArrayList<>()); // Lista de puertos abiertos

    private static final Map<Integer, String> SERVICES = new HashMap<>();

    //colores en la consola
    private static final String GREEN = "\u001B[32m"; // Verde para puertos abiertos
    private static final String RED = "\u001B[31m";   // Rojo para puertos cerrados
    private static final String CYAN = "\u001B[36m";  // Cian para la barra de progreso
    private static final String RESET = "\u001B[0m";  // Resetear color

    static {
        SERVICES.put(21, "FTP 📂");
        SERVICES.put(22, "SSH 🔐");
        SERVICES.put(25, "SMTP (Correo) 📧");
        SERVICES.put(53, "DNS 🌎");
        SERVICES.put(80, "HTTP (Web) 🌍");
        SERVICES.put(110, "POP3 (Correo) 📩");
        SERVICES.put(143, "IMAP (Correo) 📩");
        SERVICES.put(443, "HTTPS (Web Segura) 🔒");
        SERVICES.put(3306, "MySQL 🗄️");
        SERVICES.put(3389, "Escritorio Remoto (RDP) 🖥️");
    }

    public static void main(String[] args) {
        String target = "scanme.nmap.org"; // scanme.nmap.org  192.168.1.1   portquiz.net
        int startPort = 1;
        int endPort = TOTAL_PORTS; // Escanea los puertos más comunes

        System.out.println("🔎 Escaneando puertos en " + target + "...\n");

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        for (int port = startPort; port <= endPort; port++) {
            int currentPort = port;
            executor.execute(() -> scanPort(target, currentPort, endPort));
        }

        executor.shutdown();
    }

    private static void scanPort(String target, int port, int totalPorts) {
        try (Socket socket = new Socket(target, port)) {
            String service = SERVICES.getOrDefault(port, "Desconocido ❓");
            String result = "✅ Puerto " + port + " Abierto → " + service;
            openPorts.add(result); // Guardar puerto abierto
            System.out.println(GREEN + result + RESET);
        } catch (IOException e) {
            System.out.println(RED + "❌ Puerto " + port + " Cerrado 🚫" + RESET);
        }

        // Actualizar la barra de progreso cada 32 puertos escaneados
        updateProgress(totalPorts);
    }

    private static synchronized void updateProgress(int totalPorts) {
        int progress = scannedPorts.incrementAndGet(); // Aumentar el contador

        if (progress % 32 == 0 || progress == totalPorts) { // Solo actualizar cada 32 puertos
            int percentage = (progress * 100) / totalPorts;
            int barLength = 30; // Largo de la barra de progreso
            int filled = (progress * barLength) / totalPorts;
            String bar = "█".repeat(filled) + "-".repeat(barLength - filled);

            System.out.print("\r" + CYAN + "📊 Progreso: [" + bar + "] " + percentage + "% " + RESET + "\n");
            
            // Cuando termina el escaneo, mostrar resultados
            if (progress == totalPorts) {
                showOpenPorts();
            }
        }
    }

    private static void showOpenPorts() {
        System.out.println("\n🎯 Escaneo completado con éxito.");
        if (openPorts.isEmpty()) {
            System.out.println("❌ No se encontraron puertos abiertos.");
        } else {
            System.out.println("\n📋 Lista de puertos abiertos:");
            openPorts.forEach(System.out::println);
        }
    }
}
