package server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BasicServer {
	private static final int PORT = 50051;

  public static void main(String[] args) throws IOException, InterruptedException {
		Server basicServer = ServerBuilder
				.forPort(PORT)
				.build();

		basicServer.start();
    System.out.println("Server started. Listening on port: " + PORT);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("Received Shutdown signal");
									basicServer.shutdown();
                  System.out.println("Server stopped.");
                }));

		basicServer.awaitTermination();
  }
}
