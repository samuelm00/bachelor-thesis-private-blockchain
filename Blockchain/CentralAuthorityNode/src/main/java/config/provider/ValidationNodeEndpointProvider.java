package config.provider;


import connection.ConnectionDetails;


import java.util.List;

public class ValidationNodeEndpointProvider {
  public static String getEndpoint(String endpoint, List<ConnectionDetails> connections) {
    long randomNumber = Math.round(Math.random() * (connections.size() - 1));
    ConnectionDetails node = connections.get((int) randomNumber);

    return "http://" + node.getIp() + ":" + node.getServerPort() + endpoint;
  }
}
