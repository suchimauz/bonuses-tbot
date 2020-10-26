(ns tbot.commands
  (:require [morse.api :as t]
            [cheshire.core :as json]
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
        {:chat_id id
         :send_notification false
         :click_url false})))
    (t/send-text token id {:reply_markup (u/reply-markup id "🔥 Крутить колесо 🔥")}
                 (u/build-msg
                  [["Приветствую тебя, " first_name " 😉"] []
                   ["Я твой виртуальный помощник Jet Casino 🦾🤖"]
                   ["Наша международная лицензия 📃"]
                   ["еженедельные бонусные акции 🎁"]
                   ["cashback до 10% ♻️"]
                   ["а также турниры и лотереи с призовыми фондами вывели наше КАЗИНО на новый уровень! 🔝"] []
                   ["Жми кнопку \"КРУТИТЬ КОЛЕСО\" и получи от нас ДВА стартовых ПОДАРКА!"]
                   ["Искренне желаем космических побед 🪐🏅"]
                   ["и огромных выигрышей 💰💰💰"]]))
    (t/send-text token id {:reply_markup (u/reply-markup id "🎁 Забрать бонус 🎁")}
                 (u/build-msg
                  [["Мы уже подготовили для тебя МЕГА-КРУТЫЕ БОНУСЫ 🥳🥳🥳"] []
                   ["Чтобы их забрать:"]
                   ["1️⃣  Жми на кнопку ниже ⬇️"]
                   ["2️⃣ Соверши простую регистрацию ✔️"]
                   ["3️⃣ Пополни баланс всего от 200 рублей любым доступным способом"] []
                   ["И бонус \"+100% К ПЕРВОМУ ДЕПОЗИТУ\" активируется автоматически!"]
                   ["Погнали побеждать! 😉"]]))
    (t/send-video
     token id
     {:reply_markup (json/generate-string (u/reply-markup id "Вперед за победами 🚀🏆"))
      :duration "112"
      :thumb (io/file "/resources/thumb.jpg")
      :caption "Взгляни как люди побеждают на наших бонусах в виде бесплатных вращений ☺️"}
     (io/file "/resources/video.mp4"))))
