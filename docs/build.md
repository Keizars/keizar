# Build and Run

## Prerequisites

- JDK 11 or later
- Android SDK 34 or later
- Android Studio 2023.3 or later

## Building the Android Debug APP

Open the project in Android Studio, then build the app by
`Build -> Build App Bundle(s) / APK(s) -> Build APK(s)`.

It's not recommended to build the app from the commandline, because Android Studio can help you
configure dependencies. If you still want to build from the commandline, you can execute from the
project root directory:

```bash
./gradlew app:assembleDebug
```

## Building the Android Release APP

In Android Studio, run `Build -> Generate Signed App Bundle / APK`.
Studio will ask you to set up signing keys, and then builds the release APK.

For commandline usage, you need to manually set up signing keys.
Check the release build script, `.github/workflows/release.yml`, for more details.

## Running the Server

The server supports two modes: (in-memory) testing and production.

You can pass system environment `TESTING=true` to run the server in testing mode,
which will use an in-memory database.

To run the server in production mode, in which it will connect to a real database and an object
storage, pass `TESTING=false` or omit it. Then the following environment
variables are **required**:

### Server Configuration

***Database***

- `MONGODB_CONNECTION_STRING`: The connection string to the MongoDB database.
  Example: `mongodb+srv://keizar:keizar@cluste...`

***Object Storage***

- `AWS_REGION`: Region of the AWS S3 object storage. Example: `us-east-1`
- `AWS_AVATAR_BUCKET_NAME`: The name of the bucket for AWS S3 to store user avatars.
  Example: `keizar`
- `AWS_ACCESS_KEY_ID`: The access key ID for the AWS S3 object storage.
- `AWS_SECRET_ACCESS_KEY`: The secret access key for the AWS S3 object storage.

***Others***

- `TOKEN_SECRET`: RSA key used to encrypt user tokens. For
  example: `MkutjehjQXkEEc4LNRsdEgQ8dQBvR0NL`.
- `PORT`: The port the server listens on. Default is `4392`.

### IDE Run Configuration

There is a pre-configured Run Configuration "Run Test Server" for running the test server in Android
Studio. It passes `TESTING=true` to the server.

![Run Configuration](./.images/build-run-configuration.png)