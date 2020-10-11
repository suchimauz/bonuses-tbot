(ns tbot.db
  (:require [clj-pg.pool :as pool]))

(defonce ds (atom nil))

(defn get-db []
  @ds)

(defn connect []
  (if-not @ds
    (let [database-url (str "jdbc:postgresql://"
                            (or (System/getenv "PGHOST") "localhost")
                            ":"
                            (or (System/getenv "PGPORT") "5429")
                            "/" (or (System/getenv "PGDATABASE") "postgres")
                            "?user=" (or (System/getenv "PGUSER") "postgres")
                            "&password=" (or (System/getenv "PGPASSWORD") "postgres")
                            "&stringtype=unspecified")]
      (reset! ds
              {:datasource (pool/create-pool {:idle-timeout        10000
                                              :minimum-idle        1
                                              :maximum-pool-size   3
                                              :connection-init-sql "select 1"
                                              :data-source.url     database-url})}))
    @ds))
