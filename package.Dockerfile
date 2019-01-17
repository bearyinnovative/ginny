FROM clojure:lein-2.8.1-alpine

RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories
RUN apk update && apk add --no-cache \
    make \
    git

ARG build_date
ARG commit
ARG version
ARG workspace=/workspace

ENV GINNY_CHANGELOG_CONFIG="./changelog.json"

# Init runner home
RUN mkdir /.lein

# Inite workspace
VOLUME /workspace
WORKDIR /workspace

# Generate version
RUN echo "$version" >> $workspace/version
RUN echo "$commit" >> $workspace/commit
RUN echo "$build_date" >> $workspace/build_date

CMD ["make", "release"]
