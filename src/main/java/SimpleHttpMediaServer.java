import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * Usage: localhost:8000/media?url=path/to/your/media
 */
public class SimpleHttpMediaServer {

    private static final int PORT = 8000;
    private static final String CONTEXT = "/media";
    private static final String URL_PRAM = "url";

    public static void main(String[] args) throws Exception {
        int port = getServerPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(CONTEXT, new FileHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Started, Listening on port: " + port);
    }

    private static int getServerPort() {
        String port = System.getProperty("server.port");
        if (StringUtils.isEmpty(port) || !StringUtils.isNumeric(port)) {
            return PORT;
        }
        return Integer.parseInt(port);
    }

    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String mediaUrl = "";
            String query = t.getRequestURI().getQuery();
            String[] params = query.split("&");
            for (String param : params) {
                String[] split = param.split("=");
                if (split.length == 2 && URL_PRAM.equals(split[0])) {
                    mediaUrl = split[1];
                }
            }
            if (StringUtils.isEmpty(mediaUrl)) {
                String response = URL_PRAM + " param is missing";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                OutputStream os = t.getResponseBody();
                File file = new File(mediaUrl);
                t.sendResponseHeaders(200, file.length());
                InputStream in = new FileInputStream(file);
                IOUtils.copy(in,os);
                in.close();
                os.close();
            }
        }
    }
}
