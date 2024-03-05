# Deployment

## GitHub Actions for CI/CD

The project uses GitHub Actions for CI/CD, workflows include:

### Build

`.github/workflows/build.yml`: Build and run tests for each commit.
It will also build and push a Docker image for the server to Amazon ECR.

#### Secrets Required

- `AWS_ECR_REGISTRY`: The registry URL of the Amazon ECR.
  E.g. `123456789012.dkr.ecr.us-east-1.amazonaws.com`.
- `AWS_ACCESS_KEY_ID`: The access key ID for the Amazon ECR.
- `AWS_SECRET_ACCESS_KEY`: The secret access key for the Amazon ECR.
- `AWS_REGION`: The region of the Amazon ECR. E.g. `us-east-1`.

- `SIGNING_RELEASE_STOREFILE`: The keystore file for signing the release APK, encoded in base64.
- `SIGNING_RELEASE_KEYALIAS`: The key alias for the keystore.
- `SIGNING_RELEASE_STOREPASSWORD`: The store password for Android signing.
- `SIGNING_RELEASE_KEYPASSWORD`: The key password for Android signing.

### Release

`.github/workflows/release.yml`: Build and release the server and the client.
It also builds and uploads the Android APK to the release draft.

#### Secrets Required

- `SIGNING_RELEASE_STOREFILE`: The keystore file for signing the release APK, encoded in base64.
- `SIGNING_RELEASE_KEYALIAS`: The key alias for the keystore.
- `SIGNING_RELEASE_STOREPASSWORD`: The store password for Android signing.
- `SIGNING_RELEASE_KEYPASSWORD`: The key password for Android signing.

## Building Docker Image

You can find the Dockerfile in the `server` directory.
It can be used to build a Docker image for the server regardless of whether it is in testing mode or
in production mode.

In runtime the environment variables are needed, as described
in [Build/Running the Server](build#running-the-server).
