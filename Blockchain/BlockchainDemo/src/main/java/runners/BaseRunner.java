package runners;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class BaseRunner {
  private final String baseDirectoryName;

  public BaseRunner(String baseDirectoryName) {
    this.baseDirectoryName = baseDirectoryName;
  }

  public boolean isWindows() {
    return System.getProperty("os.name").toLowerCase().startsWith("windows");
  }

  private String getDirectory() {
    Path currentRelativePath = Paths.get("");
    String base = currentRelativePath.toAbsolutePath().toString();

    if (base.contains("target")) {
      return base.substring(0, base.length() - "BlockchainDemo/target".length()) + baseDirectoryName;
    }
    return base + "/" + baseDirectoryName;
  }

  /**
   * This is the command that is used to start the docker container
   *
   * @return
   */
  protected abstract String[] getDockerCommand();

  /**
   * This is the command that is used to run the program.
   *
   * @return
   */
  protected abstract String[] getStartCommand();

  /**
   * Starts the docker container using the command from {@link BaseRunner#getDockerCommand()}
   *
   * @param redirect
   * @return
   * @throws IOException
   */
  private Process startDockerContainer(ProcessBuilder.Redirect redirect) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder(getDockerCommand());
    processBuilder.directory(new File(getDirectory()));
    processBuilder.redirectOutput(redirect);
    processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
    return processBuilder.start();
  }

  /**
   * Starts the program using the command from {@link BaseRunner#getStartCommand()}
   *
   * @param redirect
   * @return
   * @throws IOException
   */
  private Process startProgram(ProcessBuilder.Redirect redirect) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder(getStartCommand());
    processBuilder.directory(new File(getDirectory()));
    processBuilder.redirectOutput(redirect);
    processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
    return processBuilder.start();
  }

  final public void start(ProcessBuilder.Redirect redirect) {
    try {
      var process = startDockerContainer(redirect);
      process.waitFor();
      var nodeProcess = startProgram(redirect);
      nodeProcess.waitFor();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
