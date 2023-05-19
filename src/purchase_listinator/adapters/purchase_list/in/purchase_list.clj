(ns purchase-listinator.adapters.purchase-list.in.purchase-list
  (:require [schema.core :as s]
            [purchase-listinator.models.internal.purchase-list.purchase-list :as models.internal.purchase-list]
            [purchase-listinator.wires.purchase-list.in.purchase-list :as wires.in.purchase-list]
            [purchase-listinator.adapters.misc :as adapters.misc]))

(s/defn wire->internal :- models.internal.purchase-list/PurchaseList
  [{:keys [id] :as wire} :- wires.in.purchase-list/PurchaseList]
  (->> (adapters.misc/string->uuid id)
       (assoc wire :id)))
