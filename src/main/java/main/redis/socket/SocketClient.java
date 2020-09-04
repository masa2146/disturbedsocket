package main.redis.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class SocketClient {

    private RedisPubSubCommands<String, String> subscribeCommand;
    private RedisPubSubCommands<String, String> publisherCommand;
    private Socket socket;

    public static void main(String[] args) {
        SocketClient socketClient = new SocketClient();
        socketClient.init();
        socketClient.startSendMessage();
    }

    private void init() {
        RedisClient redisClient = RedisClient.create("redis://192.168.143.192:6380"); // change to reflect your environment

        StatefulRedisPubSubConnection<String, String> subConnection
                = redisClient.connectPubSub();

        StatefulRedisPubSubConnection<String, String> pubConnection
                = redisClient.connectPubSub();

        System.out.println("Connected to Redis");

        subConnection.addListener(new Listener());
        subscribeCommand = subConnection.sync();
        publisherCommand = pubConnection.sync();

        subscribeCommand.subscribe("Client1");

        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.forceNew = true;
            socket = IO.socket("http://localhost:9099",
                    options);
            socket.connect();
            System.out.println("Socket connected to Server");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void startSendMessage() {
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

                while (true) {
                    System.out.println("---------------------------");
                    System.out.println("| Over Socket --> 1        |");
                    System.out.println("---------------------------");
                    System.out.println("| Over Redis  --> 2        |");
                    System.out.println("---------------------------");

                    String line = reader.readLine();

                    if (!"quit".equals(line)) {
                        if (line.equals("1")) {
                            System.out.println("Enter new message... ");
                            String msg = reader.readLine();
                            System.out.println("Enter CHANNEL ");
                            String channel = reader.readLine();
                            String s = new ObjectMapper().writeValueAsString(new Message(msg, channel));
                            socket.emit("sendMessage", s);

                        } else if (line.equals("2")) {
                            System.out.println("Enter new message... ");
                            String msg = reader.readLine();
                            System.out.println("Enter CHANNEL ");
                            String channel = reader.readLine();

                            String s = new ObjectMapper().writeValueAsString(new Message(msg, channel));
                            publisherCommand.publish(channel, s);

                        }
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
            }
        }).start();
    }


    class Listener implements RedisPubSubListener<String, String> {

        public void message(String channel, String message) {
            System.out.println("CLIENT RECEIVED MESSAGE");
            System.out.println("Message is " + message + " on channel name is " + channel);
            System.out.println(" Message will send to other client");
        }

        public void message(String s, String k1, String s2) {

        }

        public void subscribed(String s, long l) {

        }

        public void psubscribed(String s, long l) {

        }

        public void unsubscribed(String s, long l) {

        }

        public void punsubscribed(String s, long l) {

        }
    }
}
