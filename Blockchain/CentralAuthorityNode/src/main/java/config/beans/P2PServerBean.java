package config.beans;

import config.provider.ValidationNodeConnectionProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import p2pServer.P2PServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class P2PServerBean {
  private final ValidationNodeConnectionProvider connections;

  @Bean
  public P2PServer getP2PServer(@Value("${p2p.port}") String p2pServerPort, @Value("${p2p.host}") String p2pServerHost) throws IOException {
    log.info("Starting P2P server on port {}", p2pServerPort);
    connections
      .getConnections()
      .forEach(connection -> log.info("Connection to IP:{} on Port:{} provided", connection.getIp(), connection.getPort()));

    InetSocketAddress inetSocketAddress = new InetSocketAddress(p2pServerHost, Integer.parseInt(p2pServerPort));
    return new P2PServer(new ServerSocket(Integer.parseInt(p2pServerPort), 50, inetSocketAddress.getAddress()),
      connections.getConnections());
  }
}
