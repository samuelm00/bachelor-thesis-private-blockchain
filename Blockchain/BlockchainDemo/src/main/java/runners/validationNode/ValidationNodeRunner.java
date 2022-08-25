package runners.validationNode;

import runners.BaseRunner;

import java.io.File;


public class ValidationNodeRunner extends BaseRunner implements Runnable {
  private final int p2pPort;
  private final int dbPort;
  private final int serverPort;
  private final String imageName;

  public ValidationNodeRunner(int p2pPort, int dbPort, int serverPort, String imageName) {
    super("ValidationNode");
    this.p2pPort = p2pPort;
    this.dbPort = dbPort;
    this.serverPort = serverPort;
    this.imageName = imageName;
  }

  protected String[] getDockerCommand() {
    return new String[]{
      "docker",
      "container",
      "run",
      "--name",
      imageName,
      "-p",
      dbPort + ":5432",
      "-e",
      "POSTGRES_DB=ledger",
      "-e",
      "POSTGRES_PASSWORD=root",
      "-d",
      "postgres"
    };
  }

  protected String[] getStartCommand() {
    String dataSourceArg = "--spring.datasource.url=jdbc:postgresql://localhost:" + dbPort + "/ledger";
    String p2pPortArg = "--p2p.port=" + p2pPort;
    String serverPortArg = "--server.port=" + serverPort;
    return new String[]{
      super.isWindows() ? "mvn.cmd" : "mvn",
      "spring-boot:run",
      String.format("\"-Dspring-boot.run.arguments=%s %s %s\"", dataSourceArg, p2pPortArg, serverPortArg)
    };
  }

  @Override
  public void run() {
    String logFileName = "log-file-localhost:" + p2pPort + ".txt";
    File file = new File(logFileName);
    super.start(ProcessBuilder.Redirect.appendTo(file));
  }
}