(ns purchase-listinator.endpoints.http.purchase-list
  (:require [schema.core :as s]
            [purchase-listinator.wires.purchase-list.out.purchase-list :as out.purchases-lists]
            [purchase-listinator.flows.purchase-list :as flows.purchase-list]
            [purchase-listinator.adapters.purchase-list.in.purchase-list :as adapters.in.purchase-list]
            [purchase-listinator.adapters.out.purchase-list :as adapters.out.purchase-list]
            [purchase-listinator.adapters.misc :as adapters.misc]
            [purchase-listinator.adapters.purchase-list.in.purchase-category :as adapters.in.purchase-category]
            [purchase-listinator.adapters.purchase-list.in.purchase-item :as adapters.in.purchase-item]
            [purchase-listinator.flows.purchase-category :as flows.purchase-category]
            [purchase-listinator.flows.purchase-item :as flows.purchase-item]
            [purchase-listinator.misc.either :as misc.either]
            [purchase-listinator.misc.http :as misc.http]
            [purchase-listinator.wires.purchase-list.out.purchase-list :as wires.purchase-list.out.purchase-list]
            [cats.monad.either :refer :all]))

(s/defn get-purchase-lists :- {:status s/Int
                               :body   [out.purchases-lists/PurchaseList]}
  [{{:keys [datomic]} :component
    user-id           :user-id}]
  (misc.http/default-branch
    (misc.either/try-right
      (-> (adapters.misc/string->uuid user-id)
          (flows.purchase-list/get-lists datomic)
          (->> (map adapters.out.purchase-list/internal->wire))))))

(s/defn post-purchase-lists :- {:status s/Int
                                :body   wires.purchase-list.out.purchase-list/PurchaseList}
  [{{datomic :datomic} :component
    {:keys [name]}     :json-params
    user-id            :user-id}]
  (misc.http/default-branch-adapter
    (misc.either/try-right
      (-> (adapters.misc/string->uuid user-id)
          (flows.purchase-list/create name datomic)))
    adapters.out.purchase-list/internal->wire))

(s/defn disable-purchase-lists :- {:status s/Int
                                   :body   {}}
  [{{:keys [datomic]} :component
    {id :id}          :path-params
    user-id           :user-id}]
  (branch (misc.either/try-right (-> (adapters.misc/string->uuid id)
                                     (flows.purchase-list/disable (adapters.misc/string->uuid user-id) datomic)))
          misc.http/->Error
          misc.http/->Success))

(s/defn edit-purchase-lists :- {:status s/Int
                                :body   {}}
  [{{datomic :datomic} :component
    wire               :json-params
    user-id            :user-id}]
  (branch (let [user-id (adapters.misc/string->uuid user-id)]
            (-> (adapters.in.purchase-list/wire->internal wire)
                (flows.purchase-list/edit user-id datomic)))
          misc.http/->Error
          misc.http/->Success))

(s/defn add-purchases-lists-item
  [{{:keys [datomic rabbitmq]} :component
    wire                       :json-params
    user-id                    :user-id}]
  (branch (misc.either/try-right
            (let [internal-item (adapters.in.purchase-item/wire->internal wire)]
              (flows.purchase-item/create internal-item (adapters.misc/string->uuid user-id) datomic rabbitmq)))
          misc.http/->Error
          misc.http/->Success))

(s/defn add-purchases-lists-category
  [{components :component
    wire       :json-params
    user-id    :user-id}]
  (misc.http/default-branch
    (misc.either/try-right
      (-> (adapters.in.purchase-category/creation-wire->internal wire)
          (flows.purchase-category/create (adapters.misc/string->uuid user-id) components)))))

(s/defn change-category-order
  [{{datomic :datomic}        :component
    {:keys [id new-position]} :path-params
    user-id                   :user-id}]
  (branch (misc.either/try-right
            (let [category-id (adapters.misc/string->uuid id)
                  new-position (adapters.misc/string->integer new-position)
                  user-id (adapters.misc/string->uuid user-id)]
              (flows.purchase-category/change-categories-order category-id new-position user-id datomic)))
          misc.http/->Error
          misc.http/->Success))

