package main.redis.socket;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.io.IOException;

public class SocketServer2 {

    private SocketIOServer server;
    private RedisPubSubCommands<String, String> subscribeCommand;
    private RedisPubSubCommands<String, String> publisherCommand;

    public static void main(String[] args) {
        SocketServer2 socketServer = new SocketServer2();
        socketServer.init();
        socketServer.initEventListeners();
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

        subscribeCommand.subscribe("Server2");

        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9098);
        server = new SocketIOServer(config);
        server.start();
        System.out.println("Started socket server");
    }

    private void initEventListeners() {
        server.addConnectListener(client -> {
            System.out.println(" Client has been connected! ");
            System.out.println(" Added client data to REDIS List because of socket connected!");
        });

        server.addEventListener("toClientConnected", String.class, (client, data, ackRequest) -> {
            System.out.println(" Received message: " + data);
            System.out.println(" Added data to REDIS List becaouse of socket connected! Added Data:");
        });

        server.addEventListener("sendMessage", String.class, (client, data, ackRequest) -> {
            System.out.println("CLIENT RECEIVED MESSAGE OVER SOCKET");
            System.out.println("Message is " + data + " on channel name is sendMessage");
            System.out.println(" Message will send to other client");
            sendMessage(data);
        });

        server.addDisconnectListener(client -> {
            System.out.println(" Client has been disconnected!");
            System.out.println(" Removed data from REDIS List due to socket disconnected! Removed Data: ");
        });
    }

    private void sendMessage(String data) {
        ObjectMapper mapper = new ObjectMapper();
        Message message = null;
        try {
            message = mapper.readValue(data, Message.class);
            // get channel name and publish orginal message
            publisherCommand.publish(message.getChannel(), data);
            System.out.println("Message published " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Listener implements RedisPubSubListener<String, String> {

        public void message(String channel, String message) {
            System.out.println("CLIENT RECEIVED MESSAGE OVER REDIS");
            System.out.println("Message is " + message + " on channel name is " + channel);
            System.out.println(" Message will send to other client");

            // get channel name and publish orginal message
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
