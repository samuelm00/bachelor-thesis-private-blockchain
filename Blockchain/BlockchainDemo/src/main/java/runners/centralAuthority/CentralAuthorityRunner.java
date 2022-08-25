package runners.centralAuthority;

import runners.BaseRunner;


public class CentralAuthorityRunner extends BaseRunner implements Runnable {

  public CentralAuthorityRunner() {
    super("CentralAuthorityNode");
  }

  protected String[] getDockerCommand() {
    return new String[]{
      "docker",
      "container",
      "run",
      "--name",
      "central-authority-db",
      "-p",
      "27018:5432",
      "-e",
      "POSTGRES_DB=ledger",
      "-e",
      "POSTGRES_PASSWORD=root",
      "-d",
      "postgres"
    };
  }

  protected String[] getStartCommand() {
    return new String[]{
      super.isWindows() ? "mvn.cmd" : "mvn",
      "spring-boot:run",
      "-Dspring-boot.run.arguments=\"--spring.datasource.url=jdbc:postgresql://localhost:27018/ledger\"",
    };
  }

  @Override
  public void run() {
    super.start(ProcessBuilder.Redirect.INHERIT);
  }
}
