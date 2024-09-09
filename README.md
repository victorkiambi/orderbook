# Order Book Service

This project is a Kotlin-based order book service that handles buy and sell orders, matches them, and maintains a list of open orders and trade history.

## Prerequisites

- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [Gradle](https://gradle.org/install/)

## Setup

1. Clone the repository:
    ```sh
    git clone https://github.com/victorkiambi/orderbook.git
    cd orderbook-service
    ```

2. Build the application using Gradle:
    ```sh
    gradle build
    ```

## Running the Application

### Using Docker Compose

1. Build and start the services:
    ```sh
    docker-compose up --build
    ```

2. The application will be available at `http://localhost:8080`.

### Without Docker

1. Run the application:
    ```sh
    gradle run
    ```
## Accessing the Application

The application can be accessed at `https://valr.fly.dev/`.

## Running Tests

1. Run the tests using Gradle:
    ```sh
    gradle test
    ```

## Environment Variables

The following environment variables are used in the application:

- `KTOR_APPLICATION_MODULES`: Application modules to load.
- `KTOR_DEPLOYMENT_PORT`: Port on which the application runs.
- `JWT_SECRET`: Secret key for JWT.
- `JWT_ISSUER`: JWT issuer.
- `JWT_AUDIENCE`: JWT audience.
- `JWT_REALM`: JWT realm.

## License

This project is licensed under the MIT License.