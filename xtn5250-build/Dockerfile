FROM maven:3.5

# install jdk11
RUN echo 'deb http://ftp.debian.org/debian stretch-backports main' > /etc/apt/sources.list.d/backports.list \
  && apt-get update \
	&& apt-get install -y openjdk-11-jdk-headless

# install ssh client to be able to release code and use git with ssh
RUN apt-get update \
  && apt-get -y install openssh-client

