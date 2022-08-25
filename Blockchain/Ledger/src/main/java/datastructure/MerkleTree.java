package datastructure;

import lombok.Getter;
import model.MerkleNode;
import model.Tweet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MerkleTree {
  @Getter
  private MerkleNode root;

  public MerkleTree() {
  }

  public MerkleTree(MerkleNode root) {
    this.root = root;
  }

  /**
   * Builds a Merkle tree from a list of messages.
   *
   * @param tweets a list of messages
   * @return the created Merkle tree
   */
  public MerkleNode build(List<Tweet> tweets) {
    if (tweets.size() == 0) {
      return null;
    }
    if (tweets.contains(null)) {
      throw new IllegalArgumentException("Message list cannot contain null values");
    }
    return buildTree(tweets.stream().map(
      m -> new MerkleNode(null, null, m)).collect(Collectors.toList()), null);
  }

  /**
   * Builds a Merkle tree from a list of MerkleNodes.
   *
   * @param messageNodes a list of MerkleNodes
   * @return
   */
  private MerkleNode buildTree(List<MerkleNode> messageNodes, MerkleTreeSaveCallback callback) {
    if (messageNodes.size() == 1) {
      this.root = messageNodes.get(0);

      if (callback != null) {
        callback.save(this.root);
      }

      return messageNodes.get(0);
    }

    // keeps track of the parent nodes that are created in each iteration of the while loop
    List<MerkleNode> currentParentLevelNodes = new ArrayList<>();


    // keeps track of the child notes in order to create the corresponding parent nodes
    List<MerkleNode> currentChildrenNodes = new ArrayList<>(messageNodes);


    // once only one node is left in currentChildrenNodes the tree is complete
    // and the last node is the root node
    while (currentChildrenNodes.size() > 1) {

      // if the callback exists the data is saved as specified by the callback
      if (callback != null) {
        currentChildrenNodes.forEach(callback::save);
      }

      // iteration where the parent nodes are created
      for (int i = 0; i < currentChildrenNodes.size(); i += 2) {
        MerkleNode left = currentChildrenNodes.get(i);
        MerkleNode right = null;
        if (i + 1 < currentChildrenNodes.size()) {
          right = currentChildrenNodes.get(i + 1);
        } else {
          right = left;
        }
        MerkleNode parent = new MerkleNode(left, right, null);
        currentParentLevelNodes.add(parent);
      }

      // for the next iteration the parent nodes become the children nodes
      // because we need to generate their parents
      currentChildrenNodes = currentParentLevelNodes;
      currentParentLevelNodes = new ArrayList<>();
    }

    // if the callback exists the data is saved as specified by the callback
    if (callback != null) {
      currentChildrenNodes.forEach(callback::save);
    }

    this.root = currentChildrenNodes.get(0);
    return root;
  }

  /**
   * Checks if a message is in the Merkle tree.
   *
   * @param tweetId the message id
   * @return true if the message is in the Merkle tree, false otherwise
   */
  public boolean containsTweet(Long tweetId) {
    if (root == null) {
      return false;
    }
    return traverseWithPredicate(n -> {
      if (n.getTweet() == null) {
        return false;
      }
      return n.getTweet().getId().equals(tweetId);
    }, this.root);
  }

  /**
   * Returns a branch of the Merkle tree that contains the message with the given id.
   *
   * @param tweetId
   * @return
   */
  public MerkleTree getBranchForTweetId(Long tweetId) {
    if (root == null) {
      return null;
    }
    List<MerkleNode> nodes = buildBranch(getPathToNodeWithPredicate((n) -> {
      if (n == null) {
        return false;
      }
      return n.containsTweet(tweetId);
    }, this.root, new ArrayList<>()));

    return new MerkleTree(nodes.get(0));
  }

  /**
   * Constructs the branch of the Merkle tree based on the list of nodes provided.
   *
   * @param nodes the list of nodes that should be included in the branch
   * @return
   */
  private List<MerkleNode> buildBranch(List<MerkleNode> nodes) {
    return nodes.stream().map(n -> {
      if (n.getTweet() != null) {
        if (!nodes.contains(n.getLeft())) {
          n.setLeft(null);
        }
        if (!nodes.contains(n.getRight())) {
          n.setRight(null);
        }
      }
      return n;
    }).collect(Collectors.toList());
  }

  /**
   * Traverses the Merkle tree and returns the path to the node with the given message id.
   *
   * @param predicate   the predicate to check if the node is the one we are looking for
   * @param node        the node to start the search from
   * @param currentPath empty array list to keep track of the path
   * @return a list with the nodes that form the path to the node that contains the message
   */
  private List<MerkleNode> getPathToNodeWithPredicate(Predicate<MerkleNode> predicate, MerkleNode node,
                                                      List<MerkleNode> currentPath) {
    if (node == null) {
      return null;
    }

    // if we found the node that satisfies the predicate we return the current path
    if (predicate.test(node)) {
      return currentPath;
    }

    currentPath.add(node);

    // continue traversing the left side of the tree
    getPathToNodeWithPredicate(predicate, node.getLeft(), currentPath);

    // continue traversing the right side of the tree
    getPathToNodeWithPredicate(predicate, node.getRight(), currentPath);

    return currentPath;
  }

  /**
   * Private function to travers the tree (inorder-traversal) and apply a predicate on each node.
   *
   * @param predicate the predicate to apply on each node
   * @param node      the root node of the tree
   * @return true if a node satisfies the predicate
   */
  private boolean traverseWithPredicate(Predicate<MerkleNode> predicate, MerkleNode node) {
    if (node != null) {
      boolean predicateTest = traverseWithPredicate(predicate, node.getLeft());
      if (predicateTest || predicate.test(node)) {
        return true;
      }
      predicateTest = traverseWithPredicate(predicate, node.getRight());
      return predicateTest || predicate.test(node);
    }
    return false;
  }

  /**
   * Validates the Merkle tree by creating a new one with the provided tweets and comparing the two roots.
   *
   * @param tweets the tweets to validate the Merkle tree with
   * @return true if the Merkle tree is valid and false otherwise
   */
  public boolean validateTree(List<Tweet> tweets) {
    MerkleTree tree = new MerkleTree();
    tree.build(tweets);
    return tree.getRoot().equals(this.root);
  }

  /**
   * This function creates a Merkle tree from the provided tweets and saves it to the database.
   *
   * @param tweets
   * @param callback
   */
  public void buildAndSaveMerkleTree(List<Tweet> tweets, MerkleTreeSaveCallback callback) {
    buildTree(tweets.stream().map(
      m -> new MerkleNode(null, null, m)).collect(Collectors.toList()), callback);
  }
}
