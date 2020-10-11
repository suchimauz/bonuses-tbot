(ns tbot.rest
  (:require [immutant.web :as web]
            [ring.util.response :refer [redirect]]
            [environ.core :refer [env]]
            [clj-pg.honey :as pg]
            [reitit.ring :as reitit]
            [tbot.utils :as u]
            (tbot.resources
             [user :as user])
            (ring.middleware
             [keyword-params :refer [wrap-keyword-params]]
             [params :refer [wrap-params]]
             [json :refer [wrap-json-body wrap-json-response]])))

(def handler
  (reitit/ring-handler
   (reitit/router
    [["/user/:id"
      {:get
       {:handler
        (fn [{db :db/connection {:keys [id]} :path-params :as ctx}]
          (let [user (u/get-user-from-chat-id db id)]
            (when user
              (pg/update db user/table {:id (:id user)
                                        :resource (-> user :resource (assoc :click_url true))}))
            (redirect (env :user-url))))}}]])
   (constantly {:status 404, :body {:error {:message "Route not found"}}})))

(defn add-db [handler db]
  (fn [req]
    (handler (assoc req :db/connection db))))

(defn start-server [db]
  (let [stack (-> #'handler
                  (wrap-keyword-params)
                  (wrap-params)
                  (wrap-json-body {:keywords? true})
                  (wrap-json-response)
                  (add-db db))]
    (web/run stack {"host" (or (env :imm-host) "127.0.0.1")
                    "port" (or (env :imm-port) "8081")})))
