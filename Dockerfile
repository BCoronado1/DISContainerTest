FROM ubuntu:20.04
RUN apt -y update && apt -y upgrade && apt install -y openjdk-14-jdk-headless maven
COPY . /app
WORKDIR /app
RUN bash build.sh
ENTRYPOINT bash run_app.sh