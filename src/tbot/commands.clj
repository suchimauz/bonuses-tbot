(ns tbot.commands
  (:require [morse.api :as t]
            [environ.core :refer [env]]
            (tbot.resources
             [user :as user])
            (tbot
             [db    :as db]
             [utils :as u])))

(def at (atom nil))

(defn start [token {{:keys [username first_name last_name id] :as chat} :chat}]
  (let [db   (db/get-db)
        user (u/get-user-from-chat-id db id)
        reply_markup {:inline_keyboard [[{:text "Перейти по ссылке"
                                          :url (str
                                                (or (env :imm-host) "127.0.0.1")
                                                (when-let [port (env :imm-port)]
                                                  (str ":" port))
                                                "/user/" id)}]]}]
    (reset! at id)
    (when-not user
      (u/insert-table
       db user/table
       (merge
        (select-keys chat [:username :first_name :last_name])
        {:chat_id id
         :send_notification false
         :click_url false}))
      (t/send-text token id {:reply_markup reply_markup}
                   (u/build-msg
                    [["Привет, " first_name ". Погнали покорять слот-вселенную?"] []

                     ["Для тебя есть крутые бонусы!"] []

                     ["Чтобы их забрать:"]
                     ["1. Перейти по ссылке ниже;"]
                     ["2. Зарегистрируйся;"]
                     ["3. Внеси депозит от 50$."] []

                     ["Бонусы ждут тебя. Бонусы не требуют отыгрыша реальных денег, так что, залетай!"]
                     ["Кликай на кнопку и погнали!"]])))
    (when user
      (t/send-text token id {:reply_markup reply_markup}
                   (u/build-msg
                    [["Привет, " first_name "!"] []
                     ["Мы с тобой уже знакомы и искренне рады этому, если ты пришел за ссылкой, кликай на кнопку ниже!"]])))))
