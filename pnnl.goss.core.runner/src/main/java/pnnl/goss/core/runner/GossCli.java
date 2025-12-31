package pnnl.goss.core.runner;

import org.apache.http.auth.UsernamePasswordCredentials;

import pnnl.goss.core.Client.DESTINATION_TYPE;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.client.GossClient;

/**
 * Command-line GOSS client tool for subscribing and publishing.
 *
 * Usage: java -jar goss-cli.jar subscribe [options] destination java -jar
 * goss-cli.jar publish [options] destination message
 *
 * Options: -t, --topic Use a topic (default for subscribe) -q, --queue Use a
 * queue (default for publish) -b, --broker URL Broker URL (default:
 * tcp://localhost:61616) -u, --user USER Username for authentication -p,
 * --password PW Password for authentication -h, --help Show help message
 *
 * Examples: java -jar goss-cli.jar subscribe --queue
 * goss.gridappsd.process.request java -jar goss-cli.jar subscribe --topic
 * goss.gridappsd.simulation.output.123 java -jar goss-cli.jar publish --queue
 * goss.gridappsd.process.request '{"requestType":"query"}' java -jar
 * goss-cli.jar publish --topic goss.gridappsd.platform.log 'Test message'
 */
public class GossCli {

    private static final String DEFAULT_BROKER = "tcp://localhost:61616";

    public static void main(String[] args) {
        if (args.length == 0 || hasFlag(args, "-h", "--help")) {
            printUsage();
            System.exit(0);
        }

        String command = args[0].toLowerCase();

        switch (command) {
            case "subscribe" :
            case "sub" :
                handleSubscribe(args);
                break;
            case "publish" :
            case "pub" :
                handlePublish(args);
                break;
            default :
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
        }
    }

    private static void handleSubscribe(String[] args) {
        String brokerUrl = getOption(args, "-b", "--broker", DEFAULT_BROKER);
        String username = getOption(args, "-u", "--user", null);
        String password = getOption(args, "-p", "--password", null);
        boolean useQueue = hasFlag(args, "-q", "--queue");

        String destination = getPositionalArg(args, 1);
        if (destination == null) {
            System.err.println("Error: No destination specified");
            printUsage();
            System.exit(1);
        }

        // Default to topic for subscribe
        DESTINATION_TYPE destType = useQueue ? DESTINATION_TYPE.QUEUE : DESTINATION_TYPE.TOPIC;

        System.out.println("GOSS Subscriber");
        System.out.println("===============");
        System.out.println("Broker:      " + brokerUrl);
        System.out.println("Destination: " + destination);
        System.out.println("Type:        " + destType);
        if (username != null) {
            System.out.println("User:        " + username);
        }
        System.out.println();

        GossClient client = createClient(brokerUrl, username, password);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down...");
            client.close();
        }));

        try {
            client.createSession();
            System.out.println("Connected! Waiting for messages... (Ctrl+C to stop)\n");

            client.subscribe(destination, new GossResponseEvent() {
                @Override
                public void onMessage(java.io.Serializable response) {
                    System.out.println("--- Message Received ---");
                    System.out.println(response);
                    System.out.println("------------------------\n");
                }
            }, destType);

            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void handlePublish(String[] args) {
        String brokerUrl = getOption(args, "-b", "--broker", DEFAULT_BROKER);
        String username = getOption(args, "-u", "--user", null);
        String password = getOption(args, "-p", "--password", null);
        boolean useTopic = hasFlag(args, "-t", "--topic");

        String destination = getPositionalArg(args, 1);
        String message = getPositionalArg(args, 2);

        if (destination == null) {
            System.err.println("Error: No destination specified");
            printUsage();
            System.exit(1);
        }
        if (message == null) {
            System.err.println("Error: No message specified");
            printUsage();
            System.exit(1);
        }

        // Default to queue for publish (matches Python behavior)
        DESTINATION_TYPE destType = useTopic ? DESTINATION_TYPE.TOPIC : DESTINATION_TYPE.QUEUE;

        System.out.println("GOSS Publisher");
        System.out.println("==============");
        System.out.println("Broker:      " + brokerUrl);
        System.out.println("Destination: " + destination);
        System.out.println("Type:        " + destType);
        if (username != null) {
            System.out.println("User:        " + username);
        }
        System.out.println();

        GossClient client = createClient(brokerUrl, username, password);

        try {
            client.createSession();
            System.out.println("Connected! Publishing message...\n");

            client.publish(destination, message, destType);

            System.out.println("Message published successfully!");
            client.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static GossClient createClient(String brokerUrl, String username, String password) {
        if (username != null && password != null) {
            return new GossClient(PROTOCOL.OPENWIRE,
                    new UsernamePasswordCredentials(username, password),
                    brokerUrl, null);
        } else {
            return new GossClient(PROTOCOL.OPENWIRE, null, brokerUrl, null);
        }
    }

    private static void printUsage() {
        System.out.println("GOSS CLI - Command-line tool for GOSS messaging");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar goss-cli.jar subscribe [options] destination");
        System.out.println("  java -jar goss-cli.jar publish [options] destination message");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  subscribe (sub)  Subscribe to a destination and print messages");
        System.out.println("  publish (pub)    Publish a message to a destination");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -t, --topic       Use a topic (default for subscribe)");
        System.out.println("  -q, --queue       Use a queue (default for publish)");
        System.out.println("  -b, --broker URL  Broker URL (default: tcp://localhost:61616)");
        System.out.println("  -u, --user USER   Username for authentication");
        System.out.println("  -p, --password PW Password for authentication");
        System.out.println("  -h, --help        Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # Subscribe to a queue");
        System.out.println("  java -jar goss-cli.jar subscribe --queue goss.gridappsd.process.request");
        System.out.println();
        System.out.println("  # Subscribe to a topic");
        System.out.println("  java -jar goss-cli.jar sub --topic goss.gridappsd.simulation.output.123");
        System.out.println();
        System.out.println("  # Publish to a queue (default)");
        System.out.println("  java -jar goss-cli.jar publish goss.gridappsd.process.request '{\"type\":\"query\"}'");
        System.out.println();
        System.out.println("  # Publish to a topic");
        System.out.println("  java -jar goss-cli.jar pub --topic goss.gridappsd.platform.log 'Test message'");
        System.out.println();
        System.out.println("  # With authentication");
        System.out.println("  java -jar goss-cli.jar sub -u admin -p admin -q my.queue");
    }

    private static boolean hasFlag(String[] args, String... flags) {
        for (String arg : args) {
            for (String flag : flags) {
                if (arg.equals(flag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getOption(String[] args, String shortOpt, String longOpt, String defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(shortOpt) || args[i].equals(longOpt)) {
                return args[i + 1];
            }
        }
        return defaultValue;
    }

    /**
     * Get positional argument at index, skipping options and their values. Index 0
     * is the command (subscribe/publish), 1 is first positional arg, etc.
     */
    private static String getPositionalArg(String[] args, int index) {
        int positionalCount = 0;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                // Skip options that take values
                if (arg.equals("-b") || arg.equals("--broker") ||
                        arg.equals("-u") || arg.equals("--user") ||
                        arg.equals("-p") || arg.equals("--password")) {
                    i++; // Skip the value
                }
                continue;
            }
            if (positionalCount == index) {
                return arg;
            }
            positionalCount++;
        }
        return null;
    }
}
