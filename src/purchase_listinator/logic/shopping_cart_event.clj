(ns purchase-listinator.logic.shopping-cart-event
  (:require [purchase-listinator.models.internal.shopping-list :as models.internal.shopping-list]
            [purchase-listinator.models.internal.shopping-cart :as models.internal.shopping-cart]
            [schema.core :as s]))

(defmulti apply-event (fn [{:keys [event-type]} _] event-type))

(s/defmethod ^:private apply-event :add-item
  [cart-event :- models.internal.shopping-cart/CartEvent
   shopping :- models.internal.shopping-list/ShoppingList]
  shopping)

(s/defmethod ^:private apply-event :order-category
  [cart-event :- models.internal.shopping-cart/CartEvent
   shopping :- models.internal.shopping-list/ShoppingList]
  shopping)

(s/defmethod ^:private apply-event :default
  [{:keys [event-type]} :- models.internal.shopping-cart/CartEvent
   shopping :- models.internal.shopping-list/ShoppingList]
  (println "Not found apply-event function for " event-type " event type")
  shopping)

(s/defn ^:private apply-events
  [[current & remaining] :- (s/maybe [models.internal.shopping-cart/CartEvent])
   shopping :- models.internal.shopping-list/ShoppingList]
  (if (not current)
    shopping
    (recur remaining (apply-event current shopping))))

(s/defn apply-cart :- models.internal.shopping-list/ShoppingList
  [{:keys [events]} :- (s/maybe models.internal.shopping-cart/Cart)
   shopping :- models.internal.shopping-list/ShoppingList]
  (apply-events events shopping))

(s/defn add-event :- models.internal.shopping-cart/Cart
  [{:keys [events] :as cart} :- models.internal.shopping-cart/Cart
   event :- models.internal.shopping-cart/CartEvent]
  (assoc cart :events (conj events event)))
