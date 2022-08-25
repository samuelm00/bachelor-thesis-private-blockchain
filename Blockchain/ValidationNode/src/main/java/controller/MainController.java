package controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import repository.MerkleNodeRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MainController {
  private final MerkleNodeRepository merkleNodeRepository;

  private String encodeQueryParam(String param) {
    return URLDecoder.decode(param, StandardCharsets.UTF_8);
  }

  @GetMapping(value = "/tweets")
  public ResponseEntity<?> getTweets(@RequestParam(name = "publicKey") String publicKey) {
    String encodedPublicKey = encodeQueryParam(publicKey);
    log.info("Users public key: {}", encodedPublicKey);
    try {
      var merkleNodes = merkleNodeRepository.findAllByTweet_PublicKeyOfCreator(encodedPublicKey);
      log.info("Found {} tweets", merkleNodes.size());
      return ResponseEntity.ok(merkleNodes);
    } catch (Exception e) {
      log.error("Error getting tweets of user: {}", encodedPublicKey, e);
      return ResponseEntity.badRequest().body("Error getting tweets of user: " + encodedPublicKey);
    }
  }

  @GetMapping(value = "/tweet")
  public ResponseEntity<?> getTweet(@RequestParam(name = "hash") String hash) {
    String hashOfMerkleNode = encodeQueryParam(hash);

    log.info("Finding merkle node with hash: {}", hashOfMerkleNode);

    try {
      var merkleNode = merkleNodeRepository.findByHash(hashOfMerkleNode);
      if (merkleNode.isPresent()) {
        log.info("Found merkle node with hash: {}", hashOfMerkleNode);
        return ResponseEntity.ok(merkleNode.get());
      }
      return ResponseEntity.ok(null);
    } catch (Exception e) {
      log.error("Error getting tweet with hash: {}", hashOfMerkleNode, e);
      return ResponseEntity.badRequest().body("Error getting tweet with hash: " + hashOfMerkleNode);
    }
  }
}
