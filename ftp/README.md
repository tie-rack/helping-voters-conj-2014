# usps-ftp-queuer

Accepts FTP uploads, queues each line into Redis

## Usage

1. Start Redis: `redis-server /usr/local/etc/redis.conf`
2. Start this: `lein run -m usps-ftp-queuer.core`.
3. FTP in: `ftp -P 2221 ftp://admin:admin@localhost`
4. Upload a file: `put path/to/local remote`
