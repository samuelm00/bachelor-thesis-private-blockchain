package datastructure;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class Tuple<K, V> {
  @Getter
  @Setter
  private K key;

  @Getter
  @Setter
  private V value;

  public Tuple(K key, V value) {
    this.key = key;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Tuple<?, ?> tuple = (Tuple<?, ?>) o;
    return Objects.equals(key, tuple.key) && Objects.equals(value, tuple.value);
  }
}
