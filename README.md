# web-crawler

A Clojure library designed to crawl web sites.

## Replication

* Tested and running on `openjdk 16.0.2 2021-07-20`
* Using clojure `1.11.1`
* You will need `Leiningen`

### Running Directly

You can invoke using leiningen directly, if you install lein accordingly

```shell
brew install leiningen
```

## Docker

Given I don't want to make anyone's day terrible, I've wrapped this up to be invoked from docker

### Build

```shell
docker build -t crawler .
```

### Run

(I'd still recommend piping to JQ)

```shell
docker run crawler https://lucasob.github.io
```

### Compose

At this point, I've run out of other ways to help 😉

The logs are a bit bad, and you won't get nice jq, but regardless.

You have to manually specify the url you want inside the compose file.

```shell
docker compose up crawler
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

### Slow?

Yeah, the JVM has to start up every time you use lein 😭

### Enable test container reuse

This is optional, but enabling test container reuse means not spinning up a container for each test

Within `~/.testcontainers.properties` set `testcontainers.reuse.enable=true`

