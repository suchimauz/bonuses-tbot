(ns tbot.resources.user
  (:require [tbot.utils :as u]))

(def table
  (u/new-table
   :User
   {:type       "object"
    :properties {:chat_id    {:type     :string
                              :required true}
                 :username   {:type :string}
                 :first_name {:type :string}
                 :last_name  {:type :string}
                 :click_url  {:type     :string
                              :required true}
                 :send_notification {:type :string
                                     :required true}}}))
