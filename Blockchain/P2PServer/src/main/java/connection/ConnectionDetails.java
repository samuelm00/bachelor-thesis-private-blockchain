package connection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "connectionDetails")
@XmlAccessorType(XmlAccessType.NONE)
@NoArgsConstructor
public class ConnectionDetails {
  @Getter
  @Setter
  @XmlElement(name = "port", required = true)
  private int port;

  @Getter
  @Setter
  @XmlElement(name = "ip", required = true)
  private String ip;

  @Getter
  @Setter
  @XmlElement(name = "publicKey", required = true)
  private String publicKey;

  @Getter
  @Setter
  @XmlElement(name = "serverPort", required = true)
  private int serverPort;


  public ConnectionDetails(int port, int serverPort, String ip, String publicKey) {
    this.port = port;
    this.ip = ip;
    this.publicKey = publicKey;
    this.serverPort = serverPort;
  }
}
