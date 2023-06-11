package com.alKich;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.servlet.ServletContext;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerEndpointConfig;

public class Main {
    public static void main(String[] args) throws Exception {
        // Создание сервера Jetty
        Server server = new Server();
        // Создание коннектора сервера Jetty
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.addConnector(connector);

        // Создание контекста сервлета
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        server.setHandler(contextHandler);

        // Создание конфигуратора конечной точки сервера WebSocket
        final ServerEndpointConfig.Configurator configurator = new ServerEndpointConfig.Configurator();
        contextHandler.addBean(configurator);

        // Конфигурация контейнера сервера WebSocket
        WebSocketServerContainerInitializer.configure(contextHandler, new WebSocketServerContainerInitializer.Configurator() {
            @Override
            public void accept(ServletContext servletContext, ServerContainer wsContainer) throws DeploymentException {
                // Регистрация конечной точки WebSocket
                wsContainer.addEndpoint(ServerEndpointConfig.Builder.create(RandomNumberEndpoint.class, "/random").configurator(configurator).build());
            }
        });

        // Запуск сервера
        server.start();
        server.join();
    }
}
