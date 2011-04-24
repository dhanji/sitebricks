package info.sitebricks.data;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Hierarchical index of wiki documents. (Singleton)
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Index {
  private Node root;

  /** needed by db4o **/
  public Index() {
  }

  public Index(Node root) {
    this.root = root;
  }

  /**
   * Returns the list of top-level index items.
   */
  public List<Node> list() {
    return root.getChildren();
  }

  public static class Node {
    private String name; // directly maps to Document#name
    private String topic;

    private List<Node> nodes = Lists.newArrayList();

    public String getTopic() {
      return topic;
    }

    public String getName() {
      return name;
    }

    public List<Node> getChildren() {
      return nodes;
    }

    public void setDocument(Document document) {
      this.name = document.getName();
      this.topic = document.getTopic();
    }
  }
}
