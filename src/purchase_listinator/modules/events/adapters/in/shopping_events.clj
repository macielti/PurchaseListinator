(ns purchase-listinator.modules.events.adapters.in.shopping-events
  (:require [purchase-listinator.modules.events.schemas.wires.in.shopping-finished-event :as wires.in.shopping-finished-event]
            [purchase-listinator.modules.events.schemas.wires.in.shopping-events :as modules.events.schemas.wires.in.events]
            [purchase-listinator.modules.events.schemas.models.shopping-events :as modules.events.schemas.models.shopping-event]
            [schema.core :as s]))

(s/defn ^:private ->internal :- modules.events.schemas.models.shopping-event/ShoppingEvent
  [{:keys [id moment event-type user-id shopping-id item-id category-id] :as wire} :- modules.events.schemas.wires.in.events/Event]
  {:id          id
   :moment      moment
   :event-type  event-type
   :user-id     user-id
   :shopping-id shopping-id
   :item-id     item-id
   :category-id category-id
   :properties  (dissoc wire :id :moment :event-type :user-id :shopping-id :item-id :category-id)})
(s/defn wire->internal :- modules.events.schemas.models.shopping-event/ShoppingEventCollection
  [{:keys [events]} :- wires.in.shopping-finished-event/ShoppingFinishedEvent]
  {:events (map ->internal events)})