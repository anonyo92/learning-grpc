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
}
