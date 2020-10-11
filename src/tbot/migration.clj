(ns tbot.migration
  (:require [clj-pg.honey  :as pg]
            [honeysql.core :as hsql]
            [honeysql.types :as hsql-types]
            [cheshire.core :as json]
            [tbot.db        :as db]
            (tbot.resources
             [user :as user])))

(defn migrate [db table]
  (when-not (pg/table-exists? db (:table table))
    (pg/create-table db table)))

(defn migration [db]
  (migrate db user/table))
