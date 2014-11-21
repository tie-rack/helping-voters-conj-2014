#  Helping voters with Pedestal, Datomic, Om and core.async

This is the final result of the [Helping Voters][helping-voters] talk
by Nathan Herzing and Chris Shea at the 2014 Clojure/conj.

## Dependencies

* [Datomic][datomic]
* [Redis][redis]
* [Leiningen][leiningen] (naturally)

## Setup

1. Run datomic and redis on their standard ports.
1. In the `frontend` directory, run `lein cljsbuild once`.
1. In the `api` directory, run `lein reset-db` and then `lein run-dev`.
1. In the `ftp` directory, run `lein run -m usps-ftp-queuer.core`.
1. Open `frontend/voter.html` and enter `000000000` or `000000001` into the search field.
1. FTP to the server running at port 2221: `ftp -P 2221 ftp://admin:admin@localhost`.
1. Upload one of the sample files: `put ftp/usps-scans-2.csv demo`.

You should see the "status" field update in the browser.

See the individual READMEs for more details.

[helping-voters]: https://www.youtube.com/watch?v=Ohuadp9S2hg
[datomic]: http://www.datomic.com/
[redis]: http://redis.io/
[leiningen]: http://leiningen.org/
