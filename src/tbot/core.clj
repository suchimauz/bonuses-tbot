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
                                          [:= (hsql/raw "resource->>'click_url'") "false"]
                                          [:= (hsql/raw "resource->>'send_notification'") "false"]
                                          [:> (hsql/raw "(ts::timestamp + '1 minute')")
                                           (hsql/raw "CURRENT_TIMESTAMP")]]}
                                 (pg/query db))]
         (map
          (fn [{:keys [id] :as user}]
            (t/send-text token (-> user :resource :chat_id)
                         (u/build-msg
                          [["Тук-тук, " (-> user :resource :first_name) "!"] []
                           ["Ты не забрал бонусы для слотов, не забыл?"]
                           ["Го крутить колесо по ссылке. Один оборот = фриспины и деньги на счёт."]
                           []
                           ["Желаю удачи!"]]))
            (pg/update db user/table {:id id
                                      :resource (-> user :resource (assoc :send_notification true))}))
          no-click-users)))})
   "0 * * * * *"))

(defn -main [& args]
  (let [db (db/connect)
        cnj (cj/cronj)]
    (migration/migration db)
    (when (str/blank? token)
      (println "Please provde token in BOT_TOKEN environment variable!")
      (System/exit 1))

    (println "Starting the tbot")
    (cron db cnj)
    (rest/start-server db)
    (<!! channel)))

(comment
  (-main)
  (p/stop channel))
