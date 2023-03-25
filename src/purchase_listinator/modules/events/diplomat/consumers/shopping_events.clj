(ns purchase-listinator.modules.events.diplomat.consumers.shopping-events
  (:require [schema.core :as s]
            [purchase-listinator.modules.events.schemas.wires.in.shopping-finished-event :as wires.in.shopping-finished-event]
            [purchase-listinator.modules.events.adapters.shopping-events :as events.adapters.shopping-events]
            [purchase-listinator.modules.events.flows.shopping-events :as events.flows.shopping-events]))

(s/defn shopping-events-received
  [_channel
   _metadata
   components
   event :- wires.in.shopping-finished-event/ShoppingFinishedEvent]
  (-> (events.adapters.shopping-events/wire->internal event)
      (events.flows.shopping-events/receive-events components)))

(def subscribers
  [{:exchange :purchase-listinator/shopping.finished
    :queue    :purchase-listinator.events/shopping-events-receive
    :schema   wires.in.shopping-finished-event/ShoppingFinishedEvent
    :handler  shopping-events-received}])
