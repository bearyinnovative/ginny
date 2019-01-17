FROM nginx:1.14-alpine

ARG build_date
ARG commit
ARG version
# Predefined environment (NOTE should not override, for ENTRYPOINT usage)
ENV WORKSPACE=/workspace
ENV LOG_PATH=/workspace/log

# Init workspace
RUN mkdir -p $WORKSPACE/www
WORKDIR $WORKSPACE

# Generate version and build information
RUN echo "$version" >> $WORKSPACE/version
RUN echo "$commit" >> $WORKSPACE/commit
RUN echo "$build_date" >> $WORKSPACE/build_date

# Init log directory
RUN mkdir -p $LOG_PATH
VOLUME $LOG_PATH

COPY deploy/nginx.conf.template /etc/nginx/nginx.conf.template
COPY output/ $WORKSPACE/www

EXPOSE 80

CMD /bin/sh -c "envsubst '\$WORKSPACE \$LOG_PATH' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf && exec nginx -g 'daemon off;'"
