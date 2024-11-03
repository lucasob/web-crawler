# web-crawler

A Clojure library designed to crawl web sites.

## Usage

TODO

## Testing

* We rely on [testcontainers](https://testcontainers.com) to abstract away spinning up dependencies
* We use [WireMock](https://wiremock.org) to be able to properly stub HTTP calls

## Commands

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

