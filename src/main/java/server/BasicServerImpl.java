package server;

import com.example.grpc.greetings.GreetingRequest;
import com.example.grpc.greetings.GreetingResponse;
import com.example.grpc.greetings.GreetingServiceGrpc.GreetingServiceImplBase;
import io.grpc.stub.StreamObserver;

public class BasicServerImpl extends GreetingServiceImplBase {
	@Override
	public void greet(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
    System.out.println("Received greeting request\n\t" + request.toString());
		GreetingResponse response = GreetingResponse.newBuilder()
				.setResult("Hello " + request.getName())
				.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void greetManyTimes(GreetingRequest request, StreamObserver<GreetingResponse> responseObserver) {
		System.out.println("Received greeting request\n\t" + request.toString());
    System.out.println("Will send many greetings...");
		GreetingResponse response = GreetingResponse.newBuilder()
				.setResult("Hello " + request.getName())
				.build();

		for (int i = 0; i < 10; i++) {
			responseObserver.onNext(response);
		}
		responseObserver.onCompleted();
	}

	@Override
	/* The StreamObserver<GreetingRequest> return-type is returned so that
	 * when the client stub RPC's this method, it gets a stream to observe
	 * while it feeds requests to that stream */
	public StreamObserver<GreetingRequest> repeatedGreet(
			StreamObserver<GreetingResponse> responseObserver) {
		System.out.println("Going to receive many greeting requests\n\t");
		System.out.println("Will send one greeting...");

		StringBuilder sb = new StringBuilder();

    return new StreamObserver<>() {
      @Override
      public void onNext(GreetingRequest greetingRequest) {
				// will receive the request, but not respond now; only keep aggregating.
				System.out.println("Received greeting request\n\t" + greetingRequest.toString());
        sb.append("Hello ").append(greetingRequest.getName()).append("!\n");
      }

      @Override
      public void onError(Throwable throwable) {
        responseObserver.onError(throwable);
      }

      @Override
      public void onCompleted() {
        System.out.println("Aggregating response");
        GreetingResponse response = GreetingResponse.newBuilder()
						.setResult(sb.toString())
						.build();
				// Send one aggregated response
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
    };
	}

	@Override
	public StreamObserver<GreetingRequest> greetEveryone(
			StreamObserver<GreetingResponse> responseStreamObserver) {

    return new StreamObserver<>() {
			@Override
			public void onNext(GreetingRequest greetingRequest) {
				GreetingResponse response = GreetingResponse.newBuilder()
						.setResult("Hello " + greetingRequest.getName())
						.build();
				// will receive the request and send response on every observation
				responseStreamObserver.onNext(response);
			}

			@Override
			public void onError(Throwable throwable) {
				responseStreamObserver.onError(throwable);
			}

			@Override
			public void onCompleted() {
				responseStreamObserver.onCompleted();
			}
		};
	}
}
