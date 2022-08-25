package main;

import runners.centralAuthority.CentralAuthorityRunner;
import runners.validationNode.ValidationNodeRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  private static final ExecutorService handlers = Executors.newFixedThreadPool(4);

  public static void main(String[] args) {
    handlers.submit(new CentralAuthorityRunner());
    handlers.submit(new ValidationNodeRunner(9002, 27019, 9001, "Ledger1"));
    handlers.submit(new ValidationNodeRunner(9003, 27020, 9004, "Ledger2"));
    handlers.submit(new ValidationNodeRunner(9005, 27021, 9006, "Ledger3"));
  }
}

