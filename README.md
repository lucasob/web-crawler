# web-crawler

A Clojure library designed to crawl web sites.

## Replication

* Tested and running on `openjdk 16.0.2 2021-07-20`
* Using clojure `1.11.1`
* You will need `Leiningen`

### Installation of Lein

```shell
brew install leiningen
```

## Testing

* We rely on [testcontainers](https://testcontainers.com) to abstract away spinning up dependencies
* We use [WireMock](https://wiremock.org) to be able to properly stub HTTP calls

## Commands

### Run the code

```shell
lein run <URL>
```

Would, however, recommend piping to jq

```shell
lein run https://lucasob.github.io | jq
```

### Run Tests

```shell
lein test
```

### Pull Dependencies

```shell
lein deps
```

### View coverage

(This will download additional deps on first run)

```shell
lein cloverage
```

## Useful Bits

### Enable test container reuse

This is optional, but enabling test container reuse means not spinning up a container for each test

Within `~/.testcontainers.properties` set `testcontainers.reuse.enable=true`

