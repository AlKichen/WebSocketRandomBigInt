package com.alKich;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.*;

@ServerEndpoint("/random")
public class RandomNumberEndpoint {
    private static final Logger log = LoggerFactory.getLogger(RandomNumberEndpoint.class);
    private static final Map<String, Set<BigInteger>> connectedIPs = new HashMap<>();

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Получаем IP-адрес клиента
        String ipAddress = getIpAddress(session);
        // Если IP-адрес не содержится в connectedIPs, генерируем уникальные случайные числа
        if (!connectedIPs.containsKey(ipAddress)) {
            Set<BigInteger> randomNumbers = generateUniqueRandomNumbers(ipAddress);
            connectedIPs.put(ipAddress, randomNumbers);
            sendRandomBigInteger(session, getRandomNumber(randomNumbers));
        }
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        // Если получено сообщение "generate"
        if ("generate".equals(message)) {
            // Получаем IP-адрес клиента
            String ipAddress = getIpAddress(session);
            Set<BigInteger> randomNumbers = connectedIPs.get(ipAddress);
            // Генерируем уникальное случайное число, которого еще нет в randomNumbers
            BigInteger randomNumber = generateUniqueRandomNumber(ipAddress, randomNumbers);
            randomNumbers.add(randomNumber);
            sendRandomBigInteger(session, randomNumber);
        }
    }

    @OnClose
    public void onClose(Session session) {
        // Получаем IP-адрес клиента и удаляем его из connectedIPs
        String ipAddress = getIpAddress(session);
        connectedIPs.remove(ipAddress);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error occurred: " + session.getId());
        throwable.printStackTrace();
    }

    // Генерирует набор уникальных случайных чисел для заданного IP-адреса
    private Set<BigInteger> generateUniqueRandomNumbers(String ipAddress) {
        Set<BigInteger> randomNumbers = new HashSet<>();
        randomNumbers.add(generateUniqueRandomNumber(ipAddress, randomNumbers));
        return randomNumbers;
    }

    // Генерирует уникальное случайное число для заданного IP-адреса
    private BigInteger generateUniqueRandomNumber(String ipAddress, Set<BigInteger> existingNumbers) {
        BigInteger randomNumber;
        // Генерируем случайное число и проверяем, чтобы оно было уникальным
        do {
            randomNumber = new BigInteger(128, new Random());
        } while (existingNumbers.contains(randomNumber) || randomNumber.toString().equals(ipAddress));

        return randomNumber;
    }

    // Возвращает случайное число из набора randomNumbers
    private BigInteger getRandomNumber(Set<BigInteger> randomNumbers) {
        int index = new Random().nextInt(randomNumbers.size());
        return randomNumbers.toArray(new BigInteger[0])[index];
    }

    // Отправляет случайное BigInteger в формате JSON клиенту через WebSocket
    private void sendRandomBigInteger(Session session, BigInteger randomNumber) throws IOException {
        // Создаем объект RandomNumberResponse
        RandomNumberResponse response = new RandomNumberResponse(randomNumber);

        // Преобразуем объект в JSON
        Gson gson = new Gson();
        String json = gson.toJson(response);
        log.info("Send JSON: {} for IP Address: {}", json, getIpAddress(session));

        // Отправляем JSON клиенту через WebSocket
        session.getBasicRemote().sendText(json);
    }

    // Получает IP-адрес клиента из сессии WebSocket
    private String getIpAddress(Session session) {
        InetSocketAddress remoteAddress =
                (InetSocketAddress) session.getUserProperties().get("javax.websocket.endpoint.remoteAddress");
        return remoteAddress.getAddress().getHostAddress();
    }

    // Класс, для хранения случайного числа
    private static class RandomNumberResponse {
        private final String number;

        public RandomNumberResponse(BigInteger number) {
            this.number = number.toString();
        }

        public String getNumber() {
            return number;
        }
    }
}
