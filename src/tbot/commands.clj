(ns tbot.commands
  (:require [morse.api :as t]
            [cheshire.core :as json]
            [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [clj-http.client :as http]
            (tbot.resources
             [user :as user])
            (tbot
             [db    :as db]
             [utils :as u])))

(defn start [token {{:keys [username first_name last_name id] :as chat} :chat}]
  (let [db   (db/get-db)
        user (u/get-user-from-chat-id db id)]
    (when-not user
      (u/insert-table
       db user/table
       (merge
        (select-keys chat [:username :first_name :last_name])
        {:chat_id           id
         :send_notification false
         :click_url         false})))

    (doseq [msg (yaml/parse-string (slurp (u/make-bf-path "bot.yaml")))]
      (try
        (case (:type msg)
          "message" (cond->>
                        (-> msg :message
                            (u/make-bf-path)
                            (slurp)
                            (u/replace-first-name-in-msg first_name))
                      (:button msg)       (t/send-text token id {:reply_markup (u/reply-markup id (:button msg))})
                      (not (:button msg)) (t/send-text token id))
          "video" (t/send-video
                   token id
                   (merge
                    {}
                    (when-let [button (:button msg)]
                      {:reply_markup (json/generate-string (u/reply-markup id button))})
                    (when-let [message (:message msg)]
                      {:caption (-> message
                                    (u/make-bf-path)
                                    (slurp)
                                    (u/replace-first-name-in-msg first_name))}))
                   (io/file (u/make-bf-path (:file msg))))
          nil)
        (catch Exception e (str "caught exception: " (.getMessage e)))))))
