package client;

import com.example.grpc.greetings.GreetingRequest;
import com.example.grpc.greetings.GreetingResponse;
import com.example.grpc.greetings.GreetingServiceGrpc;
import com.example.grpc.greetings.GreetingServiceGrpc.GreetingServiceBlockingStub;
import com.example.grpc.greetings.GreetingServiceGrpc.GreetingServiceFutureStub;
import config.ServerConstants;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BasicClient {

	private static void doSyncGreetOnce(ManagedChannel channel, String message) {
    System.out.println("Doing Sync Greet once");
		GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);

		GreetingRequest request = GreetingRequest.newBuilder()
				.setName(message)
				.build();

		final GreetingResponse response = stub.greet(request);
    System.out.println("Greeting: " + response.getResult());
	}

	private static void doAsyncGreetOnce(ManagedChannel channel, String message) throws ExecutionException, InterruptedException {
		System.out.println("Doing Async Greet once");
		GreetingServiceFutureStub stub = GreetingServiceGrpc.newFutureStub(channel);

		GreetingRequest request = GreetingRequest.newBuilder()
				.setName(message)
				.build();

		final Future<GreetingResponse> response = stub.greet(request);
		System.out.println("Greeting: " + response.get().getResult()); // blocking get
	}

	private static void doSyncGreetOnceForManyResponses(ManagedChannel channel, String message) {
		System.out.println("Doing Sync Greet once; expecting multiple responses...");
		GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);

		GreetingRequest request = GreetingRequest.newBuilder()
				.setName(message)
				.build();

		Iterator<GreetingResponse> response = stub.greetManyTimes(request);
    response.forEachRemaining(
        r -> System.out.println("Greeting: " + r.getResult())
		);
	}

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		if (args.length < 2) {
      System.out.println("Not enough arguments provided.");
			return;
		}

    ManagedChannel channel = ManagedChannelBuilder
				.forAddress(ServerConstants.SERVER_ADDRESS, ServerConstants.PORT)
				.usePlaintext()
				.build();
    System.out.println("Channel created");

		// Make gRPC API calls
		switch (args[0]) {
			case "syncGreetOnce": doSyncGreetOnce(channel, args[1]); break;
			case "asyncGreetOnce": doAsyncGreetOnce(channel, args[1]); break;
			case "syncGreetServerStreaming": doSyncGreetOnceForManyResponses(channel, args[1]); break;
			default:
        System.out.println(
            "Invalid Argument "
                + args[0]
                + "\nWas expecting one of {'syncGreetOnce','asyncGreetOnce','syncGreetServerStreaming'}");
    }

		channel.shutdown();
		System.out.println("Channel shut down");
  }
}
