package security.util;

public enum AuthPath {
  LOGIN("/login"), REGISTER("/register");

  private final String path;
  private final String base = "/api";

  AuthPath(String path) {
    this.path = path;
  }

  public String getPathWithBase() {
    return base + this.path;
  }
}
