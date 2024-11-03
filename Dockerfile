# Delightfully there is a community clojure base image
FROM clojure as base_image
WORKDIR /usr/src/app

# If we do this, we can avoid pulling all deps on every run
COPY ./project.clj ./
RUN lein deps

FROM base_image as source_code
COPY ./ ./

FROM source_code
ENTRYPOINT ["lein", "run"]