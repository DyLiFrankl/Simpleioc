import com.t.e.web.DispatcherServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.Servlet;


public class WebServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080); // 端口 8080
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        // 注册 DispatcherServlet
        context.addServlet(new ServletHolder(new DispatcherServlet()), "/*");

        server.setHandler(context);
        server.start();
        server.join(); // 阻塞直到服务器关闭
    }
}
