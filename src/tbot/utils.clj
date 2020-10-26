(ns tbot.utils
  (:require [clj-pg.honey :as pg]
            [environ.core :refer [env]]
            [clojure.string :as str]
            [honeysql.core :as hsql]))

(defn replace-first-name-in-msg [text first-name]
  (if text
    (str/replace text #"\{\{name\}\}" first-name)
    text))

(defn make-bf-path [file]
  (str (env :bot-files) file))

(defn reply-markup [id text]
  {:inline_keyboard [[{:text text
                       :url (str
                             (or (env :bot-host) "127.0.0.1")
                             (when-let [port (env :bot-port)]
                               (str ":" port))
                             "/user/" id)}]]})

(defn new-table [resource validator]
  {:table     (keyword (str/lower-case (name resource)))
   :columns   {:id            {:type :serial :primary true :weight 0}
               :resource_type {:type :varchar :not-null true :default (str "'" (name resource) "'")}
               :resource      {:type :jsonb}
               :cts           {:type :timestamptz :default "CURRENT_TIMESTAMP"}
               :ts            {:type :timestamptz :default "CURRENT_TIMESTAMP"}}
   :validator validator})

(defn insert-table [db table body]
  (pg/create db table {:resource (dissoc body :id :resourceType)}))

(defn get-user-from-chat-id-hsql [chat-id]
  {:select [:*]
   :from [:user]
   :where [:=
           (hsql/raw "resource->>'chat_id'")
           (str chat-id)]})

(defn get-user-from-chat-id [db chat-id]
  (->> chat-id
       get-user-from-chat-id-hsql
       (pg/query-first db)))

(defn put-table [table db body]
  (->> body
       :chat_id
       (get-user-from-chat-id db)
       (fn [{:keys [id]}]
         (println id)
         (pg/update db table {:id id :resource (dissoc body :id :resourceType)}))))

(defn is-click-url? [db chat-id]
  (->> chat-id
       (get-user-from-chat-id db)
       :resource
       :click_url
       #{"true" true}))

(defn build-msg [args]
  (->> args
       (map (partial apply str))
       (str/join "\n")))
