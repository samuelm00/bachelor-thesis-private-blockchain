package envelope;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.Objects;

@XmlRootElement(name = "envelope")
@XmlAccessorType(XmlAccessType.NONE)
public class Envelope<T> {
  @Getter
  @Setter
  @XmlAnyElement(lax = true)
  private T data;

  @Getter
  @Setter
  @XmlElement(name = "ttl", required = true)
  private int ttl;

  @Getter
  @Setter
  @XmlElement(name = "type", required = true)
  private EnvelopeType type;

  @Getter
  @Setter
  @XmlElement(name = "timestamp", required = true)
  private Long timestamp;

  public Envelope(T data, EnvelopeType type) {
    this.data = data;
    this.ttl = 2;
    this.type = type;
    this.timestamp = System.currentTimeMillis();
  }

  public Envelope() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Envelope<?> envelope = (Envelope<?>) o;
    return Objects.equals(data, envelope.data) && type == envelope.type;
  }
}
