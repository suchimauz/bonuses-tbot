(ns tbot.core
  (:require [clojure.core.async :refer [<!!]]
            [honeysql.core :as hsql]
            [clj-pg.honey :as pg]
            [clojure.string :as str]
            [cronj.core :as cj]
            [cronj.data.task :as tk]
            [environ.core :refer [env]]
            [morse.handlers :as h]
            [morse.polling :as p]
            [morse.api :as t]
            [tbot.resources.user :as user]
            (tbot
             [rest :as rest]
             [commands :as com]
             [db :as db]
             [utils :as u]
             [migration :as migration]))
  (:gen-class))

(def token (env :bot-token))

(h/defhandler handler
  (h/command-fn "start" (partial com/start token)))

(def channel (p/start token handler))

(defn cron [db cnj]
  (cj/start! cnj)
  (cj/schedule-task
   cnj
   (tk/task
    {:id :job
     :handler
     (fn [dt opts]
       (let [no-click-users (->> {:select [:*]
                                  :from [:user]
                                  :where [:and
                                          [:= (hsql/raw "resource->>'click_url'") (hsql/raw "'false'")]
                                          [:= (hsql/raw "resource->>'send_notification'") (hsql/raw "'false'")]
                                          [:< (hsql/raw "(cts::timestamp + '15 minute')")
                                           (hsql/raw "CURRENT_TIMESTAMP")]]}
                                 (pg/query db))
             _ (println no-click-users)]
         (doall
          (map
           (fn [{:keys [id] :as user}]
             (let [chat-id (-> user :resource :chat_id)]
               (t/send-text token chat-id {:reply_markup (u/reply-markup chat-id "🎁 Забрать бонусы 🎁")}
                            (u/build-msg
                             [["❗️❗️❗️Ты забыл забрать свои бонусы❗️❗️❗️"]
                              [" Возвращайся скорее, они уже ждут тебя в личном кабинете 🖥"]])))
             (pg/update db user/table {:id id
                                       :resource (-> user :resource (assoc :send_notification true))}))
           no-click-users))))})
   "0 * * * * * *"))

(defn -main [& args]
  (let [db (db/connect)
        cnj (cj/cronj)]
    (migration/migration db)
    (when (str/blank? token)
      (println "Please provide token in BOT_TOKEN environment variable!")
      (System/exit 1))

    (println "Starting the tbot")
    (cron db cnj)
    (rest/start-server db)
    (<!! channel)))

(comment
  (-main)
  (p/stop channel))
