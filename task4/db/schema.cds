namespace task2.db;

entity Dealers {
    key dealerId   : Integer;
        dealerName : String;
        stateId    : Integer;
        state      : Association to States on state.stateId = stateId; 

        vehicles   : Association to many Vehicles on vehicles.dealer = $self;
}

entity Customers {
    key customerId      : Integer;
        customerName    : String;
        phn             : Integer;
        dealerId        : Integer;

        dealer          : Association to Dealers on dealer.dealerId = dealerId;
        
}

entity Delivery {
    key deliveryId : Integer;
        status     : String;

        order      : Association to Orders;
        dealer     : Association to Dealers;
}

entity Vehicles {
    key vehicleId  : String;
        modelName  : String;
        cur_price  : Decimal(10, 2);
        old_price  : Decimal(10, 2);

        dealer     : Association to Dealers;
        orders     : Composition of many Orders on orders.vehicle = $self;
}

entity Orders {
    key orderId    : Integer;
        quantity   : Integer;

        vehicle    : Association to Vehicles;
}

entity States {
    key stateId      : Integer;
        stateName    : String;
        stateCode    : String;
}

entity stateView as select from States { stateId };
entity vehicleView as projection on Vehicles { vehicleId };
