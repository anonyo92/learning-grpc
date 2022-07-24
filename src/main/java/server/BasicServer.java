package server;

import config.ServerConstants;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BasicServer {
  public static void main(String[] args) throws IOException, InterruptedException {
		Server basicServer = ServerBuilder
				.forPort(ServerConstants.PORT)
				.build();

		basicServer.start();
    System.out.println("Server started. Listening on port: " + ServerConstants.PORT);

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