(s/defn change-item-order
  [{{datomic :datomic}                        :component
    {:keys [id new-category-id new-position]} :path-params
    user-id                                   :user-id}]
  (branch (misc.either/try-right
            (let [item-id (adapters.misc/string->uuid id)
                  new-category-id (adapters.misc/string->uuid new-category-id)
                  new-position (adapters.misc/string->integer new-position)
                  user-id (adapters.misc/string->uuid user-id)]
              (flows.purchase-item/change-items-order item-id new-category-id new-position user-id datomic)))
          misc.http/->Error
          misc.http/->Success))

(s/defn purchases-lists-management-data
  [{{datomic :datomic} :component
    {id :id}           :path-params
    user-id            :user-id}]
  (branch (misc.either/try-right
            (-> (adapters.misc/string->uuid id)
                (flows.purchase-list/management-data (adapters.misc/string->uuid user-id) datomic)))
          misc.http/->Error
          misc.http/->Success))

(s/defn change-item-quantity
  [{{:keys [datomic rabbitmq]} :component
    {:keys [id new-quantity]}  :path-params
    user-id                    :user-id}]
  (misc.http/default-branch (misc.either/try-right
                              (let [new-quantity (adapters.misc/string->integer new-quantity)
                                    item-id (adapters.misc/string->uuid id)
                                    user-id (adapters.misc/string->uuid user-id)]
                                (flows.purchase-item/change-item-quantity item-id new-quantity user-id datomic rabbitmq)))))

(s/defn delete-purchases-lists-item
  [{{:keys [datomic rabbitmq]} :component
    {:keys [id]}               :path-params
    user-id                    :user-id}]
  (misc.http/default-branch (misc.either/try-right
                              (-> (adapters.misc/string->uuid id)
                                  (flows.purchase-item/delete (adapters.misc/string->uuid user-id) datomic rabbitmq)))))

(s/defn edit-item-name
  [{{:keys [datomic rabbitmq]} :component
    {:keys [id new-name]}      :path-params
    user-id                    :user-id}]
  (misc.http/default-branch (misc.either/try-right
                              (-> (adapters.misc/string->uuid id)
                                  (flows.purchase-item/edit-name new-name (adapters.misc/string->uuid user-id) datomic rabbitmq)))))

(s/defn delete-purchases-lists-category
  [{{:keys [datomic rabbitmq]} :component
    {:keys [id]}               :path-params
    user-id                    :user-id}]
  (misc.http/default-branch
    (misc.either/try-right
      (-> (adapters.misc/string->uuid id)
          (flows.purchase-category/delete (adapters.misc/string->uuid user-id) datomic rabbitmq)))))

(s/defn edit-category
  [{{:keys [datomic]} :component
    wire              :json-params
    user-id           :user-id}]
  (misc.http/default-branch (misc.either/try-right
                              (-> (adapters.in.purchase-category/wire->internal wire)
                                  (flows.purchase-category/edit (adapters.misc/string->uuid user-id) datomic)))))

;todo: /lists should return only the purchase list data and /lists/:id should return items and categories too
(def routes
  #{["/api/purchases/lists" :get [get-purchase-lists] :route-name :get-purchases-lists]
    ["/api/purchases/lists" :post [post-purchase-lists] :route-name :post-purchases-lists]
    ["/api/purchases/lists" :put [edit-purchase-lists] :route-name :edit-purchases-lists]
    ["/api/purchases/lists/:id" :delete [disable-purchase-lists] :route-name :disable-purchases-lists]
    ["/api/purchases/categories" :post [add-purchases-lists-category] :route-name :add-purchases-lists-category]
    ["/api/purchases/categories/:id" :delete [delete-purchases-lists-category] :route-name :delete-purchases-lists-category]
    ["/api/purchases/categories" :put [edit-category] :route-name :edit-category]
    ["/api/purchases/categories/:id/changeOrder/:new-position" :put [change-category-order] :route-name :change-category-order]
    ["/api/purchases/items" :post [add-purchases-lists-item] :route-name :add-purchases-lists-item]
    ["/api/purchases/items/:id" :delete [delete-purchases-lists-item] :route-name :delete-purchases-lists-item]
    ["/api/purchases/items/:id/changeName/:new-name" :put [edit-item-name] :route-name :edit-item-name]
    ["/api/purchases/items/:id/changeQuantity/:new-quantity" :put [change-item-quantity] :route-name :change-item-quantity]
    ["/api/purchases/items/:id/changeOrder/:new-category-id/:new-position" :put [change-item-order] :route-name :change-item-order]
    ["/api/purchases/lists/:id/managementData" :get [purchases-lists-management-data] :route-name :purchases-lists-management-data]})
