FROM redhat/ubi9
RUN groupadd --gid 1000 vscode \
    && useradd -s /bin/bash --uid 1000 --gid 1000 -m vscode
RUN yum install -y java-17-openjdk-devel maven git
RUN for c in java javac; do alternatives --set $c java-17-openjdk.x86_64; done
RUN echo JAVA_HOME=/usr/lib/jvm/java-17-openjdk >> /etc/java/maven.conf
