package repository;

import model.MerkleNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerkleNodeRepository extends JpaRepository<MerkleNode, String> {
  Optional<MerkleNode> findByHash(String hash);

  List<MerkleNode> findAllByTweet_PublicKeyOfCreator(String publicKey);
}