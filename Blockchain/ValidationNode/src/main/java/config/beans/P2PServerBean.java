package config.beans;

import callback.P2PCallback;
import config.ConnectionProvider;
import connection.ConnectionDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Block;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import p2pServer.P2PServer;
import p2pServer.callback.ValidationCallbackHandler;
import repository.BlockRepository;
import repository.MerkleNodeRepository;
import repository.TweetRepository;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class P2PServerBean {
  private final BlockRepository blockRepository;
  private final MerkleNodeRepository merkleNodeRepository;
  private final TweetRepository tweetRepository;
  private final ConnectionProvider connectionProvider;

  private void addGenesisBlock() {
    Block genesisBlock = new Block(null, null, 0);
    try {
      blockRepository.save(genesisBlock);
    } catch (Exception e) {
      log.error(e.getMessage());
      log.info("Genesis block already exists");
    }
    log.info("Genesis block was successfully added, hash: {}", genesisBlock.getHash());
  }

  @Bean
  public P2PServer getP2PServer(@Value("${p2p.port}") String p2pServerPort, @Value("${p2p.host}") String p2pServerHost)
    throws IOException {
    InetSocketAddress inetSocketAddress = new InetSocketAddress(p2pServerHost, Integer.parseInt(p2pServerPort));

    List<ConnectionDetails> connections = connectionProvider
      .getConnections()
      .stream()
      .filter(connection -> !connection.getIp().equals(p2pServerHost) || connection.getPort() != Integer.parseInt(p2pServerPort))
      .collect(Collectors.toList());

    log.info("P2P server connections: {}", connections);

    var p2pServer = new P2PServer(new ServerSocket(Integer.parseInt(p2pServerPort), 50,
      inetSocketAddress.getAddress()), connections);

    this.addGenesisBlock();

    ConnectionDetails currentNode = connectionProvider
      .getConnections()
      .stream()
      .filter(connection -> connection.getIp().equals(p2pServerHost) && connection.getPort() == Integer.parseInt(p2pServerPort))
      .findFirst().get();


    P2PCallback callback = new ValidationCallbackHandler(new ArrayList<>(), connectionProvider.getConnections().size(),
      currentNode, blockRepository, merkleNodeRepository, tweetRepository);
    p2pServer.register(callback);

    return p2pServer;
  }
}
