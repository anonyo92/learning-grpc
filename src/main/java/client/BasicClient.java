package client;

import com.example.grpc.greetings.GreetingRequest;
import com.example.grpc.greetings.GreetingResponse;
import com.example.grpc.greetings.GreetingServiceGrpc;
import com.example.grpc.greetings.GreetingServiceGrpc.GreetingServiceBlockingStub;
import com.example.grpc.greetings.GreetingServiceGrpc.GreetingServiceFutureStub;
import com.example.grpc.greetings.GreetingServiceGrpc.GreetingServiceStub;
import config.ServerConstants;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

	private static void doStreamGreet(ManagedChannel channel, List<String> messages) throws InterruptedException {
		System.out.println("Doing Async Greet many times");
		GreetingServiceStub stub = GreetingServiceGrpc.newStub(channel);

		/* Client Streaming is an asynchronous communication with the service.
		 * Client needs a latch to wait on, till it completes streaming its requests,
		 * and the service processes and sends the response; else this client thread
		 * would complete executing this method without waiting to receive the response. */
		CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<GreetingRequest> requestStream =
        stub.repeatedGreet(
            new StreamObserver<>() {
              @Override
              public void onNext(GreetingResponse greetingResponse) {
								/* In this example, the server does not send any response onNext();
								 * it defers till the end to send the response only onComplete().
								 * So, client will not get any response until observer completes
								 * its observation on the server side. */
                System.out.println("Next response:\n" + greetingResponse.getResult());
              }

              @Override
              public void onError(Throwable throwable) {
								// will handle errors later
							}

              @Override
              public void onCompleted() {
								// Completed streaming requests to service; done waiting on latch.
                latch.countDown();
              }
            });

		for (String message : messages) {
			requestStream.onNext(GreetingRequest.newBuilder()
					.setName(message)
					.build());
		}

		/* Unless the client completes observing this stream (by invoking onComplete()
		 * on this stream), service won't send the aggregate response in this case. */
		requestStream.onCompleted();

		// Wait (upto x timeunits) for latch to count down to 0.
		latch.await(3, TimeUnit.SECONDS);
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
			case "greetClientStreaming":
        doStreamGreet(channel, Arrays.asList(args).subList(1, args.length));
        break;
			default:
        System.out.println(
            "Invalid Argument "
                + args[0]
                + "\nWas expecting one of {'syncGreetOnce','asyncGreetOnce'," +
								"'syncGreetServerStreaming', 'greetClientStreaming'}");
    }

		channel.shutdown();
		System.out.println("Channel shut down");
  }
}
