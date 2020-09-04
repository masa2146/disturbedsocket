package main.redis.socket.manager;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;

import java.io.IOException;

public class ClientManager {

    private RedisPubSubCommands<String, String> subscribeCommand;
    private RedisPubSubCommands<String, String> publisherCommand;
    private RedisCommands<String, String> commands;

    private static final String EVENT_CONNECTED = "onConnected";
    private static final String EVENT_DISCONNECTED = "onDisconnected";
    private static final String EVENT_STATUS = "status";


    public static void main(String[] args) {
        ClientManager clientManager = new ClientManager();
        clientManager.start();
    }

    public void start() {

        RedisClient redisClient = RedisClient.create("redis://192.168.143.192:6380");

        StatefulRedisPubSubConnection<String, String> subConnection = redisClient.connectPubSub();

        StatefulRedisPubSubConnection<String, String> pubConnection = redisClient.connectPubSub();

        subConnection.addListener(new Listener());

        commands = redisClient.connect().sync();
        subscribeCommand = subConnection.sync();
        publisherCommand = pubConnection.sync();

        commands.flushall();

        subscribeCommand.subscribe(EVENT_CONNECTED, EVENT_STATUS);

        new Thread(() -> {
            while (true) {
                 statusControl();
//                try{
//                    for (String key : commands.keys("*")) {
//
//                        System.out.println("Status: " + commands.hget(key, "status"));
//                        System.out.println("Info: " + commands.hget(key, "info"));
//                    }
//                    publisherCommand.publish("marmara", "sadasda");
//                }catch (RedisException e){
//                    System.out.println("Connection reset by peer");
//                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).run();
    }

    private synchronized void statusControl() {
        try{
        for (String key : commands.keys("*")) {
            if (commands.get(key) != null) {
                if (commands.hget(key,"status").equals("0")) {
                    System.out.println("CLIENT DISCONNECTED. CLIENT ID: " + key);
                }
            }
            publisherCommand.publish(EVENT_STATUS + "/" + key, "0");
            commands.set(key, "0");
        }

        }catch (RedisException e){
            System.out.println("Connection reset by peer");
        }
    }

    class Listener implements RedisPubSubListener<String, String> {

        public void message(String channel, String clientId) {
            if (channel.equals(EVENT_CONNECTED)) {
                publisherCommand.publish(EVENT_CONNECTED + "/" + clientId, "You connected!");
                commands.hset(clientId, "status", "1");
                commands.hset(clientId, "info", "{ \"clientId\":\"" + clientId + "\", \"clientName\":\"SampleClientName\" }");
                System.out.println("ADDED STATUS: ");

                // trigger add onConnected
            } else if (channel.equals(EVENT_STATUS)) {
                //System.out.println("STATUS EVENT: " + clientId);
            }

            // get channel name and publish orginal message
        }

        public void message(String s, String k1, String s2) {

        }

        public void subscribed(String s, long l) {
            System.out.println();

        }

        public void psubscribed(String s, long l) {

        }

        public void unsubscribed(String s, long l) {

        }

        public void punsubscribed(String s, long l) {

        }
    }

}
