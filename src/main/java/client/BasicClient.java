package client;

import config.ServerConstants;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BasicClient {

	public static void main(String[] args) {
    ManagedChannel channel = ManagedChannelBuilder
				.forAddress(ServerConstants.SERVER_ADDRESS, ServerConstants.PORT)
				.usePlaintext()
				.build();
    System.out.println("Channel created");

		// Make API calls

		channel.shutdown();
		System.out.println("Channel shut down");
  }
}
