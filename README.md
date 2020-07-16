# angular-spring-websocket-sample

This sample is to demonstrate a chat application using the following cutting-edge technology stack :

* Angular as WebSocket client
* Spring WebFlux based Reactive WebSocket APIs
* In-memory Reactor Sink as a message queue([feat/reactor-sinks branch](https://github.com/hantsy/angular-spring-websocket-sample/tree/feat/reactor-sinks)) or
* Spring Data MongoDB based  `@Tailable`  query result as an infinite stream

## Build 

Before running the application, you should build and run client and server side respectively.

### Server 

Run a MongoDB service firstly, simply you can run it from a Docker container. There is a `docker-compose.yaml` file is ready for you.

```bash
docker-compose up mongodb
```

Build the application.

```e
./gradlew build
```

Run the target jar from the *build* folder to start up the application.

```bash
java -jar build/xxx.jar
```

### Client

Install dependencies.

```bash
npm install
```

Start up the application.

```bash
npm run start
```

Now  you can open a browser and  navigate to http://localhost:4200 and have a try.

## Reference

* [How to use Processor in Reactor Java/](https://ducmanhphan.github.io/2019-08-25-How-to-use-Processor-in-Reactor-Java)
* [How To Build a Chat App Using WebFlux, WebSockets & React ](https://blog.monkey.codes/how-to-build-a-chat-app-using-webflux-websockets-react/)