pisto is a simplistic formalization of the lifecycle of stateful systems. Removes a lot of the pain related to debugging a system of many simple services as long as they can be run in a single JVM.

## Getting Started

A system of, for example, simple services can be expressed in a namespace like this.
```clj
(ns system.core
  (:require [pisto.core :as pisto])
  (:require [clojure.tools.namespace.repl :as tns])
  (:require [wildrank.site :as wildrank])
  (:require [annagreta.site :as annagreta]))

;all system state (at least the state needed in order to tear down the system) will be accessible through this var after system startup
(defonce system {:start-order [:annagreta :wildrank]
                 :parts {:annagreta {:config {:db {:uri "datomic:mem://auth"}
                                              :uri {:port 8060}}}
                         :wildrank {:config {:uri {:port 8080}}}}})

(defn restart [] (alter-var-root #'system #(pisto/restart-parts %)))
(defn start   [] (alter-var-root #'system #(pisto/start-parts %)))
(defn stop    [] (alter-var-root #'system #(pisto/stop-parts %))
                 (alter-var-root #'system #(pisto/clear-all-stateful-info %)))

;These should be mapped to some keyboard shortcuts in the dev environment
(defn reload-and-restart [] (stop) (tns/refresh-all :after 'system.core/start))
(defn update-and-restart [] (stop) (tns/refresh     :after 'system.core/start))
```

It is then up to each part/sub system to ensure it can be started and stopped:
```clj
(ns annagreta.site
    (:require [net.cgrand.moustache :as moustache])
    (:require [pisto.core :as pisto]))

(defonce state nil)

(def routes (-> (moustache/app ["hello-world-resource"] (moustache/app :get (fn [req] {:body "hello world!!!"})))

(defmethod pisto/start-part :annagreta [[type {:keys [config]}]]
  (let [db-uri (:uri (:db config))
        site-jetty-port (:port (:uri config))]
    (alter-var-root #'state
                    (fn [_]
                      {:auth-db-conn (when db-uri (connect-and-init! db-uri))
                       :site-jetty (when site-jetty-port (run-jetty #'routes {:port site-jetty-port :join? false}))}))))

(defmethod pisto/stop-part  :annagreta [[type part]]
  (let [db-uri (:uri (:db (:config part)))
        site-jetty (:site-jetty (:state part))]
    (when site-jetty (.stop site-jetty))
    (d/delete-database db-uri)))
```

## Project Maturity

Developed primarily for my personal use. API's can change without notice.

## Artifacts

[Pisto on Clojars](https://clojars.org/pisto). If you are using Maven, add the following repository
definition to your `pom.xml`:

```xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

With Leiningen:
[pisto "0.1"]

## Major dependencies

 * [Clojure](http://clojure.org/) (version 1.5.1)

## License

Copyright (C) 2013 [Marcus Holst](https://twitter.com/zolst)

Licensed under the [Eclipse Public License v1.0](http://www.eclipse.org/legal/epl-v10.html) (the same as Clojure).