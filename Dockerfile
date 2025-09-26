# Stage 1: Build the application with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Stage 2: Create the runtime image with OpenCV
FROM ubuntu:22.04
WORKDIR /app

# Install Java
RUN apt-get update && \
    apt-get install -y openjdk-21-jre-headless && \
    apt-get clean

# Install OpenCV 4.9.0 from source
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    git \
    wget \
    unzip \
    libgtk2.0-dev \
    pkg-config \
    libavcodec-dev \
    libavformat-dev \
    libswscale-dev && \
    rm -rf /var/lib/apt/lists/*

RUN wget -qO- https://github.com/opencv/opencv/archive/refs/tags/4.9.0.zip > opencv.zip && \
    unzip opencv.zip && \
    rm opencv.zip

WORKDIR /app/opencv-4.9.0/build

RUN cmake -D CMAKE_BUILD_TYPE=RELEASE \
    -D CMAKE_INSTALL_PREFIX=/usr/local \
    -D BUILD_EXAMPLES=OFF \
    -D BUILD_TESTS=OFF \
    -D BUILD_PERF_TESTS=OFF \
    -D BUILD_opencv_java=ON \
    -D BUILD_SHARED_LIBS=OFF \
    ..

RUN make -j$(nproc) && make install && ldconfig

# Copy the application JAR from the build stage
WORKDIR /app
COPY --from=build /app/target/stamp-extractor-1.0.0.jar .
COPY --from=build /app/opencv-4.9.0/build/lib/opencv-490.jar ./opencv-490.jar

# Set the entrypoint
CMD ["java", "-Djava.library.path=/usr/local/share/java/opencv4", "-jar", "stamp-extractor-1.0.0.jar"]
